package com.framework.backend.common;

/** 诊所业务常量 */
public final class ClinicConstants {

  private ClinicConstants() {}

  public static final String PREFIX_PATIENT = "P";
  public static final String PREFIX_APPOINTMENT = "A";
  public static final String PREFIX_ENCOUNTER = "E";
  public static final String PREFIX_PRESCRIPTION = "RX";
  public static final String PREFIX_CHARGE = "CH";
  public static final String PREFIX_DISPENSE = "DP";
  public static final String PREFIX_INBOUND = "IN";
  public static final String PREFIX_STOCKTAKE = "ST";

  public static final String STATUS_ACTIVE = "active";
  public static final String STATUS_DISABLED = "disabled";

  /** 预约状态 */
  public static final String APPT_BOOKED = "Booked";
  public static final String APPT_CHECKED_IN = "CheckedIn";
  public static final String APPT_CANCELLED = "Cancelled";
  public static final String APPT_RESCHEDULED = "Rescheduled";
  public static final String APPT_NO_SHOW = "NoShow";

  /** 就诊状态 */
  public static final String ENC_WAITING = "Waiting";
  public static final String ENC_IN_CONSULT = "InConsult";
  public static final String ENC_SKIPPED = "Skipped";
  public static final String ENC_FINISHED = "Finished";

  /** 病历状态 */
  public static final String EMR_DRAFT = "draft";
  public static final String EMR_SUBMITTED = "submitted";

  /** 处方状态 */
  public static final String RX_DRAFT = "Draft";
  public static final String RX_SUBMITTED = "Submitted";
  public static final String RX_CHARGED = "Charged";
  public static final String RX_DISPENSING = "Dispensing";
  public static final String RX_DISPENSED = "Dispensed";
  public static final String RX_VOIDED = "Voided";
  public static final String RX_REFUNDED = "Refunded";
  public static final String RX_RETURNED = "Returned";

  /** 收费单状态 */
  public static final String CHARGE_UNPAID = "Unpaid";
  public static final String CHARGE_PAID = "Paid";
  public static final String CHARGE_CANCELLED = "Cancelled";
  public static final String CHARGE_PARTIAL_REFUND = "PartialRefunded";
  public static final String CHARGE_REFUNDED = "Refunded";

  /** 发药状态 */
  public static final String DISPENSE_PENDING = "pending";
  public static final String DISPENSE_DISPENSED = "dispensed";
  public static final String DISPENSE_REJECTED = "rejected";

  /** 出入库 */
  public static final String BOUND_DRAFT = "Draft";
  public static final String BOUND_POSTED = "Posted";
  public static final String BOUND_VOIDED = "Voided";
  public static final String BOUND_PURCHASE_IN = "PurchaseIn";
  public static final String BOUND_DISPENSE_OUT = "DispenseOut";
  public static final String BOUND_STOCKTAKE = "StocktakeAdjust";

  /** 系统参数 key */
  public static final String CFG_CANCEL_HOURS = "appointment.cancel.hours";
  public static final String CFG_EXPIRE_WARN_DAYS = "inventory.expire.warn.days";
  public static final String CFG_FORCE_AUDIT_RX = "pharmacy.force.audit";
  public static final String CFG_ALLOW_NEGATIVE_STOCK = "inventory.allow.negative";
}
