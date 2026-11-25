package com.framework.backend.charge.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.charge.entity.ChargeSheet;
import com.framework.backend.charge.service.ChargeService;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/charge")
@Tag(name = "ChargeController", description = "收费管理")
public class ChargeController {

  @Autowired private ChargeService chargeService;

  @Log(value = "生成收费单", module = "收费管理")
  @Operation(description = "按已提交处方生成收费单")
  @PostMapping("/createFromPrescription")
  public ChargeSheet createFromPrescription(@RequestBody Map<String, String> body) {
    return chargeService.createFromPrescription(body == null ? null : body.get("prescriptionId"));
  }

  @Log(value = "收银", module = "收费管理")
  @Operation(description = "收费单收银")
  @PostMapping("/pay")
  public ChargeSheet pay(@RequestBody ChargeSheet request) {
    return chargeService.pay(request);
  }

  @Log(value = "取消收费单", module = "收费管理")
  @Operation(description = "取消未支付收费单")
  @PostMapping("/cancel/{id}")
  public void cancel(@PathVariable String id) {
    chargeService.cancel(id);
  }

  @Operation(description = "收费单分页")
  @PostMapping("/pageList")
  public MyPage<ChargeSheet> pageList(@RequestBody ChargeSheet query) {
    return chargeService.pageList(query);
  }

  @Operation(description = "收费单详情")
  @GetMapping("/getDetail/{id}")
  public ChargeSheet getDetail(@PathVariable String id) {
    return chargeService.getDetail(id);
  }
}
