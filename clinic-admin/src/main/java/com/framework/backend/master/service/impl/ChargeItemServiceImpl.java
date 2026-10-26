package com.framework.backend.master.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.master.entity.ChargeItem;
import com.framework.backend.master.mapper.ChargeItemMapper;
import com.framework.backend.master.service.ChargeItemService;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ChargeItemServiceImpl extends ServiceImpl<ChargeItemMapper, ChargeItem>
    implements ChargeItemService {

  @Override
  public void add(ChargeItem chargeItem) {
    if (chargeItem == null || StringUtils.isBlank(chargeItem.getItemName())) {
      throw new BusinessException("收费项目名称不能为空");
    }
    if (StringUtils.isBlank(chargeItem.getItemCode())) {
      throw new BusinessException("收费项目编码不能为空");
    }
    checkCodeUnique(chargeItem.getItemCode(), null);
    if (StringUtils.isBlank(chargeItem.getStatus())) {
      chargeItem.setStatus(ClinicConstants.STATUS_ACTIVE);
    }
    if (chargeItem.getPrice() == null) {
      chargeItem.setPrice(BigDecimal.ZERO);
    }
    save(chargeItem);
  }

  @Override
  public void updateChargeItem(ChargeItem chargeItem) {
    if (chargeItem == null || StringUtils.isBlank(chargeItem.getId())) {
      throw new BusinessException("收费项目id不能为空");
    }
    ChargeItem exist = getById(chargeItem.getId());
    if (exist == null) {
      throw new BusinessException("收费项目不存在");
    }
    if (StringUtils.isNotBlank(chargeItem.getItemCode())) {
      checkCodeUnique(chargeItem.getItemCode(), chargeItem.getId());
    }
    updateById(chargeItem);
  }

  @Override
  public MyPage<ChargeItem> pageList(ChargeItem chargeItem) {
    QueryWrapper<ChargeItem> qw = new QueryWrapper<>();
    if (chargeItem != null && StringUtils.isNotBlank(chargeItem.getKeyword())) {
      String kw = chargeItem.getKeyword().trim();
      qw.lambda()
          .and(w -> w.like(ChargeItem::getItemName, kw).or().like(ChargeItem::getItemCode, kw));
    }
    if (chargeItem != null && StringUtils.isNotBlank(chargeItem.getItemType())) {
      qw.lambda().eq(ChargeItem::getItemType, chargeItem.getItemType());
    }
    if (chargeItem != null && StringUtils.isNotBlank(chargeItem.getStatus())) {
      qw.lambda().eq(ChargeItem::getStatus, chargeItem.getStatus());
    }
    qw.lambda().orderByDesc(ChargeItem::getCreateTime);
    long current =
        chargeItem != null && chargeItem.getCurrentPage() != null
            ? chargeItem.getCurrentPage()
            : 1L;
    long size =
        chargeItem != null && chargeItem.getPageSize() != null ? chargeItem.getPageSize() : 10L;
    MyPage<ChargeItem> page = new MyPage<>(current, size);
    return baseMapper.selectPage(page, qw);
  }

  @Override
  public ChargeItem getOne(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("收费项目id不能为空");
    }
    ChargeItem chargeItem = getById(id);
    if (chargeItem == null) {
      throw new BusinessException("收费项目不存在");
    }
    return chargeItem;
  }

  @Override
  public List<ChargeItem> listActive() {
    QueryWrapper<ChargeItem> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(ChargeItem::getStatus, ClinicConstants.STATUS_ACTIVE)
        .orderByAsc(ChargeItem::getItemName);
    return list(qw);
  }

  @Override
  public void delete(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("收费项目id不能为空");
    }
    ChargeItem exist = getById(id);
    if (exist == null) {
      throw new BusinessException("收费项目不存在");
    }
    ChargeItem update = new ChargeItem();
    update.setId(id);
    update.setStatus(ClinicConstants.STATUS_DISABLED);
    updateById(update);
  }

  private void checkCodeUnique(String code, String excludeId) {
    QueryWrapper<ChargeItem> qw = new QueryWrapper<>();
    qw.lambda().eq(ChargeItem::getItemCode, code);
    if (StringUtils.isNotBlank(excludeId)) {
      qw.lambda().ne(ChargeItem::getId, excludeId);
    }
    if (count(qw) > 0) {
      throw new BusinessException("收费项目编码已存在");
    }
  }
}
