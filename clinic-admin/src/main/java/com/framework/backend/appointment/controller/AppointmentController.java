package com.framework.backend.appointment.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.appointment.entity.Appointment;
import com.framework.backend.appointment.service.AppointmentService;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.result.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/appointment")
@Tag(name = "AppointmentController", description = "预约管理")
public class AppointmentController {

  @Autowired private AppointmentService appointmentService;

  @Log(value = "预约挂号", module = "预约管理")
  @Operation(description = "预约挂号")
  @PostMapping("/book")
  public Appointment book(@RequestBody Appointment appointment) {
    return appointmentService.book(appointment);
  }

  @Log(value = "取消预约", module = "预约管理")
  @Operation(description = "取消预约")
  @PostMapping("/cancel")
  public void cancel(@RequestBody Appointment appointment) {
    appointmentService.cancel(appointment);
  }

  @Log(value = "预约签到", module = "预约管理")
  @Operation(description = "预约签到")
  @PostMapping("/checkIn")
  public Appointment checkIn(@RequestBody Appointment appointment) {
    return appointmentService.checkIn(appointment);
  }

  @Operation(description = "预约分页")
  @PostMapping("/pageList")
  public MyPage<Appointment> pageList(@RequestBody Appointment appointment) {
    return appointmentService.pageList(appointment);
  }

  @Operation(description = "预约详情")
  @GetMapping("/getDetail/{id}")
  public Appointment getDetail(@PathVariable String id) {
    return appointmentService.getDetail(id);
  }
}
