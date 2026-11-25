package com.framework.backend.pharmacy.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.pharmacy.entity.Dispense;
import com.framework.backend.pharmacy.service.PharmacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/pharmacy")
@Tag(name = "PharmacyController", description = "药房发药")
public class PharmacyController {

  @Autowired private PharmacyService pharmacyService;

  @Operation(description = "发药单分页")
  @PostMapping("/pageList")
  public MyPage<Dispense> pageList(@RequestBody Dispense query) {
    return pharmacyService.pageList(query);
  }

  @Operation(description = "发药单详情")
  @GetMapping("/getDetail/{id}")
  public Dispense getDetail(@PathVariable String id) {
    return pharmacyService.getDetail(id);
  }

  @Log(value = "审方通过", module = "药房管理")
  @Operation(description = "审方通过")
  @PostMapping("/approve")
  public void approve(@RequestBody Dispense request) {
    pharmacyService.approve(request);
  }

  @Log(value = "审方驳回/缺药", module = "药房管理")
  @Operation(description = "审方驳回或缺药标记")
  @PostMapping("/reject")
  public void reject(@RequestBody Dispense request) {
    pharmacyService.reject(request);
  }

  @Log(value = "发药", module = "药房管理")
  @Operation(description = "确认发药并扣库存")
  @PostMapping("/dispense")
  public Dispense dispense(@RequestBody Dispense request) {
    return pharmacyService.dispense(request);
  }
}
