package com.framework.backend.inventory.service;

import com.framework.backend.common.MyPage;
import com.framework.backend.inventory.entity.InoutBound;
import com.framework.backend.inventory.entity.StockBatch;
import com.framework.backend.inventory.entity.Stocktake;
import java.util.List;

public interface InventoryService {

  MyPage<StockBatch> stockPageList(StockBatch query);

  List<StockBatch> expireWarn();

  void saveInbound(InoutBound bound);

  void postInbound(String id);

  MyPage<InoutBound> inboundPageList(InoutBound query);

  InoutBound getInboundDetail(String id);

  void saveStocktake(Stocktake stocktake);

  void postStocktake(String id);

  MyPage<Stocktake> stocktakePageList(Stocktake query);
}
