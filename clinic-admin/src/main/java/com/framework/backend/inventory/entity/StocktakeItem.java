package com.framework.backend.inventory.entity;

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
@TableName("clinic_stocktake_item")
@Schema(description = "盘点明细")
public class StocktakeItem extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("stocktake_id")
  private String stocktakeId;

  @TableField("batch_id")
  private String batchId;

  @TableField("drug_id")
  private String drugId;

  @TableField("drug_name")
  private String drugName;

  @TableField("batch_no")
  private String batchNo;

  @TableField("book_qty")
  private BigDecimal bookQty;

  @TableField("actual_qty")
  private BigDecimal actualQty;

  @TableField("diff_qty")
  private BigDecimal diffQty;
}
