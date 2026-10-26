package com.framework.backend.prescription.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.prescription.entity.Prescription;
import com.framework.backend.prescription.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/prescription")
@Tag(name = "PrescriptionController", description = "处方管理")
public class PrescriptionController {

  @Autowired private PrescriptionService prescriptionService;

  @Log(value = "保存处方草稿", module = "处方管理")
  @Operation(description = "保存处方草稿及明细")
  @PostMapping("/save")
  public Prescription save(@RequestBody Prescription prescription) {
    return prescriptionService.saveDraft(prescription);
  }

  @Log(value = "提交处方", module = "处方管理")
  @Operation(description = "提交处方")
  @PostMapping("/submit/{id}")
  public void submit(@PathVariable String id) {
    prescriptionService.submit(id);
  }

  @Log(value = "作废处方", module = "处方管理")
  @Operation(description = "作废处方")
  @PostMapping("/void/{id}")
  public void voidRx(@PathVariable String id) {
    prescriptionService.voidRx(id);
  }

  @Operation(description = "处方分页")
  @PostMapping("/pageList")
  public MyPage<Prescription> pageList(@RequestBody Prescription query) {
    return prescriptionService.pageList(query);
  }

  @Operation(description = "处方详情")
  @GetMapping("/getDetail/{id}")
  public Prescription getDetail(@PathVariable String id) {
    return prescriptionService.getDetail(id);
  }
}
