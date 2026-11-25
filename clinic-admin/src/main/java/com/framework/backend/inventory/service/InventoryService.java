package com.framework.backend.inventory.service;

import com.framework.backend.common.MyPage;
import com.framework.backend.inventory.entity.InoutBound;
import com.framework.backend.inventory.entity.StockBatch;
import com.framework.backend.inventory.entity.Stocktake;
import com.framework.backend.pharmacy.entity.DispenseItem;
import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {

  MyPage<StockBatch> stockPageList(StockBatch query);

  List<StockBatch> expireWarn();

  InoutBound saveInbound(InoutBound bound);

  void postInbound(String id);

  MyPage<InoutBound> inboundPageList(InoutBound query);

  /** 出入库流水（可按类型/状态/关键字筛选） */
  MyPage<InoutBound> boundPageList(InoutBound query);

  InoutBound getInboundDetail(String id);

  Stocktake saveStocktake(Stocktake stocktake);

  void postStocktake(String id);

  MyPage<Stocktake> stocktakePageList(Stocktake query);

  Stocktake getStocktakeDetail(String id);

  /** 发药 FEFO 扣库存，返回拆批后的发药明细 */
  List<DispenseItem> deductForDispense(
      String drugId,
      String drugName,
      String unit,
      String rxItemId,
      BigDecimal quantity,
      String dispenseId);

  /** 按发药明细生成一张已过账出库单 */
  void postDispenseOutbound(String dispenseId, List<DispenseItem> items);
}
