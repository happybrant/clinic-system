-- ============================================================
-- 诊所管理系统 DDL（MySQL 8）
-- 包含：框架基础表 + 业务表
-- ============================================================

CREATE DATABASE IF NOT EXISTS clinic DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE clinic;

-- -------------------- 框架：用户 / 角色 / 资源 --------------------
CREATE TABLE IF NOT EXISTS security_user (
  id            VARCHAR(36)  PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL COMMENT '登录名',
  password      VARCHAR(128) NOT NULL COMMENT '密码',
  avatar        VARCHAR(255) COMMENT '头像，图片URL',
  real_name     VARCHAR(50) COMMENT '真实姓名',
  email         VARCHAR(50) COMMENT '邮箱',
  remark        VARCHAR(2000) COMMENT '备注',
  status        VARCHAR(10)  DEFAULT 'active' COMMENT '状态',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户表';

CREATE TABLE IF NOT EXISTS security_role (
  id            VARCHAR(36)  PRIMARY KEY,
  name          VARCHAR(50)  NOT NULL COMMENT '角色名',
  code          VARCHAR(50)  NOT NULL COMMENT '角色编码',
  status        VARCHAR(10)  DEFAULT 'active' COMMENT '状态',
  remark        VARCHAR(2000) COMMENT '备注',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '角色表';

CREATE TABLE IF NOT EXISTS security_resource (
  id            VARCHAR(36)  PRIMARY KEY,
  name          VARCHAR(50)  NOT NULL COMMENT '资源名',
  code          VARCHAR(50)  NOT NULL COMMENT '资源编码',
  parent_id     VARCHAR(36)  DEFAULT '0' COMMENT '父资源ID',
  type          VARCHAR(50)  COMMENT '资源类型',
  status        VARCHAR(50)  DEFAULT 'active' COMMENT '状态',
  sort          INT          DEFAULT 0 COMMENT '排序',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '资源表';

CREATE TABLE IF NOT EXISTS security_authorization (
  id            VARCHAR(36)  PRIMARY KEY,
  role_id       VARCHAR(36)  NOT NULL COMMENT '角色ID',
  resource_id   VARCHAR(36)  NOT NULL COMMENT '资源ID',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '授权表';

CREATE TABLE IF NOT EXISTS security_user_role_rel (
  id            VARCHAR(36)  PRIMARY KEY,
  user_id       VARCHAR(36)  NOT NULL,
  role_id       VARCHAR(36)  NOT NULL COMMENT '角色ID',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '用户角色关联表';

CREATE TABLE IF NOT EXISTS security_log (
  id            VARCHAR(36)  PRIMARY KEY,
  content       VARCHAR(2000) COMMENT '操作内容简述',
  account       VARCHAR(100) COMMENT '操作用户账号',
  module        VARCHAR(100) COMMENT '操作模块',
  request_ip    VARCHAR(50) COMMENT '请求IP',
  start_time    DATETIME COMMENT '请求开始时间',
  end_time      DATETIME COMMENT '请求结束时间',
  cost          INT COMMENT '耗时',
  agent         VARCHAR(200) COMMENT '请求客户端',
  result_flag   VARCHAR(10) DEFAULT 'success' COMMENT '返回结果标志',
  error_msg     TEXT COMMENT '错误信息',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '日志表';

CREATE TABLE IF NOT EXISTS utility_dict (
  id            VARCHAR(36)  PRIMARY KEY,
  code          VARCHAR(50)  NOT NULL,
  name          VARCHAR(100) NOT NULL COMMENT '字典名',
  parent_id     VARCHAR(36)  DEFAULT '0' COMMENT '父字典ID',
  group_code    VARCHAR(50)  NOT NULL COMMENT '字典组编码',
  status        VARCHAR(50)  DEFAULT 'active' COMMENT '状态',
  description   VARCHAR(500) COMMENT '描述',
  sort          INT          DEFAULT 0 COMMENT '排序',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '字典表';


CREATE TABLE IF NOT EXISTS utility_attachment (
  id            VARCHAR(36)  PRIMARY KEY,
  name          VARCHAR(200) COMMENT '文件名',
  origin_name   VARCHAR(200) COMMENT '原始文件名',
  storage_mode  VARCHAR(20) COMMENT '文件存储模式',
  file_size     BIGINT COMMENT '文件大小，默认KB',
  full_path     VARCHAR(500) COMMENT '附件绝对路径',
  create_user   VARCHAR(36) COMMENT '创建用户',
  create_time   DATETIME COMMENT '创建时间',
  update_user   VARCHAR(36) COMMENT '更新用户',
  update_time   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '附件表';

-- -------------------- 业务：患者 --------------------
CREATE TABLE IF NOT EXISTS clinic_patient (
  id              VARCHAR(36)  PRIMARY KEY,
  patient_no      VARCHAR(32)  NOT NULL COMMENT '患者编号 P+yyyyMMdd+流水',
  name            VARCHAR(50)  NOT NULL,
  gender          VARCHAR(10)  COMMENT 'M/F/U',
  birthday        DATE,
  age             INT,
  mobile          VARCHAR(20),
  id_type         VARCHAR(20)  DEFAULT 'id_card',
  id_number       VARCHAR(50),
  address         VARCHAR(200),
  marital_status  VARCHAR(20),
  occupation      VARCHAR(50),
  nation          VARCHAR(30),
  height_cm       DECIMAL(6,2),
  weight_kg       DECIMAL(6,2),
  source          VARCHAR(50)  COMMENT '来源',
  allergy_history TEXT         COMMENT '过敏史',
  past_history    TEXT         COMMENT '既往史',
  remark          TEXT,
  tags            VARCHAR(200) COMMENT '标签，逗号分隔',
  visit_count     INT          DEFAULT 0,
  total_amount    DECIMAL(12,2) DEFAULT 0,
  last_visit_time DATETIME,
  status          VARCHAR(20)  DEFAULT 'active',
  create_user     VARCHAR(36),
  create_time     DATETIME,
  update_user     VARCHAR(36),
  update_time     DATETIME,
  UNIQUE KEY uk_patient_no (patient_no),
  KEY idx_patient_mobile (mobile),
  KEY idx_patient_name (name),
  KEY idx_patient_id_number (id_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_patient_contact (
  id            VARCHAR(36)  PRIMARY KEY,
  patient_id    VARCHAR(36)  NOT NULL,
  name          VARCHAR(50)  NOT NULL,
  relation      VARCHAR(30),
  mobile        VARCHAR(20),
  remark        VARCHAR(200),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_contact_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_patient_log (
  id            VARCHAR(36)  PRIMARY KEY,
  patient_id    VARCHAR(36)  NOT NULL,
  operation     VARCHAR(50),
  content       TEXT,
  operate_time  DATETIME,
  operator_name VARCHAR(50),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_plog_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：主数据 --------------------
CREATE TABLE IF NOT EXISTS clinic_department (
  id            VARCHAR(36)  PRIMARY KEY,
  code          VARCHAR(50)  NOT NULL,
  name          VARCHAR(100) NOT NULL,
  status        VARCHAR(20)  DEFAULT 'active',
  sort          INT          DEFAULT 0,
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_dept_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_doctor (
  id            VARCHAR(36)  PRIMARY KEY,
  user_id       VARCHAR(36)  COMMENT '关联系统账号',
  emp_no        VARCHAR(50),
  name          VARCHAR(50)  NOT NULL,
  gender        VARCHAR(10),
  title         VARCHAR(50)  COMMENT '职称',
  dept_id       VARCHAR(36),
  dept_name     VARCHAR(100),
  mobile        VARCHAR(20),
  specialty     VARCHAR(200) COMMENT '擅长',
  can_schedule  TINYINT(1)   DEFAULT 1,
  status        VARCHAR(20)  DEFAULT 'active',
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_drug (
  id            VARCHAR(36)  PRIMARY KEY,
  drug_code     VARCHAR(50)  NOT NULL,
  drug_name     VARCHAR(100) NOT NULL,
  generic_name  VARCHAR(100) COMMENT '通用名',
  spec          VARCHAR(100) COMMENT '规格',
  unit          VARCHAR(20)  COMMENT '单位',
  dosage_form   VARCHAR(50)  COMMENT '剂型',
  manufacturer  VARCHAR(100),
  sale_price    DECIMAL(12,4) NOT NULL DEFAULT 0,
  cost_price    DECIMAL(12,4) DEFAULT 0,
  drug_type     VARCHAR(30)  COMMENT '西药/中成药/中药饮片等',
  prescription_flag TINYINT(1) DEFAULT 1 COMMENT '是否处方药',
  status        VARCHAR(20)  DEFAULT 'active',
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_drug_code (drug_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_charge_item (
  id            VARCHAR(36)  PRIMARY KEY,
  item_code     VARCHAR(50)  NOT NULL,
  item_name     VARCHAR(100) NOT NULL,
  item_type     VARCHAR(30)  COMMENT 'register/consult/other',
  price         DECIMAL(12,4) NOT NULL DEFAULT 0,
  unit          VARCHAR(20),
  status        VARCHAR(20)  DEFAULT 'active',
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_charge_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_supplier (
  id            VARCHAR(36)  PRIMARY KEY,
  supplier_code VARCHAR(50)  NOT NULL,
  supplier_name VARCHAR(100) NOT NULL,
  contact_name  VARCHAR(50),
  contact_mobile VARCHAR(20),
  address       VARCHAR(200),
  status        VARCHAR(20)  DEFAULT 'active',
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：排班 / 预约 --------------------
CREATE TABLE IF NOT EXISTS clinic_schedule_slot (
  id            VARCHAR(36)  PRIMARY KEY,
  schedule_date DATE         NOT NULL,
  dept_id       VARCHAR(36),
  dept_name     VARCHAR(100),
  doctor_id     VARCHAR(36)  NOT NULL,
  doctor_name   VARCHAR(50),
  time_period   VARCHAR(20)  COMMENT 'morning/afternoon/evening',
  start_time    VARCHAR(10),
  end_time      VARCHAR(10),
  total_quota   INT          NOT NULL DEFAULT 0,
  used_quota    INT          NOT NULL DEFAULT 0,
  status        VARCHAR(20)  DEFAULT 'open' COMMENT 'open/closed',
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_slot_date_doctor (schedule_date, doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_appointment (
  id            VARCHAR(36)  PRIMARY KEY,
  appointment_no VARCHAR(32) NOT NULL,
  patient_id    VARCHAR(36)  NOT NULL,
  patient_name  VARCHAR(50),
  patient_mobile VARCHAR(20),
  slot_id       VARCHAR(36)  NOT NULL,
  schedule_date DATE,
  dept_id       VARCHAR(36),
  dept_name     VARCHAR(100),
  doctor_id     VARCHAR(36),
  doctor_name   VARCHAR(50),
  time_period   VARCHAR(20),
  start_time    VARCHAR(10),
  end_time      VARCHAR(10),
  status        VARCHAR(20)  NOT NULL DEFAULT 'Booked'
                COMMENT 'Booked/CheckedIn/Cancelled/Rescheduled/NoShow',
  source        VARCHAR(20)  COMMENT 'online/front',
  remark        VARCHAR(500),
  checkin_time  DATETIME,
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_appointment_no (appointment_no),
  KEY idx_appt_patient (patient_id),
  KEY idx_appt_slot (slot_id),
  KEY idx_appt_date (schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：就诊 --------------------
CREATE TABLE IF NOT EXISTS clinic_encounter (
  id            VARCHAR(36)  PRIMARY KEY,
  encounter_no  VARCHAR(32)  NOT NULL,
  patient_id    VARCHAR(36)  NOT NULL,
  patient_name  VARCHAR(50),
  patient_gender VARCHAR(10),
  patient_age   INT,
  patient_mobile VARCHAR(20),
  dept_id       VARCHAR(36),
  dept_name     VARCHAR(100),
  doctor_id     VARCHAR(36),
  doctor_name   VARCHAR(50),
  appointment_id VARCHAR(36),
  queue_no      INT,
  status        VARCHAR(20)  NOT NULL DEFAULT 'Waiting'
                COMMENT 'Waiting/InConsult/Skipped/Finished',
  register_type VARCHAR(20)  COMMENT 'appointment/walkin',
  register_fee  DECIMAL(12,2) DEFAULT 0,
  start_time    DATETIME,
  end_time      DATETIME,
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_encounter_no (encounter_no),
  KEY idx_enc_patient (patient_id),
  KEY idx_enc_doctor_status (doctor_id, status),
  KEY idx_enc_date (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：病历 --------------------
CREATE TABLE IF NOT EXISTS clinic_emr (
  id            VARCHAR(36)  PRIMARY KEY,
  encounter_id  VARCHAR(36)  NOT NULL,
  patient_id    VARCHAR(36)  NOT NULL,
  doctor_id     VARCHAR(36),
  doctor_name   VARCHAR(50),
  chief_complaint TEXT       COMMENT '主诉',
  present_illness TEXT       COMMENT '现病史',
  past_history  TEXT,
  allergy_history TEXT,
  physical_exam TEXT         COMMENT '体格检查',
  diagnosis     TEXT         COMMENT '诊断',
  diagnosis_code VARCHAR(100) COMMENT 'ICD编码，逗号分隔',
  treatment     TEXT         COMMENT '处理意见',
  advice        TEXT         COMMENT '医嘱建议',
  status        VARCHAR(20)  DEFAULT 'draft' COMMENT 'draft/submitted',
  submit_time   DATETIME,
  version_no    INT          DEFAULT 1,
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_emr_encounter (encounter_id),
  KEY idx_emr_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：处方 --------------------
CREATE TABLE IF NOT EXISTS clinic_prescription (
  id            VARCHAR(36)  PRIMARY KEY,
  prescription_no VARCHAR(32) NOT NULL,
  encounter_id  VARCHAR(36)  NOT NULL,
  patient_id    VARCHAR(36)  NOT NULL,
  patient_name  VARCHAR(50),
  doctor_id     VARCHAR(36),
  doctor_name   VARCHAR(50),
  dept_name     VARCHAR(100),
  rx_type       VARCHAR(20)  DEFAULT 'western' COMMENT 'western/chinese',
  diagnosis     VARCHAR(500),
  status        VARCHAR(20)  NOT NULL DEFAULT 'Draft'
                COMMENT 'Draft/Submitted/Charged/Dispensing/Dispensed/Voided/Refunded/Returned',
  total_amount  DECIMAL(12,2) DEFAULT 0,
  remark        VARCHAR(500),
  submit_time   DATETIME,
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_rx_no (prescription_no),
  KEY idx_rx_encounter (encounter_id),
  KEY idx_rx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_prescription_item (
  id              VARCHAR(36)  PRIMARY KEY,
  prescription_id VARCHAR(36)  NOT NULL,
  drug_id         VARCHAR(36)  NOT NULL,
  drug_code       VARCHAR(50),
  drug_name       VARCHAR(100),
  spec            VARCHAR(100),
  unit            VARCHAR(20),
  dosage          VARCHAR(50)  COMMENT '单次用量',
  frequency       VARCHAR(50)  COMMENT '频次',
  days            INT,
  quantity        DECIMAL(12,2) NOT NULL,
  unit_price      DECIMAL(12,4) NOT NULL,
  amount          DECIMAL(12,2) NOT NULL,
  usage_method    VARCHAR(100) COMMENT '用法',
  sort_no         INT          DEFAULT 0,
  create_user     VARCHAR(36),
  create_time     DATETIME,
  update_user     VARCHAR(36),
  update_time     DATETIME,
  KEY idx_rxi_rx (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：收费 --------------------
CREATE TABLE IF NOT EXISTS clinic_charge_sheet (
  id            VARCHAR(36)  PRIMARY KEY,
  charge_no     VARCHAR(32)  NOT NULL,
  encounter_id  VARCHAR(36),
  patient_id    VARCHAR(36)  NOT NULL,
  patient_name  VARCHAR(50),
  prescription_id VARCHAR(36),
  total_amount  DECIMAL(12,2) NOT NULL DEFAULT 0,
  paid_amount   DECIMAL(12,2) DEFAULT 0,
  refund_amount DECIMAL(12,2) DEFAULT 0,
  status        VARCHAR(20)  NOT NULL DEFAULT 'Unpaid'
                COMMENT 'Unpaid/Paid/Cancelled/PartialRefunded/Refunded',
  pay_method    VARCHAR(30)  COMMENT 'cash/wechat/alipay/card/mix',
  pay_time      DATETIME,
  cashier_id    VARCHAR(36),
  cashier_name  VARCHAR(50),
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_charge_no (charge_no),
  KEY idx_charge_patient (patient_id),
  KEY idx_charge_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_charge_sheet_item (
  id            VARCHAR(36)  PRIMARY KEY,
  charge_id     VARCHAR(36)  NOT NULL,
  item_type     VARCHAR(30)  COMMENT 'register/consult/drug/other',
  item_id       VARCHAR(36),
  item_code     VARCHAR(50),
  item_name     VARCHAR(100),
  quantity      DECIMAL(12,2) DEFAULT 1,
  unit_price    DECIMAL(12,4) DEFAULT 0,
  amount        DECIMAL(12,2) DEFAULT 0,
  ref_id        VARCHAR(36)  COMMENT '关联处方明细等',
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_csi_charge (charge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_payment (
  id            VARCHAR(36)  PRIMARY KEY,
  charge_id     VARCHAR(36)  NOT NULL,
  pay_method    VARCHAR(30)  NOT NULL,
  amount        DECIMAL(12,2) NOT NULL,
  pay_type      VARCHAR(20)  DEFAULT 'pay' COMMENT 'pay/refund',
  pay_time      DATETIME,
  remark        VARCHAR(200),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_pay_charge (charge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：发药 --------------------
CREATE TABLE IF NOT EXISTS clinic_dispense (
  id            VARCHAR(36)  PRIMARY KEY,
  dispense_no   VARCHAR(32)  NOT NULL,
  prescription_id VARCHAR(36) NOT NULL,
  prescription_no VARCHAR(32),
  patient_id    VARCHAR(36),
  patient_name  VARCHAR(50),
  status        VARCHAR(20)  DEFAULT 'pending' COMMENT 'pending/dispensed/rejected',
  pharmacist_id VARCHAR(36),
  pharmacist_name VARCHAR(50),
  dispense_time DATETIME,
  reject_reason VARCHAR(500),
  remark        VARCHAR(500),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_dispense_no (dispense_no),
  KEY idx_disp_rx (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_dispense_item (
  id            VARCHAR(36)  PRIMARY KEY,
  dispense_id   VARCHAR(36)  NOT NULL,
  rx_item_id    VARCHAR(36),
  drug_id       VARCHAR(36),
  drug_name     VARCHAR(100),
  batch_id      VARCHAR(36),
  batch_no      VARCHAR(50),
  quantity      DECIMAL(12,2) NOT NULL,
  unit          VARCHAR(20),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_di_dispense (dispense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：库存 --------------------
CREATE TABLE IF NOT EXISTS clinic_stock_batch (
  id            VARCHAR(36)  PRIMARY KEY,
  drug_id       VARCHAR(36)  NOT NULL,
  drug_code     VARCHAR(50),
  drug_name     VARCHAR(100),
  batch_no      VARCHAR(50)  NOT NULL,
  expire_date   DATE,
  quantity      DECIMAL(12,2) NOT NULL DEFAULT 0,
  locked_qty    DECIMAL(12,2) DEFAULT 0,
  cost_price    DECIMAL(12,4) DEFAULT 0,
  supplier_id   VARCHAR(36),
  warehouse     VARCHAR(50)  DEFAULT 'default',
  status        VARCHAR(20)  DEFAULT 'active',
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_batch (drug_id, batch_no),
  KEY idx_batch_expire (expire_date),
  KEY idx_batch_drug (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_inout_bound (
  id            VARCHAR(36)  PRIMARY KEY,
  bound_no      VARCHAR(32)  NOT NULL,
  bound_type    VARCHAR(30)  NOT NULL
                COMMENT 'PurchaseIn/PurchaseReturn/DispenseOut/ReturnIn/StocktakeAdjust',
  status        VARCHAR(20)  DEFAULT 'Draft' COMMENT 'Draft/Posted/Voided',
  supplier_id   VARCHAR(36),
  supplier_name VARCHAR(100),
  total_amount  DECIMAL(12,2) DEFAULT 0,
  biz_ref_id    VARCHAR(36)  COMMENT '关联业务单',
  remark        VARCHAR(500),
  post_time     DATETIME,
  operator_id   VARCHAR(36),
  operator_name VARCHAR(50),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_bound_no (bound_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_inout_bound_item (
  id            VARCHAR(36)  PRIMARY KEY,
  bound_id      VARCHAR(36)  NOT NULL,
  drug_id       VARCHAR(36)  NOT NULL,
  drug_code     VARCHAR(50),
  drug_name     VARCHAR(100),
  batch_no      VARCHAR(50),
  expire_date   DATE,
  quantity      DECIMAL(12,2) NOT NULL,
  unit_price    DECIMAL(12,4) DEFAULT 0,
  amount        DECIMAL(12,2) DEFAULT 0,
  unit          VARCHAR(20),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_bound_item (bound_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_stocktake (
  id            VARCHAR(36)  PRIMARY KEY,
  stocktake_no  VARCHAR(32)  NOT NULL,
  status        VARCHAR(20)  DEFAULT 'Draft' COMMENT 'Draft/Posted',
  remark        VARCHAR(500),
  post_time     DATETIME,
  operator_name VARCHAR(50),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_stocktake_no (stocktake_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_stocktake_item (
  id            VARCHAR(36)  PRIMARY KEY,
  stocktake_id  VARCHAR(36)  NOT NULL,
  batch_id      VARCHAR(36),
  drug_id       VARCHAR(36),
  drug_name     VARCHAR(100),
  batch_no      VARCHAR(50),
  book_qty      DECIMAL(12,2) DEFAULT 0,
  actual_qty    DECIMAL(12,2) DEFAULT 0,
  diff_qty      DECIMAL(12,2) DEFAULT 0,
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  KEY idx_sti (stocktake_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------- 业务：系统参数 / 流水号 --------------------
CREATE TABLE IF NOT EXISTS clinic_config (
  id            VARCHAR(36)  PRIMARY KEY,
  config_key    VARCHAR(100) NOT NULL,
  config_value  VARCHAR(500),
  description   VARCHAR(200),
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_serial (
  id            VARCHAR(36)  PRIMARY KEY,
  serial_key    VARCHAR(50)  NOT NULL COMMENT '如 P:20261008',
  current_val   INT          NOT NULL DEFAULT 0,
  create_user   VARCHAR(36),
  create_time   DATETIME,
  update_user   VARCHAR(36),
  update_time   DATETIME,
  UNIQUE KEY uk_serial_key (serial_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
