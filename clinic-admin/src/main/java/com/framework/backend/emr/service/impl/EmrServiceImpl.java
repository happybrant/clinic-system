package com.framework.backend.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.emr.entity.Emr;
import com.framework.backend.emr.mapper.EmrMapper;
import com.framework.backend.emr.service.EmrService;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmrServiceImpl extends ServiceImpl<EmrMapper, Emr> implements EmrService {

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Emr saveDraft(Emr emr) {
    if (emr == null || StringUtils.isBlank(emr.getEncounterId())) {
      throw new BusinessException("就诊id不能为空");
    }
    if (StringUtils.isBlank(emr.getPatientId())) {
      throw new BusinessException("患者id不能为空");
    }
    QueryWrapper<Emr> qw = new QueryWrapper<>();
    qw.lambda().eq(Emr::getEncounterId, emr.getEncounterId());
    Emr exist = getOne(qw, false);
    if (exist == null) {
      emr.setId(null);
      emr.setStatus(ClinicConstants.EMR_DRAFT);
      if (emr.getVersionNo() == null) {
        emr.setVersionNo(1);
      }
      save(emr);
      return emr;
    }
    if (!ClinicConstants.EMR_DRAFT.equals(exist.getStatus())) {
      throw new BusinessException("病历已提交，不可修改");
    }
    emr.setId(exist.getId());
    emr.setStatus(ClinicConstants.EMR_DRAFT);
    emr.setVersionNo(exist.getVersionNo());
    emr.setSubmitTime(null);
    updateById(emr);
    return getById(exist.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void submit(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("病历id不能为空");
    }
    Emr emr = getById(id);
    if (emr == null) {
      throw new BusinessException("病历不存在");
    }
    if (!ClinicConstants.EMR_DRAFT.equals(emr.getStatus())) {
      throw new BusinessException("仅草稿病历可提交");
    }
    emr.setStatus(ClinicConstants.EMR_SUBMITTED);
    emr.setSubmitTime(LocalDateTime.now());
    updateById(emr);
  }

  @Override
  public Emr getByEncounter(String encounterId) {
    if (StringUtils.isBlank(encounterId)) {
      throw new BusinessException("就诊id不能为空");
    }
    QueryWrapper<Emr> qw = new QueryWrapper<>();
    qw.lambda().eq(Emr::getEncounterId, encounterId);
    return getOne(qw, false);
  }

  @Override
  public List<Emr> historyByPatient(String patientId) {
    if (StringUtils.isBlank(patientId)) {
      throw new BusinessException("患者id不能为空");
    }
    QueryWrapper<Emr> qw = new QueryWrapper<>();
    qw.lambda().eq(Emr::getPatientId, patientId).orderByDesc(Emr::getCreateTime);
    return list(qw);
  }
}
