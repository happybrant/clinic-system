package com.framework.backend.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.master.entity.ChargeItem;
import java.util.List;

public interface ChargeItemService extends IService<ChargeItem> {

  void add(ChargeItem chargeItem);

  void updateChargeItem(ChargeItem chargeItem);

  MyPage<ChargeItem> pageList(ChargeItem chargeItem);

  ChargeItem getOne(String id);

  List<ChargeItem> listActive();

  void delete(String id);
}
