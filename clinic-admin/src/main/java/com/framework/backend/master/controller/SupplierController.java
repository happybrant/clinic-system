package com.framework.backend.master.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.master.entity.Supplier;
import com.framework.backend.master.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/supplier")
@Tag(name = "SupplierController", description = "供应商")
public class SupplierController {

  @Autowired private SupplierService supplierService;

  @Log(value = "新增供应商", module = "主数据")
  @Operation(description = "新增供应商")
  @PostMapping("/add")
  public void add(@RequestBody Supplier supplier) {
    supplierService.add(supplier);
  }

  @Log(value = "更新供应商", module = "主数据")
  @Operation(description = "更新供应商")
  @PostMapping("/update")
  public void update(@RequestBody Supplier supplier) {
    supplierService.updateSupplier(supplier);
  }

  @Operation(description = "供应商分页")
  @PostMapping("/pageList")
  public MyPage<Supplier> pageList(@RequestBody Supplier supplier) {
    return supplierService.pageList(supplier);
  }

  @Operation(description = "供应商详情")
  @GetMapping("/getOne/{id}")
  public Supplier getOne(@PathVariable String id) {
    return supplierService.getOne(id);
  }

  @Operation(description = "启用供应商")
  @GetMapping("/listActive")
  public List<Supplier> listActive() {
    return supplierService.listActive();
  }

  @Log(value = "停用供应商", module = "主数据")
  @Operation(description = "停用供应商")
  @PostMapping("/delete/{id}")
  public void delete(@PathVariable String id) {
    supplierService.delete(id);
  }
}
