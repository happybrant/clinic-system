package com.framework.backend.patient.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.patient.entity.Patient;
import com.framework.backend.patient.entity.PatientLog;
import com.framework.backend.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/patient")
@Tag(name = "PatientController", description = "患者管理")
public class PatientController {

  @Autowired private PatientService patientService;

  @Log(value = "新增患者", module = "患者管理")
  @Operation(description = "新增患者")
  @PostMapping("/add")
  public void add(@RequestBody Patient patient) {
    patientService.add(patient);
  }

  @Log(value = "更新患者", module = "患者管理")
  @Operation(description = "更新患者")
  @PostMapping("/update")
  public void update(@RequestBody Patient patient) {
    patientService.updatePatient(patient);
  }

  @Operation(description = "患者分页")
  @PostMapping("/pageList")
  public MyPage<Patient> pageList(@RequestBody Patient patient) {
    return patientService.pageList(patient);
  }

  @Operation(description = "患者详情")
  @GetMapping("/getDetail/{id}")
  public Patient getDetail(@PathVariable String id) {
    return patientService.getDetail(id);
  }

  @Log(value = "添加患者日志", module = "患者管理")
  @Operation(description = "添加患者日志")
  @PostMapping("/addLog")
  public void addLog(@RequestBody PatientLog log) {
    patientService.addLog(log);
  }
}
