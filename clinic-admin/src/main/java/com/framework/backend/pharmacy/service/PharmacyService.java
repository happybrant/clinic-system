package com.framework.backend.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.pharmacy.entity.Dispense;

public interface PharmacyService extends IService<Dispense> {

  MyPage<Dispense> pageList(Dispense query);

  Dispense getDetail(String id);

  /** 审方通过（强制审方开启时，发药前需先通过） */
  void approve(Dispense request);

  /** 审方驳回 / 缺药挂起（状态 rejected） */
  void reject(Dispense request);

  /** 发药并按 FEFO 扣减库存 */
  Dispense dispense(Dispense request);
}
