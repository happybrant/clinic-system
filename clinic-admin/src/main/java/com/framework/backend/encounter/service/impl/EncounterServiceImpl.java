package com.framework.backend.encounter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.SerialNoService;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.encounter.entity.Encounter;
import com.framework.backend.encounter.mapper.EncounterMapper;
import com.framework.backend.encounter.service.EncounterService;
import com.framework.backend.patient.entity.Patient;
import com.framework.backend.patient.service.PatientService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EncounterServiceImpl extends ServiceImpl<EncounterMapper, Encounter>
    implements EncounterService {

  @Autowired private SerialNoService serialNoService;
  @Autowired private PatientService patientService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Encounter createWaiting(Encounter encounter) {
    if (encounter == null || StringUtils.isBlank(encounter.getPatientId())) {
      throw new BusinessException("患者不能为空");
    }
    fillPatientSnapshot(encounter);
    encounter.setEncounterNo(serialNoService.next(ClinicConstants.PREFIX_ENCOUNTER));
    encounter.setStatus(ClinicConstants.ENC_WAITING);
    if (StringUtils.isBlank(encounter.getRegisterType())) {
      encounter.setRegisterType("appointment");
    }
    if (encounter.getRegisterFee() == null) {
      encounter.setRegisterFee(BigDecimal.ZERO);
    }
    if (encounter.getQueueNo() == null) {
      encounter.setQueueNo(nextQueueNo(encounter.getDoctorId()));
    }
    save(encounter);
    return encounter;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Encounter registerWalkIn(Encounter encounter) {
    if (encounter == null || StringUtils.isBlank(encounter.getPatientId())) {
      throw new BusinessException("患者不能为空");
    }
    if (StringUtils.isBlank(encounter.getDoctorId())) {
      throw new BusinessException("医生不能为空");
    }
    encounter.setRegisterType("walkin");
    return createWaiting(encounter);
  }

  @Override
  public MyPage<Encounter> pageList(Encounter encounter) {
    QueryWrapper<Encounter> qw = new QueryWrapper<>();
    if (encounter != null) {
      if (StringUtils.isNotBlank(encounter.getPatientId())) {
        qw.lambda().eq(Encounter::getPatientId, encounter.getPatientId());
      }
      if (StringUtils.isNotBlank(encounter.getDoctorId())) {
        qw.lambda().eq(Encounter::getDoctorId, encounter.getDoctorId());
      }
      if (StringUtils.isNotBlank(encounter.getDeptId())) {
        qw.lambda().eq(Encounter::getDeptId, encounter.getDeptId());
      }
      if (StringUtils.isNotBlank(encounter.getStatus())) {
        qw.lambda().eq(Encounter::getStatus, encounter.getStatus());
      }
      if (StringUtils.isNotBlank(encounter.getPatientName())) {
        qw.lambda().like(Encounter::getPatientName, encounter.getPatientName());
      }
    }
    qw.lambda().orderByAsc(Encounter::getQueueNo).orderByDesc(Encounter::getCreateTime);
    long current =
        encounter != null && encounter.getCurrentPage() != null ? encounter.getCurrentPage() : 1L;
    long size =
        encounter != null && encounter.getPageSize() != null ? encounter.getPageSize() : 20L;
    return baseMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Encounter callNext(Encounter encounter) {
    if (encounter == null || StringUtils.isBlank(encounter.getDoctorId())) {
      throw new BusinessException("医生不能为空");
    }
    QueryWrapper<Encounter> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(Encounter::getDoctorId, encounter.getDoctorId())
        .eq(Encounter::getStatus, ClinicConstants.ENC_WAITING)
        .ge(Encounter::getCreateTime, LocalDate.now().atStartOfDay())
        .le(Encounter::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MAX))
        .orderByAsc(Encounter::getQueueNo)
        .orderByAsc(Encounter::getCreateTime)
        .last("limit 1");
    Encounter next = getOne(qw, false);
    if (next == null) {
      throw new BusinessException("当前无候诊患者");
    }
    start(next.getId());
    return getById(next.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void start(String id) {
    Encounter exist = requireEncounter(id);
    if (!ClinicConstants.ENC_WAITING.equals(exist.getStatus())
        && !ClinicConstants.ENC_SKIPPED.equals(exist.getStatus())) {
      throw new BusinessException("仅候诊或过号状态可接诊");
    }
    exist.setStatus(ClinicConstants.ENC_IN_CONSULT);
    exist.setStartTime(LocalDateTime.now());
    updateById(exist);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void skip(String id) {
    Encounter exist = requireEncounter(id);
    if (!ClinicConstants.ENC_WAITING.equals(exist.getStatus())
        && !ClinicConstants.ENC_IN_CONSULT.equals(exist.getStatus())) {
      throw new BusinessException("当前状态不可过号");
    }
    exist.setStatus(ClinicConstants.ENC_SKIPPED);
    updateById(exist);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void finish(String id) {
    Encounter exist = requireEncounter(id);
    if (!ClinicConstants.ENC_IN_CONSULT.equals(exist.getStatus())) {
      throw new BusinessException("仅接诊中可完成就诊");
    }
    exist.setStatus(ClinicConstants.ENC_FINISHED);
    exist.setEndTime(LocalDateTime.now());
    updateById(exist);

    Patient patient = patientService.getById(exist.getPatientId());
    if (patient != null) {
      int visit = patient.getVisitCount() == null ? 0 : patient.getVisitCount();
      patient.setVisitCount(visit + 1);
      patient.setLastVisitTime(LocalDateTime.now());
      patientService.updateById(patient);
    }
  }

  @Override
  public Encounter getDetail(String id) {
    return requireEncounter(id);
  }

  private Encounter requireEncounter(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("就诊id不能为空");
    }
    Encounter exist = getById(id);
    if (exist == null) {
      throw new BusinessException("就诊不存在");
    }
    return exist;
  }

  private void fillPatientSnapshot(Encounter encounter) {
    Patient patient = patientService.getById(encounter.getPatientId());
    if (patient == null) {
      throw new BusinessException("患者不存在");
    }
    if (StringUtils.isBlank(encounter.getPatientName())) {
      encounter.setPatientName(patient.getName());
    }
    if (StringUtils.isBlank(encounter.getPatientGender())) {
      encounter.setPatientGender(patient.getGender());
    }
    if (encounter.getPatientAge() == null) {
      encounter.setPatientAge(patient.getAge());
    }
    if (StringUtils.isBlank(encounter.getPatientMobile())) {
      encounter.setPatientMobile(patient.getMobile());
    }
  }

  private Integer nextQueueNo(String doctorId) {
    QueryWrapper<Encounter> qw = new QueryWrapper<>();
    qw.lambda()
        .ge(Encounter::getCreateTime, LocalDate.now().atStartOfDay())
        .le(Encounter::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
    if (StringUtils.isNotBlank(doctorId)) {
      qw.lambda().eq(Encounter::getDoctorId, doctorId);
    }
    qw.lambda().orderByDesc(Encounter::getQueueNo).last("limit 1");
    Encounter max = getOne(qw, false);
    int current = max == null || max.getQueueNo() == null ? 0 : max.getQueueNo();
    return current + 1;
  }
}
