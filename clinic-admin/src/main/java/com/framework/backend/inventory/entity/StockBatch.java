package com.framework.backend.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_stock_batch")
@Schema(description = "库存批次")
public class StockBatch extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("drug_id")
  private String drugId;

  @TableField("drug_code")
  private String drugCode;

  @TableField("drug_name")
  private String drugName;

  @TableField("batch_no")
  private String batchNo;

  @TableField("expire_date")
  private LocalDate expireDate;

  @TableField("quantity")
  private BigDecimal quantity;

  @TableField("locked_qty")
  private BigDecimal lockedQty;

  @TableField("cost_price")
  private BigDecimal costPrice;

  @TableField("supplier_id")
  private String supplierId;

  @TableField("warehouse")
  private String warehouse;

  @TableField("status")
  private String status;
}
