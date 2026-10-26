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
@TableName("clinic_inout_bound_item")
@Schema(description = "出入库明细")
public class InoutBoundItem extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("bound_id")
  private String boundId;

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

  @TableField("unit_price")
  private BigDecimal unitPrice;

  @TableField("amount")
  private BigDecimal amount;

  @TableField("unit")
  private String unit;
}
