package com.framework.backend.prescription.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_prescription_item")
@Schema(description = "处方明细")
public class PrescriptionItem extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("prescription_id")
  private String prescriptionId;

  @TableField("drug_id")
  private String drugId;

  @TableField("drug_code")
  private String drugCode;

  @TableField("drug_name")
  private String drugName;

  @TableField("spec")
  private String spec;

  @TableField("unit")
  private String unit;

  @TableField("dosage")
  private String dosage;

  @TableField("frequency")
  private String frequency;

  @TableField("days")
  private Integer days;

  @TableField("quantity")
  private BigDecimal quantity;

  @TableField("unit_price")
  private BigDecimal unitPrice;

  @TableField("amount")
  private BigDecimal amount;

  @TableField("usage_method")
  private String usageMethod;

  @TableField("sort_no")
  private Integer sortNo;
}
