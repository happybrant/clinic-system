package com.framework.backend.system.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.system.entity.ClinicConfig;
import com.framework.backend.system.service.ClinicConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/config")
@Tag(name = "ClinicConfigController", description = "诊所系统参数")
public class ClinicConfigController {

  @Autowired private ClinicConfigService clinicConfigService;

  @Operation(description = "按键查询参数")
  @GetMapping("/getByKey/{configKey}")
  public ClinicConfig getByKey(@PathVariable String configKey) {
    return clinicConfigService.getByKey(configKey);
  }

  @Log(value = "更新系统参数", module = "系统参数")
  @Operation(description = "更新参数")
  @PostMapping("/update")
  public void update(@RequestBody ClinicConfig config) {
    clinicConfigService.updateConfig(config);
  }

  @Operation(description = "参数列表")
  @GetMapping("/list")
  public List<ClinicConfig> list() {
    return clinicConfigService.listAll();
  }
}
