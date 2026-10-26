package com.framework.backend.schedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.schedule.entity.ScheduleSlot;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleService extends IService<ScheduleSlot> {

  void add(ScheduleSlot slot);

  void updateSlot(ScheduleSlot slot);

  MyPage<ScheduleSlot> pageList(ScheduleSlot slot);

  void close(String id);

  void open(String id);

  List<ScheduleSlot> available(LocalDate date, String doctorId);
}
