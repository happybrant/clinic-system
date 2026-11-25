package com.framework.backend.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.SerialNoService;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.inventory.entity.InoutBound;
import com.framework.backend.inventory.entity.InoutBoundItem;
import com.framework.backend.inventory.entity.StockBatch;
import com.framework.backend.inventory.entity.Stocktake;
import com.framework.backend.inventory.entity.StocktakeItem;
import com.framework.backend.inventory.mapper.InoutBoundItemMapper;
import com.framework.backend.inventory.mapper.InoutBoundMapper;
import com.framework.backend.inventory.mapper.StockBatchMapper;
import com.framework.backend.inventory.mapper.StocktakeItemMapper;
import com.framework.backend.inventory.mapper.StocktakeMapper;
import com.framework.backend.inventory.service.InventoryService;
import com.framework.backend.master.entity.Drug;
import com.framework.backend.master.service.DrugService;
import com.framework.backend.pharmacy.entity.DispenseItem;
import com.framework.backend.system.entity.ClinicConfig;
import com.framework.backend.system.service.ClinicConfigService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

  @Autowired private SerialNoService serialNoService;
  @Autowired private StockBatchMapper stockBatchMapper;
  @Autowired private InoutBoundMapper inoutBoundMapper;
  @Autowired private InoutBoundItemMapper inoutBoundItemMapper;
  @Autowired private StocktakeMapper stocktakeMapper;
  @Autowired private StocktakeItemMapper stocktakeItemMapper;
  @Autowired private DrugService drugService;
  @Autowired private ClinicConfigService clinicConfigService;

  @Override
  public MyPage<StockBatch> stockPageList(StockBatch query) {
    QueryWrapper<StockBatch> qw = new QueryWrapper<>();
    if (query != null) {
      if (StringUtils.isNotBlank(query.getDrugId())) {
        qw.lambda().eq(StockBatch::getDrugId, query.getDrugId());
      }
      if (StringUtils.isNotBlank(query.getStatus())) {
        qw.lambda().eq(StockBatch::getStatus, query.getStatus());
      }
      if (StringUtils.isNotBlank(query.getBatchNo())) {
        qw.lambda().like(StockBatch::getBatchNo, query.getBatchNo().trim());
      }
      if (StringUtils.isNotBlank(query.getDrugName())) {
        String kw = query.getDrugName().trim();
        qw.lambda()
            .and(
                w ->
                    w.like(StockBatch::getDrugName, kw)
                        .or()
                        .like(StockBatch::getDrugCode, kw)
                        .or()
                        .like(StockBatch::getBatchNo, kw));
      }
    }
    qw.lambda()
        .orderByAsc(StockBatch::getExpireDate)
        .orderByAsc(StockBatch::getCreateTime);
    long current = query != null && query.getCurrentPage() != null ? query.getCurrentPage() : 1L;
    long size = query != null && query.getPageSize() != null ? query.getPageSize() : 10L;
    return stockBatchMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  public List<StockBatch> expireWarn() {
    int days = 90;
    ClinicConfig cfg = clinicConfigService.getByKey(ClinicConstants.CFG_EXPIRE_WARN_DAYS);
    if (cfg != null && StringUtils.isNotBlank(cfg.getConfigValue())) {
      try {
        days = Integer.parseInt(cfg.getConfigValue().trim());
      } catch (NumberFormatException ignored) {
        // keep default
      }
    }
    LocalDate deadline = LocalDate.now().plusDays(days);
    QueryWrapper<StockBatch> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(StockBatch::getStatus, ClinicConstants.STATUS_ACTIVE)
        .gt(StockBatch::getQuantity, BigDecimal.ZERO)
        .isNotNull(StockBatch::getExpireDate)
        .le(StockBatch::getExpireDate, deadline)
        .orderByAsc(StockBatch::getExpireDate);
    return stockBatchMapper.selectList(qw);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public InoutBound saveInbound(InoutBound bound) {
    if (bound == null) {
      throw new BusinessException("入库单不能为空");
    }
    List<InoutBoundItem> items = bound.getItems();
    if (items == null || items.isEmpty()) {
      throw new BusinessException("入库明细不能为空");
    }
    validateInboundItems(items);
    BigDecimal total = calcBoundTotal(items);

    boolean isNew = StringUtils.isBlank(bound.getId());
    if (isNew) {
      bound.setBoundNo(serialNoService.next(ClinicConstants.PREFIX_INBOUND));
      bound.setBoundType(ClinicConstants.BOUND_PURCHASE_IN);
      bound.setStatus(ClinicConstants.BOUND_DRAFT);
      bound.setTotalAmount(total);
      inoutBoundMapper.insert(bound);
    } else {
      InoutBound exist = inoutBoundMapper.selectById(bound.getId());
      if (exist == null) {
        throw new BusinessException("入库单不存在");
      }
      if (!ClinicConstants.BOUND_DRAFT.equals(exist.getStatus())) {
        throw new BusinessException("仅草稿入库单可修改");
      }
      bound.setBoundNo(exist.getBoundNo());
      bound.setBoundType(ClinicConstants.BOUND_PURCHASE_IN);
      bound.setStatus(ClinicConstants.BOUND_DRAFT);
      bound.setTotalAmount(total);
      inoutBoundMapper.updateById(bound);
      QueryWrapper<InoutBoundItem> del = new QueryWrapper<>();
      del.lambda().eq(InoutBoundItem::getBoundId, bound.getId());
      inoutBoundItemMapper.delete(del);
    }

    for (InoutBoundItem item : items) {
      item.setId(null);
      item.setBoundId(bound.getId());
      inoutBoundItemMapper.insert(item);
    }
    return getInboundDetail(bound.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void postInbound(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("入库单id不能为空");
    }
    InoutBound bound = inoutBoundMapper.selectById(id);
    if (bound == null) {
      throw new BusinessException("入库单不存在");
    }
    if (!ClinicConstants.BOUND_DRAFT.equals(bound.getStatus())) {
      throw new BusinessException("仅草稿入库单可过账");
    }
    QueryWrapper<InoutBoundItem> qw = new QueryWrapper<>();
    qw.lambda().eq(InoutBoundItem::getBoundId, id);
    List<InoutBoundItem> items = inoutBoundItemMapper.selectList(qw);
    if (items == null || items.isEmpty()) {
      throw new BusinessException("入库明细为空");
    }

    for (InoutBoundItem item : items) {
      upsertBatchOnInbound(item, bound.getSupplierId());
    }

    bound.setStatus(ClinicConstants.BOUND_POSTED);
    bound.setPostTime(LocalDateTime.now());
    inoutBoundMapper.updateById(bound);
  }

  @Override
  public MyPage<InoutBound> inboundPageList(InoutBound query) {
    InoutBound q = query == null ? new InoutBound() : query;
    q.setBoundType(ClinicConstants.BOUND_PURCHASE_IN);
    return boundPageList(q);
  }

  @Override
  public MyPage<InoutBound> boundPageList(InoutBound query) {
    QueryWrapper<InoutBound> qw = new QueryWrapper<>();
    if (query != null) {
      if (StringUtils.isNotBlank(query.getBoundType())) {
        qw.lambda().eq(InoutBound::getBoundType, query.getBoundType());
      }
      if (StringUtils.isNotBlank(query.getStatus())) {
        qw.lambda().eq(InoutBound::getStatus, query.getStatus());
      }
      if (StringUtils.isNotBlank(query.getBoundNo())) {
        qw.lambda().like(InoutBound::getBoundNo, query.getBoundNo().trim());
      }
      if (StringUtils.isNotBlank(query.getKeyword())) {
        String kw = query.getKeyword().trim();
        qw.lambda()
            .and(
                w ->
                    w.like(InoutBound::getBoundNo, kw)
                        .or()
                        .like(InoutBound::getSupplierName, kw)
                        .or()
                        .like(InoutBound::getRemark, kw));
      }
    }
    qw.lambda().orderByDesc(InoutBound::getCreateTime);
    long current = query != null && query.getCurrentPage() != null ? query.getCurrentPage() : 1L;
    long size = query != null && query.getPageSize() != null ? query.getPageSize() : 10L;
    return inoutBoundMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  public InoutBound getInboundDetail(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("入库单id不能为空");
    }
    InoutBound bound = inoutBoundMapper.selectById(id);
    if (bound == null) {
      throw new BusinessException("入库单不存在");
    }
    QueryWrapper<InoutBoundItem> qw = new QueryWrapper<>();
    qw.lambda().eq(InoutBoundItem::getBoundId, id);
    bound.setItems(inoutBoundItemMapper.selectList(qw));
    return bound;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Stocktake saveStocktake(Stocktake stocktake) {
    if (stocktake == null) {
      throw new BusinessException("盘点单不能为空");
    }
    List<StocktakeItem> items = stocktake.getItems();
    if (items == null || items.isEmpty()) {
      throw new BusinessException("盘点明细不能为空");
    }
    validateStocktakeItems(items);

    boolean isNew = StringUtils.isBlank(stocktake.getId());
    if (isNew) {
      stocktake.setStocktakeNo(serialNoService.next(ClinicConstants.PREFIX_STOCKTAKE));
      stocktake.setStatus(ClinicConstants.BOUND_DRAFT);
      stocktakeMapper.insert(stocktake);
    } else {
      Stocktake exist = requireStocktake(stocktake.getId());
      if (!ClinicConstants.BOUND_DRAFT.equals(exist.getStatus())) {
        throw new BusinessException("仅草稿盘点单可修改");
      }
      stocktake.setStocktakeNo(exist.getStocktakeNo());
      stocktake.setStatus(ClinicConstants.BOUND_DRAFT);
      stocktakeMapper.updateById(stocktake);
      QueryWrapper<StocktakeItem> del = new QueryWrapper<>();
      del.lambda().eq(StocktakeItem::getStocktakeId, stocktake.getId());
      stocktakeItemMapper.delete(del);
    }

    for (StocktakeItem item : items) {
      item.setId(null);
      item.setStocktakeId(stocktake.getId());
      BigDecimal book = item.getBookQty() == null ? BigDecimal.ZERO : item.getBookQty();
      BigDecimal actual = item.getActualQty() == null ? BigDecimal.ZERO : item.getActualQty();
      item.setDiffQty(actual.subtract(book).setScale(2, RoundingMode.HALF_UP));
      stocktakeItemMapper.insert(item);
    }
    return getStocktakeDetail(stocktake.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void postStocktake(String id) {
    Stocktake stocktake = requireStocktake(id);
    if (!ClinicConstants.BOUND_DRAFT.equals(stocktake.getStatus())) {
      throw new BusinessException("仅草稿盘点单可过账");
    }
    QueryWrapper<StocktakeItem> qw = new QueryWrapper<>();
    qw.lambda().eq(StocktakeItem::getStocktakeId, id);
    List<StocktakeItem> items = stocktakeItemMapper.selectList(qw);
    if (items == null || items.isEmpty()) {
      throw new BusinessException("盘点明细为空");
    }

    InoutBound bound = new InoutBound();
    bound.setBoundNo(serialNoService.next("ADJ"));
    bound.setBoundType(ClinicConstants.BOUND_STOCKTAKE);
    bound.setStatus(ClinicConstants.BOUND_POSTED);
    bound.setBizRefId(stocktake.getId());
    bound.setPostTime(LocalDateTime.now());
    bound.setRemark("盘点调整 " + stocktake.getStocktakeNo());
    BigDecimal total = BigDecimal.ZERO;
    inoutBoundMapper.insert(bound);

    for (StocktakeItem item : items) {
      if (StringUtils.isBlank(item.getBatchId())) {
        throw new BusinessException("盘点明细缺少批次");
      }
      StockBatch batch = stockBatchMapper.selectById(item.getBatchId());
      if (batch == null) {
        throw new BusinessException("批次不存在：" + item.getBatchNo());
      }
      BigDecimal book = item.getBookQty() == null ? BigDecimal.ZERO : item.getBookQty();
      BigDecimal actual = item.getActualQty() == null ? BigDecimal.ZERO : item.getActualQty();
      BigDecimal diff = actual.subtract(book).setScale(2, RoundingMode.HALF_UP);
      item.setDiffQty(diff);
      stocktakeItemMapper.updateById(item);

      if (diff.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      BigDecimal current = batch.getQuantity() == null ? BigDecimal.ZERO : batch.getQuantity();
      BigDecimal next = current.add(diff);
      if (next.compareTo(BigDecimal.ZERO) < 0 && !isAllowNegativeStock()) {
        throw new BusinessException(
            "药品【"
                + batch.getDrugName()
                + "】批次 "
                + batch.getBatchNo()
                + " 盘点后库存为负，已禁止");
      }
      batch.setQuantity(next);
      stockBatchMapper.updateById(batch);

      InoutBoundItem boundItem = new InoutBoundItem();
      boundItem.setBoundId(bound.getId());
      boundItem.setDrugId(batch.getDrugId());
      boundItem.setDrugCode(batch.getDrugCode());
      boundItem.setDrugName(batch.getDrugName());
      boundItem.setBatchNo(batch.getBatchNo());
      boundItem.setExpireDate(batch.getExpireDate());
      boundItem.setQuantity(diff);
      boundItem.setUnitPrice(batch.getCostPrice() == null ? BigDecimal.ZERO : batch.getCostPrice());
      boundItem.setAmount(
          boundItem
              .getUnitPrice()
              .multiply(diff.abs())
              .setScale(2, RoundingMode.HALF_UP));
      boundItem.setUnit(null);
      inoutBoundItemMapper.insert(boundItem);
      total = total.add(boundItem.getAmount() == null ? BigDecimal.ZERO : boundItem.getAmount());
    }

    bound.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    inoutBoundMapper.updateById(bound);

    stocktake.setStatus(ClinicConstants.BOUND_POSTED);
    stocktake.setPostTime(LocalDateTime.now());
    stocktakeMapper.updateById(stocktake);
  }

  @Override
  public MyPage<Stocktake> stocktakePageList(Stocktake query) {
    QueryWrapper<Stocktake> qw = new QueryWrapper<>();
    if (query != null) {
      if (StringUtils.isNotBlank(query.getStatus())) {
        qw.lambda().eq(Stocktake::getStatus, query.getStatus());
      }
      if (StringUtils.isNotBlank(query.getKeyword())) {
        String kw = query.getKeyword().trim();
        qw.lambda()
            .and(
                w ->
                    w.like(Stocktake::getStocktakeNo, kw)
                        .or()
                        .like(Stocktake::getOperatorName, kw)
                        .or()
                        .like(Stocktake::getRemark, kw));
      }
    }
    qw.lambda().orderByDesc(Stocktake::getCreateTime);
    long current = query != null && query.getCurrentPage() != null ? query.getCurrentPage() : 1L;
    long size = query != null && query.getPageSize() != null ? query.getPageSize() : 10L;
    return stocktakeMapper.selectPage(new MyPage<>(current, size), qw);
  }

  @Override
  public Stocktake getStocktakeDetail(String id) {
    Stocktake stocktake = requireStocktake(id);
    QueryWrapper<StocktakeItem> qw = new QueryWrapper<>();
    qw.lambda().eq(StocktakeItem::getStocktakeId, id).orderByAsc(StocktakeItem::getCreateTime);
    stocktake.setItems(stocktakeItemMapper.selectList(qw));
    return stocktake;
  }

  private Stocktake requireStocktake(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("盘点单id不能为空");
    }
    Stocktake stocktake = stocktakeMapper.selectById(id);
    if (stocktake == null) {
      throw new BusinessException("盘点单不存在");
    }
    return stocktake;
  }

  private void validateStocktakeItems(List<StocktakeItem> items) {
    int idx = 0;
    for (StocktakeItem item : items) {
      idx++;
      if (StringUtils.isBlank(item.getBatchId())) {
        throw new BusinessException("第" + idx + "行请选择库存批次");
      }
      if (item.getActualQty() == null || item.getActualQty().compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessException("第" + idx + "行实盘数量不能为空且不能为负");
      }
      StockBatch batch = stockBatchMapper.selectById(item.getBatchId());
      if (batch == null) {
        throw new BusinessException("第" + idx + "行批次不存在");
      }
      item.setDrugId(batch.getDrugId());
      item.setDrugName(batch.getDrugName());
      item.setBatchNo(batch.getBatchNo());
      item.setBookQty(batch.getQuantity() == null ? BigDecimal.ZERO : batch.getQuantity());
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public List<DispenseItem> deductForDispense(
      String drugId,
      String drugName,
      String unit,
      String rxItemId,
      BigDecimal quantity,
      String dispenseId) {
    if (StringUtils.isBlank(drugId)) {
      throw new BusinessException("药品不能为空");
    }
    if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException("发药数量必须大于0");
    }

    QueryWrapper<StockBatch> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(StockBatch::getDrugId, drugId)
        .eq(StockBatch::getStatus, ClinicConstants.STATUS_ACTIVE)
        .gt(StockBatch::getQuantity, BigDecimal.ZERO)
        .and(
            w ->
                w.isNull(StockBatch::getExpireDate)
                    .or()
                    .ge(StockBatch::getExpireDate, LocalDate.now()))
        .orderByAsc(StockBatch::getExpireDate)
        .orderByAsc(StockBatch::getCreateTime);
    List<StockBatch> batches = stockBatchMapper.selectList(qw);

    BigDecimal remain = quantity;
    BigDecimal available =
        batches.stream()
            .map(b -> b.getQuantity() == null ? BigDecimal.ZERO : b.getQuantity())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    boolean allowNegative = isAllowNegativeStock();
    if (available.compareTo(quantity) < 0 && !allowNegative) {
      throw new BusinessException(
          "药品【"
              + (StringUtils.defaultIfBlank(drugName, drugId))
              + "】库存不足，需要 "
              + quantity.stripTrailingZeros().toPlainString()
              + "，可用 "
              + available.stripTrailingZeros().toPlainString());
    }

    List<DispenseItem> result = new ArrayList<>();
    for (StockBatch batch : batches) {
      if (remain.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      BigDecimal batchQty = batch.getQuantity() == null ? BigDecimal.ZERO : batch.getQuantity();
      if (batchQty.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal take = batchQty.min(remain);
      batch.setQuantity(batchQty.subtract(take));
      stockBatchMapper.updateById(batch);

      DispenseItem di = new DispenseItem();
      di.setDispenseId(dispenseId);
      di.setRxItemId(rxItemId);
      di.setDrugId(drugId);
      di.setDrugName(StringUtils.defaultIfBlank(drugName, batch.getDrugName()));
      di.setBatchId(batch.getId());
      di.setBatchNo(batch.getBatchNo());
      di.setQuantity(take);
      di.setUnit(unit);
      result.add(di);

      remain = remain.subtract(take);
    }

    if (remain.compareTo(BigDecimal.ZERO) > 0) {
      // 允许负库存：无批次虚扣，仅保留发药明细
      DispenseItem di = new DispenseItem();
      di.setDispenseId(dispenseId);
      di.setRxItemId(rxItemId);
      di.setDrugId(drugId);
      di.setDrugName(drugName);
      di.setBatchNo("NEG");
      di.setQuantity(remain);
      di.setUnit(unit);
      result.add(di);
    }
    return result;
  }

  /** 按发药明细生成一张已过账出库单 */
  @Transactional(rollbackFor = Exception.class)
  public void postDispenseOutbound(String dispenseId, List<DispenseItem> items) {
    if (items == null || items.isEmpty()) {
      return;
    }
    InoutBound bound = new InoutBound();
    bound.setBoundNo(serialNoService.next("OUT"));
    bound.setBoundType(ClinicConstants.BOUND_DISPENSE_OUT);
    bound.setStatus(ClinicConstants.BOUND_POSTED);
    bound.setBizRefId(dispenseId);
    bound.setPostTime(LocalDateTime.now());
    bound.setRemark("发药出库");
    BigDecimal total = BigDecimal.ZERO;
    inoutBoundMapper.insert(bound);
    for (DispenseItem item : items) {
      InoutBoundItem boundItem = new InoutBoundItem();
      boundItem.setBoundId(bound.getId());
      boundItem.setDrugId(item.getDrugId());
      boundItem.setDrugName(item.getDrugName());
      boundItem.setBatchNo(item.getBatchNo());
      boundItem.setQuantity(item.getQuantity());
      boundItem.setUnitPrice(BigDecimal.ZERO);
      boundItem.setAmount(BigDecimal.ZERO);
      boundItem.setUnit(item.getUnit());
      if (StringUtils.isNotBlank(item.getBatchId())) {
        StockBatch batch = stockBatchMapper.selectById(item.getBatchId());
        if (batch != null) {
          boundItem.setDrugCode(batch.getDrugCode());
          boundItem.setExpireDate(batch.getExpireDate());
          boundItem.setUnitPrice(batch.getCostPrice() == null ? BigDecimal.ZERO : batch.getCostPrice());
          boundItem.setAmount(
              boundItem
                  .getUnitPrice()
                  .multiply(item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity())
                  .setScale(2, RoundingMode.HALF_UP));
        }
      }
      total = total.add(boundItem.getAmount() == null ? BigDecimal.ZERO : boundItem.getAmount());
      inoutBoundItemMapper.insert(boundItem);
    }
    bound.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    inoutBoundMapper.updateById(bound);
  }

  private boolean isAllowNegativeStock() {
    ClinicConfig cfg = clinicConfigService.getByKey(ClinicConstants.CFG_ALLOW_NEGATIVE_STOCK);
    if (cfg == null || StringUtils.isBlank(cfg.getConfigValue())) {
      return false;
    }
    String v = cfg.getConfigValue().trim();
    return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
  }

  private void upsertBatchOnInbound(InoutBoundItem item, String supplierId) {
    QueryWrapper<StockBatch> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(StockBatch::getDrugId, item.getDrugId())
        .eq(StockBatch::getBatchNo, item.getBatchNo());
    StockBatch batch = stockBatchMapper.selectOne(qw);
    if (batch == null) {
      batch = new StockBatch();
      batch.setDrugId(item.getDrugId());
      batch.setDrugCode(item.getDrugCode());
      batch.setDrugName(item.getDrugName());
      batch.setBatchNo(item.getBatchNo());
      batch.setExpireDate(item.getExpireDate());
      batch.setQuantity(item.getQuantity());
      batch.setLockedQty(BigDecimal.ZERO);
      batch.setCostPrice(item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice());
      batch.setSupplierId(supplierId);
      batch.setWarehouse("default");
      batch.setStatus(ClinicConstants.STATUS_ACTIVE);
      stockBatchMapper.insert(batch);
    } else {
      BigDecimal qty = batch.getQuantity() == null ? BigDecimal.ZERO : batch.getQuantity();
      batch.setQuantity(qty.add(item.getQuantity()));
      if (item.getExpireDate() != null) {
        batch.setExpireDate(item.getExpireDate());
      }
      if (item.getUnitPrice() != null) {
        batch.setCostPrice(item.getUnitPrice());
      }
      if (StringUtils.isNotBlank(supplierId)) {
        batch.setSupplierId(supplierId);
      }
      batch.setStatus(ClinicConstants.STATUS_ACTIVE);
      stockBatchMapper.updateById(batch);
    }
  }

  private void validateInboundItems(List<InoutBoundItem> items) {
    int idx = 0;
    for (InoutBoundItem item : items) {
      idx++;
      if (StringUtils.isBlank(item.getDrugId())) {
        throw new BusinessException("第" + idx + "行药品不能为空");
      }
      if (StringUtils.isBlank(item.getBatchNo())) {
        throw new BusinessException("第" + idx + "行批次号不能为空");
      }
      if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException("第" + idx + "行数量必须大于0");
      }
      Drug drug = drugService.getOne(item.getDrugId());
      if (StringUtils.isBlank(item.getDrugName())) {
        item.setDrugName(drug.getDrugName());
      }
      if (StringUtils.isBlank(item.getDrugCode())) {
        item.setDrugCode(drug.getDrugCode());
      }
      if (StringUtils.isBlank(item.getUnit())) {
        item.setUnit(drug.getUnit());
      }
      if (item.getUnitPrice() == null) {
        item.setUnitPrice(drug.getCostPrice() == null ? BigDecimal.ZERO : drug.getCostPrice());
      }
      item.setAmount(
          item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
    }
  }

  private BigDecimal calcBoundTotal(List<InoutBoundItem> items) {
    BigDecimal total = BigDecimal.ZERO;
    for (InoutBoundItem item : items) {
      total = total.add(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
    }
    return total.setScale(2, RoundingMode.HALF_UP);
  }
}
