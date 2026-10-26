package com.framework.backend.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_dispense")
@Schema(description = "发药单")
public class Dispense extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("dispense_no")
  private String dispenseNo;

  @TableField("prescription_id")
  private String prescriptionId;

  @TableField("prescription_no")
  private String prescriptionNo;

  @TableField("patient_id")
  private String patientId;

  @TableField("patient_name")
  private String patientName;

  @TableField("status")
  private String status;

  @TableField("pharmacist_id")
  private String pharmacistId;

  @TableField("pharmacist_name")
  private String pharmacistName;

  @TableField("dispense_time")
  private LocalDateTime dispenseTime;

  @TableField("reject_reason")
  private String rejectReason;

  @TableField("remark")
  private String remark;

  @TableField(exist = false)
  private List<DispenseItem> items;

  @TableField(exist = false)
  private String keyword;
}
