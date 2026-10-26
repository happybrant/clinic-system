package com.framework.backend.pharmacy.entity;

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
@TableName("clinic_dispense_item")
@Schema(description = "发药明细")
public class DispenseItem extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("dispense_id")
  private String dispenseId;

  @TableField("rx_item_id")
  private String rxItemId;

  @TableField("drug_id")
  private String drugId;

  @TableField("drug_name")
  private String drugName;

  @TableField("batch_id")
  private String batchId;

  @TableField("batch_no")
  private String batchNo;

  @TableField("quantity")
  private BigDecimal quantity;

  @TableField("unit")
  private String unit;
}
