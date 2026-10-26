package com.framework.backend.patient.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.framework.backend.common.MyBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clinic_patient_contact")
@Schema(description = "患者联系人")
public class PatientContact extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @TableField("patient_id")
  private String patientId;

  @TableField("name")
  private String name;

  @TableField("relation")
  private String relation;

  @TableField("mobile")
  private String mobile;

  @TableField("remark")
  private String remark;
}
