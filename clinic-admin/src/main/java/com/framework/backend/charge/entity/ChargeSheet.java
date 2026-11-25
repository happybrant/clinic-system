package com.framework.backend.charge.entity;

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
@TableName("clinic_charge_sheet")
@Schema(description = "收费单")
public class ChargeSheet extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("charge_no")
  private String chargeNo;

  @TableField("encounter_id")
  private String encounterId;

  @TableField("patient_id")
  private String patientId;

  @TableField("patient_name")
  private String patientName;

  @TableField("prescription_id")
  private String prescriptionId;

  @TableField("total_amount")
  private BigDecimal totalAmount;

  @TableField("paid_amount")
  private BigDecimal paidAmount;

  @TableField("refund_amount")
  private BigDecimal refundAmount;

  @TableField("status")
  private String status;

  @TableField("pay_method")
  private String payMethod;

  @TableField("pay_time")
  private LocalDateTime payTime;

  @TableField("cashier_id")
  private String cashierId;

  @TableField("cashier_name")
  private String cashierName;

  @TableField("remark")
  private String remark;

  @TableField(exist = false)
  private List<ChargeSheetItem> items;

  @TableField(exist = false)
  private List<Payment> payments;

  @TableField(exist = false)
  private String keyword;

  @TableField(exist = false)
  private String prescriptionNo;
}
