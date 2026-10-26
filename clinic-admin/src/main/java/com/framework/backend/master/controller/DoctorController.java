package com.framework.backend.master.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.master.entity.Doctor;
import com.framework.backend.master.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/doctor")
@Tag(name = "DoctorController", description = "医生管理")
public class DoctorController {

  @Autowired private DoctorService doctorService;

  @Log(value = "新增医生", module = "主数据")
  @Operation(description = "新增医生")
  @PostMapping("/add")
  public void add(@RequestBody Doctor doctor) {
    doctorService.add(doctor);
  }

  @Log(value = "更新医生", module = "主数据")
  @Operation(description = "更新医生")
  @PostMapping("/update")
  public void update(@RequestBody Doctor doctor) {
    doctorService.updateDoctor(doctor);
  }

  @Operation(description = "医生分页")
  @PostMapping("/pageList")
  public MyPage<Doctor> pageList(@RequestBody Doctor doctor) {
    return doctorService.pageList(doctor);
  }

  @Operation(description = "医生详情")
  @GetMapping("/getOne/{id}")
  public Doctor getOne(@PathVariable String id) {
    return doctorService.getOne(id);
  }

  @Operation(description = "启用医生列表")
  @GetMapping("/listActive")
  public List<Doctor> listActive(@RequestParam(required = false) String deptId) {
    return doctorService.listActive(deptId);
  }

  @Log(value = "停用医生", module = "主数据")
  @Operation(description = "停用医生")
  @PostMapping("/delete/{id}")
  public void delete(@PathVariable String id) {
    doctorService.delete(id);
  }
}
