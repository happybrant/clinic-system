package com.framework.backend.prescription.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.SerialNoService;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.prescription.entity.Prescription;
import com.framework.backend.prescription.entity.PrescriptionItem;
import com.framework.backend.prescription.mapper.PrescriptionItemMapper;
import com.framework.backend.prescription.mapper.PrescriptionMapper;
import com.framework.backend.prescription.service.PrescriptionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription>
    implements PrescriptionService {

  @Autowired private SerialNoService serialNoService;
  @Autowired private PrescriptionItemMapper prescriptionItemMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Prescription saveDraft(Prescription prescription) {
    if (prescription == null) {
      throw new BusinessException("处方不能为空");
    }
    if (StringUtils.isBlank(prescription.getEncounterId())) {
      throw new BusinessException("就诊id不能为空");
    }
    if (StringUtils.isBlank(prescription.getPatientId())) {
      throw new BusinessException("患者id不能为空");
    }
    List<PrescriptionItem> items = prescription.getItems();
    if (items == null || items.isEmpty()) {
      throw new BusinessException("处方明细不能为空");
    }
    BigDecimal total = validateAndCalcTotal(items);

    boolean isNew = StringUtils.isBlank(prescription.getId());
    if (isNew) {
      prescription.setPrescriptionNo(serialNoService.next(ClinicConstants.PREFIX_PRESCRIPTION));
      prescription.setStatus(ClinicConstants.RX_DRAFT);
      prescription.setTotalAmount(total);
      if (StringUtils.isBlank(prescription.getRxType())) {
        prescription.setRxType("western");
      }
      save(prescription);
    } else {
      Prescription exist = getById(prescription.getId());
      if (exist == null) {
        throw new BusinessException("处方不存在");
      }
      if (!ClinicConstants.RX_DRAFT.equals(exist.getStatus())) {
        throw new BusinessException("仅草稿处方可修改");
      }
      prescription.setPrescriptionNo(exist.getPrescriptionNo());
      prescription.setStatus(ClinicConstants.RX_DRAFT);
      prescription.setTotalAmount(total);
      updateById(prescription);
      QueryWrapper<PrescriptionItem> del = new QueryWrapper<>();
      del.lambda().eq(PrescriptionItem::getPrescriptionId, prescription.getId());
      prescriptionItemMapper.delete(del);
    }

    int sort = 0;
    for (PrescriptionItem item : items) {
      item.setId(null);
      item.setPrescriptionId(prescription.getId());
      if (item.getSortNo() == null) {
        item.setSortNo(sort++);
      }
      prescriptionItemMapper.insert(item);
    }
    return getDetail(prescription.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void submit(String id) {
    Prescription rx = requireRx(id);
    if (!ClinicConstants.RX_DRAFT.equals(rx.getStatus())) {
      throw new BusinessException("仅草稿处方可提交");
    }
    rx.setStatus(ClinicConstants.RX_SUBMITTED);
    rx.setSubmitTime(LocalDateTime.now());
    updateById(rx);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void voidRx(String id) {
    Prescription rx = requireRx(id);
    String status = rx.getStatus();
    if (!ClinicConstants.RX_DRAFT.equals(status)
        && !ClinicConstants.RX_SUBMITTED.equals(status)) {
      throw new BusinessException("仅草稿或已提交处方可作废");
    }
    rx.setStatus(ClinicConstants.RX_VOIDED);
    updateById(rx);
  }

  @Override
  public MyPage<Prescription> pageList(Prescription query) {
    QueryWrapper<Prescription> qw = new QueryWrapper<>();
    if (query != null) {
      if (StringUtils.isNotBlank(query.getStatus())) {
        qw.lambda().eq(Prescription::getStatus, query.getStatus());
      }
      if (StringUtils.isNotBlank(query.getPatientId())) {
        qw.lambda().eq(Prescription::getPatientId, query.getPatientId());
      }
      if (StringUtils.isNotBlank(query.getEncounterId())) {
        qw.lambda().eq(Prescription::getEncounterId, query.getEncounterId());
      }
      if (StringUtils.isNotBlank(query.getKeyword())) {
        String kw = query.getKeyword().trim();
        qw.lambda()
            .and(
                w ->
                    w.like(Prescription::getPrescriptionNo, kw)
                        .or()
                        .like(Prescription::getPatientName, kw));
      }
    }
    qw.lambda().orderByDesc(Prescription::getCreateTime);
    long current = query != null && query.getCurrentPage() != null ? query.getCurrentPage() : 1L;
    long size = query != null && query.getPageSize() != null ? query.getPageSize() : 10L;
    return baseMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  public Prescription getDetail(String id) {
    Prescription rx = requireRx(id);
    QueryWrapper<PrescriptionItem> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(PrescriptionItem::getPrescriptionId, id)
        .orderByAsc(PrescriptionItem::getSortNo);
    rx.setItems(prescriptionItemMapper.selectList(qw));
    return rx;
  }

  private Prescription requireRx(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("处方id不能为空");
    }
    Prescription rx = getById(id);
    if (rx == null) {
      throw new BusinessException("处方不存在");
    }
    return rx;
  }

  private BigDecimal validateAndCalcTotal(List<PrescriptionItem> items) {
    BigDecimal total = BigDecimal.ZERO;
    int idx = 0;
    for (PrescriptionItem item : items) {
      idx++;
      if (StringUtils.isBlank(item.getDrugId())) {
        throw new BusinessException("第" + idx + "行药品不能为空");
      }
      if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException("第" + idx + "行数量必须大于0");
      }
      if (item.getUnitPrice() == null) {
        throw new BusinessException("第" + idx + "行单价不能为空");
      }
      BigDecimal amount =
          item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
      item.setAmount(amount);
      total = total.add(amount);
    }
    return total.setScale(2, RoundingMode.HALF_UP);
  }
}
