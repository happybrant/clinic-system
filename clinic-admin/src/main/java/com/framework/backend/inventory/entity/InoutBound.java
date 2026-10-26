package com.framework.backend.inventory.entity;

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
@TableName("clinic_inout_bound")
@Schema(description = "出入库单")
public class InoutBound extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("bound_no")
  private String boundNo;

  @TableField("bound_type")
  private String boundType;

  @TableField("status")
  private String status;

  @TableField("supplier_id")
  private String supplierId;

  @TableField("supplier_name")
  private String supplierName;

  @TableField("total_amount")
  private BigDecimal totalAmount;

  @TableField("biz_ref_id")
  private String bizRefId;

  @TableField("remark")
  private String remark;

  @TableField("post_time")
  private LocalDateTime postTime;

  @TableField("operator_id")
  private String operatorId;

  @TableField("operator_name")
  private String operatorName;

  @TableField(exist = false)
  private List<InoutBoundItem> items;
}
