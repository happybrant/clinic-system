package com.framework.backend.encounter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.encounter.entity.Encounter;

public interface EncounterService extends IService<Encounter> {

  /** 创建 Waiting 就诊（预约签到 / 内部复用） */
  Encounter createWaiting(Encounter encounter);

  Encounter registerWalkIn(Encounter encounter);

  MyPage<Encounter> pageList(Encounter encounter);

  Encounter callNext(Encounter encounter);

  void start(String id);

  void skip(String id);

  void finish(String id);

  Encounter getDetail(String id);
}
