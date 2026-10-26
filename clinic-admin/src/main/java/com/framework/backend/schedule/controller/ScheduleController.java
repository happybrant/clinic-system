package com.framework.backend.schedule.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.schedule.entity.ScheduleSlot;
import com.framework.backend.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/schedule")
@Tag(name = "ScheduleController", description = "排班号源")
public class ScheduleController {

  @Autowired private ScheduleService scheduleService;

  @Log(value = "新增号源", module = "排班管理")
  @Operation(description = "新增号源")
  @PostMapping("/add")
  public void add(@RequestBody ScheduleSlot slot) {
    scheduleService.add(slot);
  }

  @Log(value = "更新号源", module = "排班管理")
  @Operation(description = "更新号源")
  @PostMapping("/update")
  public void update(@RequestBody ScheduleSlot slot) {
    scheduleService.updateSlot(slot);
  }

  @Operation(description = "号源分页")
  @PostMapping("/pageList")
  public MyPage<ScheduleSlot> pageList(@RequestBody ScheduleSlot slot) {
    return scheduleService.pageList(slot);
  }

  @Log(value = "关闭号源", module = "排班管理")
  @Operation(description = "关闭号源")
  @PostMapping("/close/{id}")
  public void close(@PathVariable String id) {
    scheduleService.close(id);
  }

  @Log(value = "开放号源", module = "排班管理")
  @Operation(description = "开放号源")
  @PostMapping("/open/{id}")
  public void open(@PathVariable String id) {
    scheduleService.open(id);
  }

  @Operation(description = "可预约号源")
  @GetMapping("/available")
  public List<ScheduleSlot> available(
      @RequestParam LocalDate date, @RequestParam(required = false) String doctorId) {
    return scheduleService.available(date, doctorId);
  }
}
