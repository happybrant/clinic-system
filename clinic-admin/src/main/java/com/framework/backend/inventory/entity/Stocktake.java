package com.framework.backend.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_stocktake")
@Schema(description = "盘点单")
public class Stocktake extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("stocktake_no")
  private String stocktakeNo;

  @TableField("status")
  private String status;

  @TableField("remark")
  private String remark;

  @TableField("post_time")
  private LocalDateTime postTime;

  @TableField("operator_name")
  private String operatorName;

  @TableField(exist = false)
  private String keyword;

  @TableField(exist = false)
  private List<StocktakeItem> items;
}
