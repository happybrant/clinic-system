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
@TableName("clinic_doctor")
@Schema(description = "医生")
public class Doctor extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "关联系统账号")
  @TableField("user_id")
  private String userId;

  @Schema(description = "工号")
  @TableField("emp_no")
  private String empNo;

  @Schema(description = "姓名")
  @TableField("name")
  private String name;

  @Schema(description = "性别")
  @TableField("gender")
  private String gender;

  @Schema(description = "职称")
  @TableField("title")
  private String title;

  @Schema(description = "科室id")
  @TableField("dept_id")
  private String deptId;

  @Schema(description = "科室名称")
  @TableField("dept_name")
  private String deptName;

  @Schema(description = "手机号")
  @TableField("mobile")
  private String mobile;

  @Schema(description = "擅长")
  @TableField("specialty")
  private String specialty;

  @Schema(description = "是否可排班")
  @TableField("can_schedule")
  private Boolean canSchedule;

  @Schema(description = "状态")
  @TableField("status")
  private String status;

  @Schema(description = "备注")
  @TableField("remark")
  private String remark;

  @TableField(exist = false)
  private String keyword;
}
