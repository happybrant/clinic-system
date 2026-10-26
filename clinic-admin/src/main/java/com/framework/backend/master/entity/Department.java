package com.framework.backend.master.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_department")
@Schema(description = "科室")
public class Department extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "科室编码")
  @TableField("code")
  private String code;

  @Schema(description = "科室名称")
  @TableField("name")
  private String name;

  @Schema(description = "状态")
  @TableField("status")
  private String status;

  @Schema(description = "排序")
  @TableField("sort")
  private Integer sort;

  @Schema(description = "备注")
  @TableField("remark")
  private String remark;

  @TableField(exist = false)
  private String keyword;
}
