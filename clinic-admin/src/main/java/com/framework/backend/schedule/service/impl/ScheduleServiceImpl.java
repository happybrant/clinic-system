package com.framework.backend.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.schedule.entity.ScheduleSlot;
import com.framework.backend.schedule.mapper.ScheduleSlotMapper;
import com.framework.backend.schedule.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleSlotMapper, ScheduleSlot>
    implements ScheduleService {

  private static final String STATUS_OPEN = "open";
  private static final String STATUS_CLOSED = "closed";

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void add(ScheduleSlot slot) {
    if (slot == null) {
      throw new BusinessException("号源不能为空");
    }
    if (StringUtils.isBlank(slot.getDoctorId())) {
      throw new BusinessException("医生不能为空");
    }
    if (slot.getScheduleDate() == null) {
      throw new BusinessException("排班日期不能为空");
    }
    if (slot.getTotalQuota() == null || slot.getTotalQuota() <= 0) {
      throw new BusinessException("号源总量必须大于0");
    }
    if (slot.getUsedQuota() == null) {
      slot.setUsedQuota(0);
    }
    slot.setStatus(STATUS_OPEN);
    save(slot);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateSlot(ScheduleSlot slot) {
    if (slot == null || StringUtils.isBlank(slot.getId())) {
      throw new BusinessException("号源id不能为空");
    }
    ScheduleSlot exist = getById(slot.getId());
    if (exist == null) {
      throw new BusinessException("号源不存在");
    }
    if (slot.getTotalQuota() != null && slot.getTotalQuota() <= 0) {
      throw new BusinessException("号源总量必须大于0");
    }
    if (slot.getTotalQuota() != null
        && exist.getUsedQuota() != null
        && slot.getTotalQuota() < exist.getUsedQuota()) {
      throw new BusinessException("号源总量不能小于已用数量");
    }
    updateById(slot);
  }

  @Override
  public MyPage<ScheduleSlot> pageList(ScheduleSlot slot) {
    QueryWrapper<ScheduleSlot> qw = new QueryWrapper<>();
    if (slot != null) {
      if (slot.getScheduleDate() != null) {
        qw.lambda().eq(ScheduleSlot::getScheduleDate, slot.getScheduleDate());
      }
      if (StringUtils.isNotBlank(slot.getDoctorId())) {
        qw.lambda().eq(ScheduleSlot::getDoctorId, slot.getDoctorId());
      }
      if (StringUtils.isNotBlank(slot.getDeptId())) {
        qw.lambda().eq(ScheduleSlot::getDeptId, slot.getDeptId());
      }
      if (StringUtils.isNotBlank(slot.getStatus())) {
        qw.lambda().eq(ScheduleSlot::getStatus, slot.getStatus());
      }
    }
    qw.lambda()
        .orderByDesc(ScheduleSlot::getScheduleDate)
        .orderByAsc(ScheduleSlot::getStartTime);
    long current = slot != null && slot.getCurrentPage() != null ? slot.getCurrentPage() : 1L;
    long size = slot != null && slot.getPageSize() != null ? slot.getPageSize() : 10L;
    MyPage<ScheduleSlot> page = new MyPage<>(current, size);
    return baseMapper.selectPage(page, qw);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void close(String id) {
    ScheduleSlot slot = requireSlot(id);
    slot.setStatus(STATUS_CLOSED);
    updateById(slot);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void open(String id) {
    ScheduleSlot slot = requireSlot(id);
    slot.setStatus(STATUS_OPEN);
    updateById(slot);
  }

  @Override
  public List<ScheduleSlot> available(LocalDate date, String doctorId) {
    if (date == null) {
      throw new BusinessException("日期不能为空");
    }
    QueryWrapper<ScheduleSlot> qw = new QueryWrapper<>();
    qw.lambda().eq(ScheduleSlot::getScheduleDate, date).eq(ScheduleSlot::getStatus, STATUS_OPEN);
    if (StringUtils.isNotBlank(doctorId)) {
      qw.lambda().eq(ScheduleSlot::getDoctorId, doctorId);
    }
    qw.apply("used_quota < total_quota");
    qw.lambda().orderByAsc(ScheduleSlot::getStartTime);
    return list(qw);
  }

  private ScheduleSlot requireSlot(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("号源id不能为空");
    }
    ScheduleSlot slot = getById(id);
    if (slot == null) {
      throw new BusinessException("号源不存在");
    }
    return slot;
  }
}
