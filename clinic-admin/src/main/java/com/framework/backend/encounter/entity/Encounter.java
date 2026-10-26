package com.framework.backend.encounter.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 就诊实体（收费模块读取挂号费等字段）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_encounter")
@Schema(description = "就诊")
public class Encounter extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("encounter_no")
  private String encounterNo;

  @TableField("patient_id")
  private String patientId;

  @TableField("patient_name")
  private String patientName;

  @TableField("patient_gender")
  private String patientGender;

  @TableField("patient_age")
  private Integer patientAge;

  @TableField("patient_mobile")
  private String patientMobile;

  @TableField("dept_id")
  private String deptId;

  @TableField("dept_name")
  private String deptName;

  @TableField("doctor_id")
  private String doctorId;

  @TableField("doctor_name")
  private String doctorName;

  @TableField("appointment_id")
  private String appointmentId;

  @TableField("queue_no")
  private Integer queueNo;

  @TableField("status")
  private String status;

  @TableField("register_type")
  private String registerType;

  @TableField("register_fee")
  private BigDecimal registerFee;

  @TableField("start_time")
  private LocalDateTime startTime;

  @TableField("end_time")
  private LocalDateTime endTime;

  @TableField("remark")
  private String remark;
}
