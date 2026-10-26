package com.framework.backend.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.SerialNoService;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.patient.entity.Patient;
import com.framework.backend.patient.entity.PatientContact;
import com.framework.backend.patient.entity.PatientLog;
import com.framework.backend.patient.mapper.PatientContactMapper;
import com.framework.backend.patient.mapper.PatientLogMapper;
import com.framework.backend.patient.mapper.PatientMapper;
import com.framework.backend.patient.service.PatientService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient>
    implements PatientService {

  @Autowired private SerialNoService serialNoService;
  @Autowired private PatientContactMapper patientContactMapper;
  @Autowired private PatientLogMapper patientLogMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void add(Patient patient) {
    if (patient == null || StringUtils.isBlank(patient.getName())) {
      throw new BusinessException("患者姓名不能为空");
    }
    patient.setPatientNo(serialNoService.next(ClinicConstants.PREFIX_PATIENT));
    if (StringUtils.isBlank(patient.getStatus())) {
      patient.setStatus(ClinicConstants.STATUS_ACTIVE);
    }
    if (patient.getVisitCount() == null) {
      patient.setVisitCount(0);
    }
    if (patient.getTotalAmount() == null) {
      patient.setTotalAmount(BigDecimal.ZERO);
    }
    save(patient);
    if (patient.getContacts() != null) {
      for (PatientContact contact : patient.getContacts()) {
        contact.setPatientId(patient.getId());
        patientContactMapper.insert(contact);
      }
    }
    PatientLog log = new PatientLog();
    log.setPatientId(patient.getId());
    log.setOperation("create");
    log.setContent("新建患者档案");
    log.setOperateTime(LocalDateTime.now());
    patientLogMapper.insert(log);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updatePatient(Patient patient) {
    if (patient == null || StringUtils.isBlank(patient.getId())) {
      throw new BusinessException("患者id不能为空");
    }
    Patient exist = getById(patient.getId());
    if (exist == null) {
      throw new BusinessException("患者不存在");
    }
    updateById(patient);
    if (patient.getContacts() != null) {
      QueryWrapper<PatientContact> qw = new QueryWrapper<>();
      qw.lambda().eq(PatientContact::getPatientId, patient.getId());
      patientContactMapper.delete(qw);
      for (PatientContact contact : patient.getContacts()) {
        contact.setId(null);
        contact.setPatientId(patient.getId());
        patientContactMapper.insert(contact);
      }
    }
    PatientLog log = new PatientLog();
    log.setPatientId(patient.getId());
    log.setOperation("update");
    log.setContent("更新患者档案");
    log.setOperateTime(LocalDateTime.now());
    patientLogMapper.insert(log);
  }

  @Override
  public MyPage<Patient> pageList(Patient patient) {
    QueryWrapper<Patient> qw = new QueryWrapper<>();
    if (patient != null && StringUtils.isNotBlank(patient.getKeyword())) {
      String kw = patient.getKeyword().trim();
      qw.lambda()
          .and(
              w ->
                  w.like(Patient::getName, kw)
                      .or()
                      .like(Patient::getMobile, kw)
                      .or()
                      .like(Patient::getPatientNo, kw));
    }
    if (patient != null && StringUtils.isNotBlank(patient.getStatus())) {
      qw.lambda().eq(Patient::getStatus, patient.getStatus());
    }
    qw.lambda().orderByDesc(Patient::getCreateTime);
    long current = patient != null && patient.getCurrentPage() != null ? patient.getCurrentPage() : 1L;
    long size = patient != null && patient.getPageSize() != null ? patient.getPageSize() : 10L;
    MyPage<Patient> page = new MyPage<>(current, size);
    return baseMapper.selectPage(page, qw);
  }

  @Override
  public Patient getDetail(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("患者id不能为空");
    }
    Patient patient = getById(id);
    if (patient == null) {
      throw new BusinessException("患者不存在");
    }
    QueryWrapper<PatientContact> cqw = new QueryWrapper<>();
    cqw.lambda().eq(PatientContact::getPatientId, id);
    patient.setContacts(patientContactMapper.selectList(cqw));
    QueryWrapper<PatientLog> lqw = new QueryWrapper<>();
    lqw.lambda().eq(PatientLog::getPatientId, id).orderByDesc(PatientLog::getOperateTime);
    patient.setLogs(patientLogMapper.selectList(lqw));
    return patient;
  }

  @Override
  public void addLog(PatientLog log) {
    if (log == null || StringUtils.isBlank(log.getPatientId())) {
      throw new BusinessException("患者id不能为空");
    }
    if (log.getOperateTime() == null) {
      log.setOperateTime(LocalDateTime.now());
    }
    patientLogMapper.insert(log);
  }
}
