package com.framework.backend.charge.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_payment")
@Schema(description = "收付款流水")
public class Payment extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("charge_id")
  private String chargeId;

  @TableField("pay_method")
  private String payMethod;

  @TableField("amount")
  private BigDecimal amount;

  @TableField("pay_type")
  private String payType;

  @TableField("pay_time")
  private LocalDateTime payTime;

  @TableField("remark")
  private String remark;
}
