package com.framework.backend.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.inventory.service.InventoryService;
import com.framework.backend.pharmacy.entity.Dispense;
import com.framework.backend.pharmacy.entity.DispenseItem;
import com.framework.backend.pharmacy.mapper.DispenseItemMapper;
import com.framework.backend.pharmacy.mapper.DispenseMapper;
import com.framework.backend.pharmacy.service.PharmacyService;
import com.framework.backend.prescription.entity.Prescription;
import com.framework.backend.prescription.entity.PrescriptionItem;
import com.framework.backend.prescription.service.PrescriptionService;
import com.framework.backend.system.entity.ClinicConfig;
import com.framework.backend.system.service.ClinicConfigService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PharmacyServiceImpl extends ServiceImpl<DispenseMapper, Dispense>
    implements PharmacyService {

  @Autowired private DispenseItemMapper dispenseItemMapper;
  @Autowired private PrescriptionService prescriptionService;
  @Autowired private InventoryService inventoryService;
  @Autowired private ClinicConfigService clinicConfigService;

  @Override
  public MyPage<Dispense> pageList(Dispense query) {
    QueryWrapper<Dispense> qw = new QueryWrapper<>();
    if (query != null) {
      if (StringUtils.isNotBlank(query.getStatus())) {
        qw.lambda().eq(Dispense::getStatus, query.getStatus());
      }
      if (StringUtils.isNotBlank(query.getPatientId())) {
        qw.lambda().eq(Dispense::getPatientId, query.getPatientId());
      }
      if (StringUtils.isNotBlank(query.getKeyword())) {
        String kw = query.getKeyword().trim();
        qw.lambda()
            .and(
                w ->
                    w.like(Dispense::getDispenseNo, kw)
                        .or()
                        .like(Dispense::getPrescriptionNo, kw)
                        .or()
                        .like(Dispense::getPatientName, kw));
      }
    }
    qw.lambda().orderByDesc(Dispense::getCreateTime);
    long current = query != null && query.getCurrentPage() != null ? query.getCurrentPage() : 1L;
    long size = query != null && query.getPageSize() != null ? query.getPageSize() : 10L;
    return baseMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  public Dispense getDetail(String id) {
    Dispense dispense = requireDispense(id);
    QueryWrapper<DispenseItem> itemQw = new QueryWrapper<>();
    itemQw.lambda().eq(DispenseItem::getDispenseId, id).orderByAsc(DispenseItem::getCreateTime);
    dispense.setItems(dispenseItemMapper.selectList(itemQw));
    dispense.setAudited(StringUtils.isNotBlank(dispense.getPharmacistName()));

    if (StringUtils.isNotBlank(dispense.getPrescriptionId())) {
      Prescription rx = prescriptionService.getDetail(dispense.getPrescriptionId());
      if (rx != null) {
        dispense.setDiagnosis(rx.getDiagnosis());
        dispense.setDoctorName(rx.getDoctorName());
        if (ClinicConstants.DISPENSE_PENDING.equals(dispense.getStatus())
            && (dispense.getItems() == null || dispense.getItems().isEmpty())) {
          dispense.setPrescriptionItems(rx.getItems());
        }
      }
    }
    return dispense;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void approve(Dispense request) {
    if (request == null || StringUtils.isBlank(request.getId())) {
      throw new BusinessException("发药单id不能为空");
    }
    Dispense dispense = requireDispense(request.getId());
    if (!ClinicConstants.DISPENSE_PENDING.equals(dispense.getStatus())) {
      throw new BusinessException("仅待发药单可审方");
    }
    dispense.setPharmacistId(request.getPharmacistId());
    dispense.setPharmacistName(
        StringUtils.defaultIfBlank(request.getPharmacistName(), dispense.getPharmacistName()));
    if (StringUtils.isBlank(dispense.getPharmacistName())) {
      throw new BusinessException("请填写药师姓名");
    }
    if (StringUtils.isNotBlank(request.getRemark())) {
      dispense.setRemark(request.getRemark());
    }
    updateById(dispense);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void reject(Dispense request) {
    if (request == null || StringUtils.isBlank(request.getId())) {
      throw new BusinessException("发药单id不能为空");
    }
    if (StringUtils.isBlank(request.getRejectReason())) {
      throw new BusinessException("请填写驳回/缺药原因");
    }
    Dispense dispense = requireDispense(request.getId());
    if (!ClinicConstants.DISPENSE_PENDING.equals(dispense.getStatus())) {
      throw new BusinessException("仅待发药单可驳回");
    }
    dispense.setStatus(ClinicConstants.DISPENSE_REJECTED);
    dispense.setRejectReason(request.getRejectReason());
    dispense.setPharmacistId(request.getPharmacistId());
    if (StringUtils.isNotBlank(request.getPharmacistName())) {
      dispense.setPharmacistName(request.getPharmacistName());
    }
    updateById(dispense);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Dispense dispense(Dispense request) {
    if (request == null || StringUtils.isBlank(request.getId())) {
      throw new BusinessException("发药单id不能为空");
    }
    Dispense dispense = requireDispense(request.getId());
    if (!ClinicConstants.DISPENSE_PENDING.equals(dispense.getStatus())) {
      throw new BusinessException("仅待发药单可发药");
    }
    if (isForceAudit() && StringUtils.isBlank(dispense.getPharmacistName())) {
      throw new BusinessException("已开启强制审方，请先审方通过");
    }

    Prescription rx = prescriptionService.getDetail(dispense.getPrescriptionId());
    if (rx == null) {
      throw new BusinessException("关联处方不存在");
    }
    if (!ClinicConstants.RX_CHARGED.equals(rx.getStatus())
        && !ClinicConstants.RX_DISPENSING.equals(rx.getStatus())) {
      throw new BusinessException("处方状态不可发药：" + rx.getStatus());
    }
    List<PrescriptionItem> rxItems = rx.getItems();
    if (rxItems == null || rxItems.isEmpty()) {
      throw new BusinessException("处方明细为空");
    }

    rx.setStatus(ClinicConstants.RX_DISPENSING);
    prescriptionService.updateById(rx);

    List<DispenseItem> allItems = new ArrayList<>();
    for (PrescriptionItem rxItem : rxItems) {
      List<DispenseItem> allocated =
          inventoryService.deductForDispense(
              rxItem.getDrugId(),
              rxItem.getDrugName(),
              rxItem.getUnit(),
              rxItem.getId(),
              rxItem.getQuantity(),
              dispense.getId());
      allItems.addAll(allocated);
    }

    QueryWrapper<DispenseItem> del = new QueryWrapper<>();
    del.lambda().eq(DispenseItem::getDispenseId, dispense.getId());
    dispenseItemMapper.delete(del);
    for (DispenseItem item : allItems) {
      item.setId(null);
      item.setDispenseId(dispense.getId());
      dispenseItemMapper.insert(item);
    }
    inventoryService.postDispenseOutbound(dispense.getId(), allItems);

    LocalDateTime now = LocalDateTime.now();
    dispense.setStatus(ClinicConstants.DISPENSE_DISPENSED);
    dispense.setDispenseTime(now);
    if (StringUtils.isNotBlank(request.getPharmacistName())) {
      dispense.setPharmacistName(request.getPharmacistName());
    }
    if (StringUtils.isNotBlank(request.getPharmacistId())) {
      dispense.setPharmacistId(request.getPharmacistId());
    }
    if (StringUtils.isBlank(dispense.getPharmacistName())) {
      dispense.setPharmacistName("药师");
    }
    if (StringUtils.isNotBlank(request.getRemark())) {
      dispense.setRemark(request.getRemark());
    }
    updateById(dispense);

    rx.setStatus(ClinicConstants.RX_DISPENSED);
    prescriptionService.updateById(rx);
    return getDetail(dispense.getId());
  }

  private boolean isForceAudit() {
    ClinicConfig cfg = clinicConfigService.getByKey(ClinicConstants.CFG_FORCE_AUDIT_RX);
    if (cfg == null || StringUtils.isBlank(cfg.getConfigValue())) {
      return false;
    }
    String v = cfg.getConfigValue().trim();
    return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
  }

  private Dispense requireDispense(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("发药单id不能为空");
    }
    Dispense dispense = getById(id);
    if (dispense == null) {
      throw new BusinessException("发药单不存在");
    }
    return dispense;
  }
}
