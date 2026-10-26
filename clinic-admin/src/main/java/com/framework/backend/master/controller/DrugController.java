package com.framework.backend.master.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.master.entity.Drug;
import com.framework.backend.master.service.DrugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/drug")
@Tag(name = "DrugController", description = "药品字典")
public class DrugController {

  @Autowired private DrugService drugService;

  @Log(value = "新增药品", module = "主数据")
  @Operation(description = "新增药品")
  @PostMapping("/add")
  public void add(@RequestBody Drug drug) {
    drugService.add(drug);
  }

  @Log(value = "更新药品", module = "主数据")
  @Operation(description = "更新药品")
  @PostMapping("/update")
  public void update(@RequestBody Drug drug) {
    drugService.updateDrug(drug);
  }

  @Operation(description = "药品分页")
  @PostMapping("/pageList")
  public MyPage<Drug> pageList(@RequestBody Drug drug) {
    return drugService.pageList(drug);
  }

  @Operation(description = "药品详情")
  @GetMapping("/getOne/{id}")
  public Drug getOne(@PathVariable String id) {
    return drugService.getOne(id);
  }

  @Log(value = "停用药品", module = "主数据")
  @Operation(description = "停用药品")
  @PostMapping("/delete/{id}")
  public void delete(@PathVariable String id) {
    drugService.delete(id);
  }
}
