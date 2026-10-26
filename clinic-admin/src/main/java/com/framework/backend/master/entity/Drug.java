package com.framework.backend.master.entity;

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
@TableName("clinic_drug")
@Schema(description = "药品")
public class Drug extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "药品编码")
  @TableField("drug_code")
  private String drugCode;

  @Schema(description = "药品名称")
  @TableField("drug_name")
  private String drugName;

  @Schema(description = "通用名")
  @TableField("generic_name")
  private String genericName;

  @Schema(description = "规格")
  @TableField("spec")
  private String spec;

  @Schema(description = "单位")
  @TableField("unit")
  private String unit;

  @Schema(description = "剂型")
  @TableField("dosage_form")
  private String dosageForm;

  @Schema(description = "生产厂家")
  @TableField("manufacturer")
  private String manufacturer;

  @Schema(description = "售价")
  @TableField("sale_price")
  private BigDecimal salePrice;

  @Schema(description = "成本价")
  @TableField("cost_price")
  private BigDecimal costPrice;

  @Schema(description = "药品类型")
  @TableField("drug_type")
  private String drugType;

  @Schema(description = "是否处方药")
  @TableField("prescription_flag")
  private Boolean prescriptionFlag;

  @Schema(description = "状态")
  @TableField("status")
  private String status;

  @Schema(description = "备注")
  @TableField("remark")
  private String remark;

  @TableField(exist = false)
  private String keyword;
}
