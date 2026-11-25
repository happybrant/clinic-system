package com.framework.backend.inventory.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.inventory.entity.InoutBound;
import com.framework.backend.inventory.entity.StockBatch;
import com.framework.backend.inventory.entity.Stocktake;
import com.framework.backend.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/inventory")
@Tag(name = "InventoryController", description = "进销存")
public class InventoryController {

  @Autowired private InventoryService inventoryService;

  @Operation(description = "批次库存分页")
  @PostMapping("/stock/pageList")
  public MyPage<StockBatch> stockPageList(@RequestBody StockBatch query) {
    return inventoryService.stockPageList(query);
  }

  @Operation(description = "近效期预警")
  @GetMapping("/stock/expireWarn")
  public List<StockBatch> expireWarn() {
    return inventoryService.expireWarn();
  }

  @Log(value = "保存入库单", module = "进销存")
  @Operation(description = "保存入库草稿")
  @PostMapping("/inbound/save")
  public InoutBound saveInbound(@RequestBody InoutBound bound) {
    return inventoryService.saveInbound(bound);
  }

  @Log(value = "入库过账", module = "进销存")
  @Operation(description = "入库过账增加批次库存")
  @PostMapping("/inbound/post/{id}")
  public void postInbound(@PathVariable String id) {
    inventoryService.postInbound(id);
  }

  @Operation(description = "入库单分页")
  @PostMapping("/inbound/pageList")
  public MyPage<InoutBound> inboundPageList(@RequestBody InoutBound query) {
    return inventoryService.inboundPageList(query);
  }

  @Operation(description = "出入库流水分页")
  @PostMapping("/bound/pageList")
  public MyPage<InoutBound> boundPageList(@RequestBody InoutBound query) {
    return inventoryService.boundPageList(query);
  }

  @Operation(description = "入库单详情")
  @GetMapping("/inbound/getDetail/{id}")
  public InoutBound getInboundDetail(@PathVariable String id) {
    return inventoryService.getInboundDetail(id);
  }

  @Operation(description = "出入库单据详情")
  @GetMapping("/bound/getDetail/{id}")
  public InoutBound getBoundDetail(@PathVariable String id) {
    return inventoryService.getInboundDetail(id);
  }

  @Log(value = "保存盘点单", module = "进销存")
  @Operation(description = "保存盘点草稿")
  @PostMapping("/stocktake/save")
  public Stocktake saveStocktake(@RequestBody Stocktake stocktake) {
    return inventoryService.saveStocktake(stocktake);
  }

  @Log(value = "盘点过账", module = "进销存")
  @Operation(description = "盘点过账调整库存")
  @PostMapping("/stocktake/post/{id}")
  public void postStocktake(@PathVariable String id) {
    inventoryService.postStocktake(id);
  }

  @Operation(description = "盘点单分页")
  @PostMapping("/stocktake/pageList")
  public MyPage<Stocktake> stocktakePageList(@RequestBody Stocktake query) {
    return inventoryService.stocktakePageList(query);
  }

  @Operation(description = "盘点单详情")
  @GetMapping("/stocktake/getDetail/{id}")
  public Stocktake getStocktakeDetail(@PathVariable String id) {
    return inventoryService.getStocktakeDetail(id);
  }
}
