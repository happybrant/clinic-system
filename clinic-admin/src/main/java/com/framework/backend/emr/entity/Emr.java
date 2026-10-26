package com.framework.backend.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_emr")
@Schema(description = "电子病历")
public class Emr extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("encounter_id")
  private String encounterId;

  @TableField("patient_id")
  private String patientId;

  @TableField("doctor_id")
  private String doctorId;

  @TableField("doctor_name")
  private String doctorName;

  @TableField("chief_complaint")
  private String chiefComplaint;

  @TableField("present_illness")
  private String presentIllness;

  @TableField("past_history")
  private String pastHistory;

  @TableField("allergy_history")
  private String allergyHistory;

  @TableField("physical_exam")
  private String physicalExam;

  @TableField("diagnosis")
  private String diagnosis;

  @TableField("diagnosis_code")
  private String diagnosisCode;

  @TableField("treatment")
  private String treatment;

  @TableField("advice")
  private String advice;

  @TableField("status")
  private String status;

  @TableField("submit_time")
  private LocalDateTime submitTime;

  @TableField("version_no")
  private Integer versionNo;
}
