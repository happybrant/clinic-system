package com.framework.backend.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_serial")
@Schema(description = "业务流水号")
public class ClinicSerial extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "流水键，如 P:20261008")
  @TableField("serial_key")
  private String serialKey;

  @Schema(description = "当前值")
  @TableField("current_val")
  private Integer currentVal;
}
