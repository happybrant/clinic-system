package com.framework.backend.prescription.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_prescription")
@Schema(description = "处方")
public class Prescription extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("prescription_no")
  private String prescriptionNo;

  @TableField("encounter_id")
  private String encounterId;

  @TableField("patient_id")
  private String patientId;

  @TableField("patient_name")
  private String patientName;

  @TableField("doctor_id")
  private String doctorId;

  @TableField("doctor_name")
  private String doctorName;

  @TableField("dept_name")
  private String deptName;

  @TableField("rx_type")
  private String rxType;

  @TableField("diagnosis")
  private String diagnosis;

  @TableField("status")
  private String status;

  @TableField("total_amount")
  private BigDecimal totalAmount;

  @TableField("remark")
  private String remark;

  @TableField("submit_time")
  private LocalDateTime submitTime;

  @TableField(exist = false)
  private List<PrescriptionItem> items;

  @TableField(exist = false)
  private String keyword;
}
