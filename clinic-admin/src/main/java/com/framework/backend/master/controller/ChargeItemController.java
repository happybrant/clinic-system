package com.framework.backend.master.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.master.entity.ChargeItem;
import com.framework.backend.master.service.ChargeItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/chargeItem")
@Tag(name = "ChargeItemController", description = "收费项目")
public class ChargeItemController {

  @Autowired private ChargeItemService chargeItemService;

  @Log(value = "新增收费项目", module = "主数据")
  @Operation(description = "新增收费项目")
  @PostMapping("/add")
  public void add(@RequestBody ChargeItem chargeItem) {
    chargeItemService.add(chargeItem);
  }

  @Log(value = "更新收费项目", module = "主数据")
  @Operation(description = "更新收费项目")
  @PostMapping("/update")
  public void update(@RequestBody ChargeItem chargeItem) {
    chargeItemService.updateChargeItem(chargeItem);
  }

  @Operation(description = "收费项目分页")
  @PostMapping("/pageList")
  public MyPage<ChargeItem> pageList(@RequestBody ChargeItem chargeItem) {
    return chargeItemService.pageList(chargeItem);
  }

  @Operation(description = "收费项目详情")
  @GetMapping("/getOne/{id}")
  public ChargeItem getOne(@PathVariable String id) {
    return chargeItemService.getOne(id);
  }

  @Operation(description = "启用收费项目")
  @GetMapping("/listActive")
  public List<ChargeItem> listActive() {
    return chargeItemService.listActive();
  }

  @Log(value = "停用收费项目", module = "主数据")
  @Operation(description = "停用收费项目")
  @PostMapping("/delete/{id}")
  public void delete(@PathVariable String id) {
    chargeItemService.delete(id);
  }
}
