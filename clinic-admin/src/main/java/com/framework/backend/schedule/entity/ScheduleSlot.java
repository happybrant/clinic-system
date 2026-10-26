package com.framework.backend.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_schedule_slot")
@Schema(description = "排班号源")
public class ScheduleSlot extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("schedule_date")
  private LocalDate scheduleDate;

  @TableField("dept_id")
  private String deptId;

  @TableField("dept_name")
  private String deptName;

  @TableField("doctor_id")
  private String doctorId;

  @TableField("doctor_name")
  private String doctorName;

  @TableField("time_period")
  private String timePeriod;

  @TableField("start_time")
  private String startTime;

  @TableField("end_time")
  private String endTime;

  @TableField("total_quota")
  private Integer totalQuota;

  @TableField("used_quota")
  private Integer usedQuota;

  /** open / closed */
  @TableField("status")
  private String status;
}
