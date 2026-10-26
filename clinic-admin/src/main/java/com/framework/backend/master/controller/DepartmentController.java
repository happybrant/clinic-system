package com.framework.backend.master.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.master.entity.Department;
import com.framework.backend.master.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/department")
@Tag(name = "DepartmentController", description = "科室管理")
public class DepartmentController {

  @Autowired private DepartmentService departmentService;

  @Log(value = "新增科室", module = "主数据")
  @Operation(description = "新增科室")
  @PostMapping("/add")
  public void add(@RequestBody Department department) {
    departmentService.add(department);
  }

  @Log(value = "更新科室", module = "主数据")
  @Operation(description = "更新科室")
  @PostMapping("/update")
  public void update(@RequestBody Department department) {
    departmentService.updateDepartment(department);
  }

  @Operation(description = "科室分页")
  @PostMapping("/pageList")
  public MyPage<Department> pageList(@RequestBody Department department) {
    return departmentService.pageList(department);
  }

  @Operation(description = "科室详情")
  @GetMapping("/getOne/{id}")
  public Department getOne(@PathVariable String id) {
    return departmentService.getOne(id);
  }

  @Operation(description = "启用科室列表")
  @GetMapping("/listActive")
  public List<Department> listActive() {
    return departmentService.listActive();
  }

  @Log(value = "停用科室", module = "主数据")
  @Operation(description = "停用科室")
  @PostMapping("/delete/{id}")
  public void delete(@PathVariable String id) {
    departmentService.delete(id);
  }
}
