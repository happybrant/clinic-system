package com.framework.backend.patient.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_patient")
@Schema(description = "患者")
public class Patient extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("patient_no")
  private String patientNo;

  @TableField("name")
  private String name;

  @TableField("gender")
  private String gender;

  @TableField("birthday")
  private LocalDate birthday;

  @TableField("age")
  private Integer age;

  @TableField("mobile")
  private String mobile;

  @TableField("id_type")
  private String idType;

  @TableField("id_number")
  private String idNumber;

  @TableField("address")
  private String address;

  @TableField("marital_status")
  private String maritalStatus;

  @TableField("occupation")
  private String occupation;

  @TableField("nation")
  private String nation;

  @TableField("height_cm")
  private BigDecimal heightCm;

  @TableField("weight_kg")
  private BigDecimal weightKg;

  @TableField("source")
  private String source;

  @TableField("allergy_history")
  private String allergyHistory;

  @TableField("past_history")
  private String pastHistory;

  @TableField("remark")
  private String remark;

  @TableField("tags")
  private String tags;

  @TableField("visit_count")
  private Integer visitCount;

  @TableField("total_amount")
  private BigDecimal totalAmount;

  @TableField("last_visit_time")
  private LocalDateTime lastVisitTime;

  @TableField("status")
  private String status;

  @TableField(exist = false)
  private String keyword;

  @TableField(exist = false)
  private List<PatientContact> contacts;

  @TableField(exist = false)
  private List<PatientLog> logs;
}
