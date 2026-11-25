package com.framework.backend.charge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.charge.entity.ChargeSheet;
import com.framework.backend.common.MyPage;

public interface ChargeService extends IService<ChargeSheet> {

  /** 按已提交处方生成待收费单（可附带未收挂号费） */
  ChargeSheet createFromPrescription(String prescriptionId);

  /** 收银 */
  ChargeSheet pay(ChargeSheet request);

  /** 取消未支付收费单 */
  void cancel(String id);

  MyPage<ChargeSheet> pageList(ChargeSheet query);

  ChargeSheet getDetail(String id);
}
