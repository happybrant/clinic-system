package com.framework.backend.encounter.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.encounter.entity.Encounter;
import com.framework.backend.encounter.service.EncounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/encounter")
@Tag(name = "EncounterController", description = "就诊候诊")
public class EncounterController {

  @Autowired private EncounterService encounterService;

  @Log(value = "现场挂号", module = "就诊管理")
  @Operation(description = "现场挂号进入候诊")
  @PostMapping("/walkIn")
  public Encounter walkIn(@RequestBody Encounter encounter) {
    return encounterService.registerWalkIn(encounter);
  }

  @Operation(description = "就诊分页/队列")
  @PostMapping("/pageList")
  public MyPage<Encounter> pageList(@RequestBody Encounter encounter) {
    return encounterService.pageList(encounter);
  }

  @Operation(description = "就诊详情")
  @GetMapping("/getDetail/{id}")
  public Encounter getDetail(@PathVariable String id) {
    return encounterService.getDetail(id);
  }

  @Log(value = "叫号下一位", module = "就诊管理")
  @Operation(description = "叫号下一位")
  @PostMapping("/callNext")
  public Encounter callNext(@RequestBody Encounter encounter) {
    return encounterService.callNext(encounter);
  }

  @Log(value = "开始接诊", module = "就诊管理")
  @Operation(description = "开始接诊")
  @PostMapping("/start/{id}")
  public void start(@PathVariable String id) {
    encounterService.start(id);
  }

  @Log(value = "过号", module = "就诊管理")
  @Operation(description = "过号")
  @PostMapping("/skip/{id}")
  public void skip(@PathVariable String id) {
    encounterService.skip(id);
  }

  @Log(value = "完成就诊", module = "就诊管理")
  @Operation(description = "完成就诊")
  @PostMapping("/finish/{id}")
  public void finish(@PathVariable String id) {
    encounterService.finish(id);
  }
}
