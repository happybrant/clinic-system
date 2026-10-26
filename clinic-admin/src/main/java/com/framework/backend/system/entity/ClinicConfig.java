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
@TableName("clinic_config")
@Schema(description = "诊所系统参数")
public class ClinicConfig extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "参数键")
  @TableField("config_key")
  private String configKey;

  @Schema(description = "参数值")
  @TableField("config_value")
  private String configValue;

  @Schema(description = "描述")
  @TableField("description")
  private String description;
}
