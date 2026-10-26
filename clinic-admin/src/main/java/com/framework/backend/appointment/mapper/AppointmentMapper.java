package com.framework.backend.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.framework.backend.appointment.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {}
