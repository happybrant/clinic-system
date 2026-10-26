package com.framework.backend.patient.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_patient_log")
@Schema(description = "患者操作日志")
public class PatientLog extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("patient_id")
  private String patientId;

  @TableField("operation")
  private String operation;

  @TableField("content")
  private String content;

  @TableField("operate_time")
  private LocalDateTime operateTime;

  @TableField("operator_name")
  private String operatorName;
}
