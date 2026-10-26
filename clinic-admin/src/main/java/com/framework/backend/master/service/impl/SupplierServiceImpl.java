package com.framework.backend.master.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.master.entity.Supplier;
import com.framework.backend.master.mapper.SupplierMapper;
import com.framework.backend.master.service.SupplierService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier>
    implements SupplierService {

  @Override
  public void add(Supplier supplier) {
    if (supplier == null || StringUtils.isBlank(supplier.getSupplierName())) {
      throw new BusinessException("供应商名称不能为空");
    }
    if (StringUtils.isBlank(supplier.getSupplierCode())) {
      throw new BusinessException("供应商编码不能为空");
    }
    checkCodeUnique(supplier.getSupplierCode(), null);
    if (StringUtils.isBlank(supplier.getStatus())) {
      supplier.setStatus(ClinicConstants.STATUS_ACTIVE);
    }
    save(supplier);
  }

  @Override
  public void updateSupplier(Supplier supplier) {
    if (supplier == null || StringUtils.isBlank(supplier.getId())) {
      throw new BusinessException("供应商id不能为空");
    }
    Supplier exist = getById(supplier.getId());
    if (exist == null) {
      throw new BusinessException("供应商不存在");
    }
    if (StringUtils.isNotBlank(supplier.getSupplierCode())) {
      checkCodeUnique(supplier.getSupplierCode(), supplier.getId());
    }
    updateById(supplier);
  }

  @Override
  public MyPage<Supplier> pageList(Supplier supplier) {
    QueryWrapper<Supplier> qw = new QueryWrapper<>();
    if (supplier != null && StringUtils.isNotBlank(supplier.getKeyword())) {
      String kw = supplier.getKeyword().trim();
      qw.lambda()
          .and(
              w ->
                  w.like(Supplier::getSupplierName, kw)
                      .or()
                      .like(Supplier::getSupplierCode, kw)
                      .or()
                      .like(Supplier::getContactMobile, kw));
    }
    if (supplier != null && StringUtils.isNotBlank(supplier.getStatus())) {
      qw.lambda().eq(Supplier::getStatus, supplier.getStatus());
    }
    qw.lambda().orderByDesc(Supplier::getCreateTime);
    long current =
        supplier != null && supplier.getCurrentPage() != null ? supplier.getCurrentPage() : 1L;
    long size = supplier != null && supplier.getPageSize() != null ? supplier.getPageSize() : 10L;
    return baseMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  public Supplier getOne(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("供应商id不能为空");
    }
    Supplier supplier = getById(id);
    if (supplier == null) {
      throw new BusinessException("供应商不存在");
    }
    return supplier;
  }

  @Override
  public List<Supplier> listActive() {
    QueryWrapper<Supplier> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(Supplier::getStatus, ClinicConstants.STATUS_ACTIVE)
        .orderByAsc(Supplier::getSupplierName);
    return list(qw);
  }

  @Override
  public void delete(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("供应商id不能为空");
    }
    Supplier exist = getById(id);
    if (exist == null) {
      throw new BusinessException("供应商不存在");
    }
    Supplier update = new Supplier();
    update.setId(id);
    update.setStatus(ClinicConstants.STATUS_DISABLED);
    updateById(update);
  }

  private void checkCodeUnique(String code, String excludeId) {
    QueryWrapper<Supplier> qw = new QueryWrapper<>();
    qw.lambda().eq(Supplier::getSupplierCode, code);
    if (StringUtils.isNotBlank(excludeId)) {
      qw.lambda().ne(Supplier::getId, excludeId);
    }
    if (count(qw) > 0) {
      throw new BusinessException("供应商编码已存在");
    }
  }
}
