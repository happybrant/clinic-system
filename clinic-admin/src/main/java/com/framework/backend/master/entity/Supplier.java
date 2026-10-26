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
@TableName("clinic_supplier")
@Schema(description = "供应商")
public class Supplier extends MyBaseEntity {
  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "供应商编码")
  @TableField("supplier_code")
  private String supplierCode;

  @Schema(description = "供应商名称")
  @TableField("supplier_name")
  private String supplierName;

  @Schema(description = "联系人")
  @TableField("contact_name")
  private String contactName;

  @Schema(description = "联系电话")
  @TableField("contact_mobile")
  private String contactMobile;

  @Schema(description = "地址")
  @TableField("address")
  private String address;

  @Schema(description = "状态")
  @TableField("status")
  private String status;

  @Schema(description = "备注")
  @TableField("remark")
  private String remark;

  @TableField(exist = false)
  private String keyword;
}
