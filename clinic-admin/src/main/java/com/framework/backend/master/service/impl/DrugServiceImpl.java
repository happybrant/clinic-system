package com.framework.backend.master.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.master.entity.Drug;
import com.framework.backend.master.mapper.DrugMapper;
import com.framework.backend.master.service.DrugService;
import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug> implements DrugService {

  @Override
  public void add(Drug drug) {
    if (drug == null || StringUtils.isBlank(drug.getDrugName())) {
      throw new BusinessException("药品名称不能为空");
    }
    if (StringUtils.isBlank(drug.getDrugCode())) {
      throw new BusinessException("药品编码不能为空");
    }
    checkCodeUnique(drug.getDrugCode(), null);
    if (StringUtils.isBlank(drug.getStatus())) {
      drug.setStatus(ClinicConstants.STATUS_ACTIVE);
    }
    if (drug.getSalePrice() == null) {
      drug.setSalePrice(BigDecimal.ZERO);
    }
    if (drug.getCostPrice() == null) {
      drug.setCostPrice(BigDecimal.ZERO);
    }
    if (drug.getPrescriptionFlag() == null) {
      drug.setPrescriptionFlag(true);
    }
    save(drug);
  }

  @Override
  public void updateDrug(Drug drug) {
    if (drug == null || StringUtils.isBlank(drug.getId())) {
      throw new BusinessException("药品id不能为空");
    }
    Drug exist = getById(drug.getId());
    if (exist == null) {
      throw new BusinessException("药品不存在");
    }
    if (StringUtils.isNotBlank(drug.getDrugCode())) {
      checkCodeUnique(drug.getDrugCode(), drug.getId());
    }
    updateById(drug);
  }

  @Override
  public MyPage<Drug> pageList(Drug drug) {
    QueryWrapper<Drug> qw = new QueryWrapper<>();
    if (drug != null && StringUtils.isNotBlank(drug.getKeyword())) {
      String kw = drug.getKeyword().trim();
      qw.lambda()
          .and(
              w ->
                  w.like(Drug::getDrugName, kw)
                      .or()
                      .like(Drug::getDrugCode, kw)
                      .or()
                      .like(Drug::getGenericName, kw));
    }
    if (drug != null && StringUtils.isNotBlank(drug.getDrugType())) {
      qw.lambda().eq(Drug::getDrugType, drug.getDrugType());
    }
    if (drug != null && StringUtils.isNotBlank(drug.getStatus())) {
      qw.lambda().eq(Drug::getStatus, drug.getStatus());
    }
    qw.lambda().orderByDesc(Drug::getCreateTime);
    long current = drug != null && drug.getCurrentPage() != null ? drug.getCurrentPage() : 1L;
    long size = drug != null && drug.getPageSize() != null ? drug.getPageSize() : 10L;
    MyPage<Drug> page = new MyPage<>(current, size);
    return baseMapper.selectPage(page, qw);
  }

  @Override
  public Drug getOne(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("药品id不能为空");
    }
    Drug drug = getById(id);
    if (drug == null) {
      throw new BusinessException("药品不存在");
    }
    return drug;
  }

  @Override
  public void delete(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("药品id不能为空");
    }
    Drug exist = getById(id);
    if (exist == null) {
      throw new BusinessException("药品不存在");
    }
    Drug update = new Drug();
    update.setId(id);
    update.setStatus(ClinicConstants.STATUS_DISABLED);
    updateById(update);
  }

  private void checkCodeUnique(String code, String excludeId) {
    QueryWrapper<Drug> qw = new QueryWrapper<>();
    qw.lambda().eq(Drug::getDrugCode, code);
    if (StringUtils.isNotBlank(excludeId)) {
      qw.lambda().ne(Drug::getId, excludeId);
    }
    if (count(qw) > 0) {
      throw new BusinessException("药品编码已存在");
    }
  }
}
