package com.framework.backend.appointment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.appointment.entity.Appointment;
import com.framework.backend.common.MyPage;

public interface AppointmentService extends IService<Appointment> {

  Appointment book(Appointment appointment);

  void cancel(Appointment appointment);

  Appointment checkIn(Appointment appointment);

  MyPage<Appointment> pageList(Appointment appointment);

  Appointment getDetail(String id);
}
