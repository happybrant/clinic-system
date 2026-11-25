package com.framework.backend.charge.entity;

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
@TableName("clinic_charge_sheet_item")
@Schema(description = "收费单明细")
public class ChargeSheetItem extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("charge_id")
  private String chargeId;

  @TableField("item_type")
  private String itemType;

  @TableField("item_id")
  private String itemId;

  @TableField("item_code")
  private String itemCode;

  @TableField("item_name")
  private String itemName;

  @TableField("quantity")
  private BigDecimal quantity;

  @TableField("unit_price")
  private BigDecimal unitPrice;

  @TableField("amount")
  private BigDecimal amount;

  @TableField("ref_id")
  private String refId;
}
