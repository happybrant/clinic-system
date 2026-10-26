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
@TableName("clinic_charge_item")
@Schema(description = "收费项目")
public class ChargeItem extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "项目编码")
  @TableField("item_code")
  private String itemCode;

  @Schema(description = "项目名称")
  @TableField("item_name")
  private String itemName;

  @Schema(description = "项目类型")
  @TableField("item_type")
  private String itemType;

  @Schema(description = "单价")
  @TableField("price")
  private BigDecimal price;

  @Schema(description = "单位")
  @TableField("unit")
  private String unit;

  @Schema(description = "状态")
  @TableField("status")
  private String status;

  @Schema(description = "备注")
  @TableField("remark")
  private String remark;

  @TableField(exist = false)
  private String keyword;
}
