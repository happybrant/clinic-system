package com.framework.backend.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.charge.entity.ChargeSheet;
import com.framework.backend.charge.entity.ChargeSheetItem;
import com.framework.backend.charge.entity.Payment;
import com.framework.backend.charge.mapper.ChargeSheetItemMapper;
import com.framework.backend.charge.mapper.ChargeSheetMapper;
import com.framework.backend.charge.mapper.PaymentMapper;
import com.framework.backend.charge.service.ChargeService;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.SerialNoService;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.encounter.entity.Encounter;
import com.framework.backend.encounter.service.EncounterService;
import com.framework.backend.pharmacy.entity.Dispense;
import com.framework.backend.pharmacy.mapper.DispenseMapper;
import com.framework.backend.prescription.entity.Prescription;
import com.framework.backend.prescription.entity.PrescriptionItem;
import com.framework.backend.prescription.service.PrescriptionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChargeServiceImpl extends ServiceImpl<ChargeSheetMapper, ChargeSheet>
    implements ChargeService {

  @Autowired private SerialNoService serialNoService;
  @Autowired private ChargeSheetItemMapper chargeSheetItemMapper;
  @Autowired private PaymentMapper paymentMapper;
  @Autowired private PrescriptionService prescriptionService;
  @Autowired private EncounterService encounterService;
  @Autowired private DispenseMapper dispenseMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ChargeSheet createFromPrescription(String prescriptionId) {
    if (StringUtils.isBlank(prescriptionId)) {
      throw new BusinessException("处方id不能为空");
    }
    Prescription rx = prescriptionService.getDetail(prescriptionId);
    if (!ClinicConstants.RX_SUBMITTED.equals(rx.getStatus())) {
      throw new BusinessException("仅已提交处方可生成收费单");
    }

    QueryWrapper<ChargeSheet> existQw = new QueryWrapper<>();
    existQw
        .lambda()
        .eq(ChargeSheet::getPrescriptionId, prescriptionId)
        .in(
            ChargeSheet::getStatus,
            ClinicConstants.CHARGE_UNPAID,
            ClinicConstants.CHARGE_PAID,
            ClinicConstants.CHARGE_PARTIAL_REFUND);
    if (count(existQw) > 0) {
      throw new BusinessException("该处方已存在有效收费单");
    }

    List<ChargeSheetItem> items = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    if (StringUtils.isNotBlank(rx.getEncounterId())) {
      Encounter encounter = encounterService.getDetail(rx.getEncounterId());
      if (encounter.getRegisterFee() != null
          && encounter.getRegisterFee().compareTo(BigDecimal.ZERO) > 0
          && !hasRegisterFeeCharged(encounter.getId())) {
        ChargeSheetItem reg = new ChargeSheetItem();
        reg.setItemType("register");
        reg.setItemCode("REG");
        reg.setItemName("挂号费");
        reg.setQuantity(BigDecimal.ONE);
        reg.setUnitPrice(encounter.getRegisterFee());
        reg.setAmount(encounter.getRegisterFee().setScale(2, RoundingMode.HALF_UP));
        reg.setRefId(encounter.getId());
        items.add(reg);
        total = total.add(reg.getAmount());
      }
    }

    List<PrescriptionItem> rxItems = rx.getItems();
    if (rxItems == null || rxItems.isEmpty()) {
      throw new BusinessException("处方明细为空，无法收费");
    }
    for (PrescriptionItem rxItem : rxItems) {
      ChargeSheetItem item = new ChargeSheetItem();
      item.setItemType("drug");
      item.setItemId(rxItem.getDrugId());
      item.setItemCode(rxItem.getDrugCode());
      item.setItemName(rxItem.getDrugName() + (StringUtils.isBlank(rxItem.getSpec()) ? "" : " " + rxItem.getSpec()));
      item.setQuantity(rxItem.getQuantity() == null ? BigDecimal.ONE : rxItem.getQuantity());
      item.setUnitPrice(rxItem.getUnitPrice() == null ? BigDecimal.ZERO : rxItem.getUnitPrice());
      BigDecimal amount =
          rxItem.getAmount() != null
              ? rxItem.getAmount()
              : item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
      item.setAmount(amount);
      item.setRefId(rxItem.getId());
      items.add(item);
      total = total.add(amount);
    }

    ChargeSheet sheet = new ChargeSheet();
    sheet.setChargeNo(serialNoService.next(ClinicConstants.PREFIX_CHARGE));
    sheet.setEncounterId(rx.getEncounterId());
    sheet.setPatientId(rx.getPatientId());
    sheet.setPatientName(rx.getPatientName());
    sheet.setPrescriptionId(rx.getId());
    sheet.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    sheet.setPaidAmount(BigDecimal.ZERO);
    sheet.setRefundAmount(BigDecimal.ZERO);
    sheet.setStatus(ClinicConstants.CHARGE_UNPAID);
    sheet.setRemark("处方 " + rx.getPrescriptionNo());
    save(sheet);

    for (ChargeSheetItem item : items) {
      item.setId(null);
      item.setChargeId(sheet.getId());
      chargeSheetItemMapper.insert(item);
    }
    return getDetail(sheet.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ChargeSheet pay(ChargeSheet request) {
    if (request == null || StringUtils.isBlank(request.getId())) {
      throw new BusinessException("收费单id不能为空");
    }
    if (StringUtils.isBlank(request.getPayMethod())) {
      throw new BusinessException("请选择支付方式");
    }
    ChargeSheet sheet = requireSheet(request.getId());
    if (!ClinicConstants.CHARGE_UNPAID.equals(sheet.getStatus())) {
      throw new BusinessException("仅未支付收费单可收银");
    }

    LocalDateTime now = LocalDateTime.now();
    sheet.setStatus(ClinicConstants.CHARGE_PAID);
    sheet.setPayMethod(request.getPayMethod());
    sheet.setPayTime(now);
    sheet.setPaidAmount(sheet.getTotalAmount());
    sheet.setCashierId(request.getCashierId());
    sheet.setCashierName(request.getCashierName());
    if (StringUtils.isNotBlank(request.getRemark())) {
      sheet.setRemark(request.getRemark());
    }
    updateById(sheet);

    Payment payment = new Payment();
    payment.setChargeId(sheet.getId());
    payment.setPayMethod(request.getPayMethod());
    payment.setAmount(sheet.getTotalAmount());
    payment.setPayType("pay");
    payment.setPayTime(now);
    paymentMapper.insert(payment);

    if (StringUtils.isNotBlank(sheet.getPrescriptionId())) {
      Prescription rx = prescriptionService.getById(sheet.getPrescriptionId());
      if (rx != null && ClinicConstants.RX_SUBMITTED.equals(rx.getStatus())) {
        rx.setStatus(ClinicConstants.RX_CHARGED);
        prescriptionService.updateById(rx);
        createPendingDispense(rx);
      }
    }
    return getDetail(sheet.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cancel(String id) {
    ChargeSheet sheet = requireSheet(id);
    if (!ClinicConstants.CHARGE_UNPAID.equals(sheet.getStatus())) {
      throw new BusinessException("仅未支付收费单可取消");
    }
    sheet.setStatus(ClinicConstants.CHARGE_CANCELLED);
    updateById(sheet);
  }

  @Override
  public MyPage<ChargeSheet> pageList(ChargeSheet query) {
    QueryWrapper<ChargeSheet> qw = new QueryWrapper<>();
    if (query != null) {
      if (StringUtils.isNotBlank(query.getStatus())) {
        qw.lambda().eq(ChargeSheet::getStatus, query.getStatus());
      }
      if (StringUtils.isNotBlank(query.getPatientId())) {
        qw.lambda().eq(ChargeSheet::getPatientId, query.getPatientId());
      }
      if (StringUtils.isNotBlank(query.getEncounterId())) {
        qw.lambda().eq(ChargeSheet::getEncounterId, query.getEncounterId());
      }
      if (StringUtils.isNotBlank(query.getKeyword())) {
        String kw = query.getKeyword().trim();
        qw.lambda()
            .and(
                w ->
                    w.like(ChargeSheet::getChargeNo, kw)
                        .or()
                        .like(ChargeSheet::getPatientName, kw));
      }
    }
    qw.lambda().orderByDesc(ChargeSheet::getCreateTime);
    long current = query != null && query.getCurrentPage() != null ? query.getCurrentPage() : 1L;
    long size = query != null && query.getPageSize() != null ? query.getPageSize() : 10L;
    return baseMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  public ChargeSheet getDetail(String id) {
    ChargeSheet sheet = requireSheet(id);
    QueryWrapper<ChargeSheetItem> itemQw = new QueryWrapper<>();
    itemQw.lambda().eq(ChargeSheetItem::getChargeId, id).orderByAsc(ChargeSheetItem::getCreateTime);
    sheet.setItems(chargeSheetItemMapper.selectList(itemQw));

    QueryWrapper<Payment> payQw = new QueryWrapper<>();
    payQw.lambda().eq(Payment::getChargeId, id).orderByAsc(Payment::getPayTime);
    sheet.setPayments(paymentMapper.selectList(payQw));

    if (StringUtils.isNotBlank(sheet.getPrescriptionId())) {
      Prescription rx = prescriptionService.getById(sheet.getPrescriptionId());
      if (rx != null) {
        sheet.setPrescriptionNo(rx.getPrescriptionNo());
      }
    }
    return sheet;
  }

  private boolean hasRegisterFeeCharged(String encounterId) {
    QueryWrapper<ChargeSheet> sheetQw = new QueryWrapper<>();
    sheetQw
        .lambda()
        .eq(ChargeSheet::getEncounterId, encounterId)
        .in(
            ChargeSheet::getStatus,
            ClinicConstants.CHARGE_UNPAID,
            ClinicConstants.CHARGE_PAID,
            ClinicConstants.CHARGE_PARTIAL_REFUND);
    List<ChargeSheet> sheets = list(sheetQw);
    for (ChargeSheet sheet : sheets) {
      QueryWrapper<ChargeSheetItem> itemQw = new QueryWrapper<>();
      itemQw
          .lambda()
          .eq(ChargeSheetItem::getChargeId, sheet.getId())
          .eq(ChargeSheetItem::getItemType, "register");
      if (chargeSheetItemMapper.selectCount(itemQw) > 0) {
        return true;
      }
    }
    return false;
  }

  private void createPendingDispense(Prescription rx) {
    QueryWrapper<Dispense> qw = new QueryWrapper<>();
    qw.lambda().eq(Dispense::getPrescriptionId, rx.getId());
    if (dispenseMapper.selectCount(qw) > 0) {
      return;
    }
    Dispense dispense = new Dispense();
    dispense.setDispenseNo(serialNoService.next(ClinicConstants.PREFIX_DISPENSE));
    dispense.setPrescriptionId(rx.getId());
    dispense.setPrescriptionNo(rx.getPrescriptionNo());
    dispense.setPatientId(rx.getPatientId());
    dispense.setPatientName(rx.getPatientName());
    dispense.setStatus(ClinicConstants.DISPENSE_PENDING);
    dispenseMapper.insert(dispense);
  }

  private ChargeSheet requireSheet(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("收费单id不能为空");
    }
    ChargeSheet sheet = getById(id);
    if (sheet == null) {
      throw new BusinessException("收费单不存在");
    }
    return sheet;
  }
}
