package com.framework.backend.appointment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_appointment")
@Schema(description = "预约")
public class Appointment extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("appointment_no")
  private String appointmentNo;

  @TableField("patient_id")
  private String patientId;

  @TableField("patient_name")
  private String patientName;

  @TableField("patient_mobile")
  private String patientMobile;

  @TableField("slot_id")
  private String slotId;

  @TableField("schedule_date")
  private LocalDate scheduleDate;

  @TableField("dept_id")
  private String deptId;

  @TableField("dept_name")
  private String deptName;

  @TableField("doctor_id")
  private String doctorId;

  @TableField("doctor_name")
  private String doctorName;

  @TableField("time_period")
  private String timePeriod;

  @TableField("start_time")
  private String startTime;

  @TableField("end_time")
  private String endTime;

  @TableField("status")
  private String status;

  @TableField("source")
  private String source;

  @TableField("remark")
  private String remark;

  @TableField("checkin_time")
  private LocalDateTime checkinTime;

  @TableField(exist = false)
  private String keyword;
}
