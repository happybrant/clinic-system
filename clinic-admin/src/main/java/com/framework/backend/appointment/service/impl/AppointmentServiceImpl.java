package com.framework.backend.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.appointment.entity.Appointment;
import com.framework.backend.appointment.mapper.AppointmentMapper;
import com.framework.backend.appointment.service.AppointmentService;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.SerialNoService;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.encounter.entity.Encounter;
import com.framework.backend.encounter.service.EncounterService;
import com.framework.backend.patient.entity.Patient;
import com.framework.backend.patient.mapper.PatientMapper;
import com.framework.backend.schedule.entity.ScheduleSlot;
import com.framework.backend.schedule.mapper.ScheduleSlotMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment>
    implements AppointmentService {

  @Autowired private SerialNoService serialNoService;
  @Autowired private ScheduleSlotMapper scheduleSlotMapper;
  @Autowired private PatientMapper patientMapper;
  @Autowired private EncounterService encounterService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Appointment book(Appointment appointment) {
    if (appointment == null) {
      throw new BusinessException("预约信息不能为空");
    }
    if (StringUtils.isBlank(appointment.getPatientId())) {
      throw new BusinessException("患者不能为空");
    }
    if (StringUtils.isBlank(appointment.getSlotId())) {
      throw new BusinessException("号源不能为空");
    }

    ScheduleSlot slot = scheduleSlotMapper.selectById(appointment.getSlotId());
    if (slot == null) {
      throw new BusinessException("号源不存在");
    }
    if (!"open".equals(slot.getStatus())) {
      throw new BusinessException("号源未开放");
    }
    int used = slot.getUsedQuota() == null ? 0 : slot.getUsedQuota();
    int total = slot.getTotalQuota() == null ? 0 : slot.getTotalQuota();
    if (used >= total) {
      throw new BusinessException("号源已满");
    }

    QueryWrapper<Appointment> dupQw = new QueryWrapper<>();
    dupQw
        .lambda()
        .eq(Appointment::getPatientId, appointment.getPatientId())
        .eq(Appointment::getDoctorId, slot.getDoctorId())
        .eq(Appointment::getScheduleDate, slot.getScheduleDate())
        .eq(Appointment::getTimePeriod, slot.getTimePeriod())
        .in(
            Appointment::getStatus,
            Arrays.asList(ClinicConstants.APPT_BOOKED, ClinicConstants.APPT_CHECKED_IN));
    if (count(dupQw) > 0) {
      throw new BusinessException("同一患者在该医生该时段已有有效预约");
    }

    Patient patient = patientMapper.selectById(appointment.getPatientId());
    if (patient == null) {
      throw new BusinessException("患者不存在");
    }

    appointment.setAppointmentNo(serialNoService.next(ClinicConstants.PREFIX_APPOINTMENT));
    appointment.setPatientName(
        StringUtils.isNotBlank(appointment.getPatientName())
            ? appointment.getPatientName()
            : patient.getName());
    appointment.setPatientMobile(
        StringUtils.isNotBlank(appointment.getPatientMobile())
            ? appointment.getPatientMobile()
            : patient.getMobile());
    appointment.setScheduleDate(slot.getScheduleDate());
    appointment.setDeptId(slot.getDeptId());
    appointment.setDeptName(slot.getDeptName());
    appointment.setDoctorId(slot.getDoctorId());
    appointment.setDoctorName(slot.getDoctorName());
    appointment.setTimePeriod(slot.getTimePeriod());
    appointment.setStartTime(slot.getStartTime());
    appointment.setEndTime(slot.getEndTime());
    appointment.setStatus(ClinicConstants.APPT_BOOKED);
    save(appointment);

    slot.setUsedQuota(used + 1);
    scheduleSlotMapper.updateById(slot);
    return appointment;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cancel(Appointment appointment) {
    if (appointment == null || StringUtils.isBlank(appointment.getId())) {
      throw new BusinessException("预约id不能为空");
    }
    Appointment exist = getById(appointment.getId());
    if (exist == null) {
      throw new BusinessException("预约不存在");
    }
    if (ClinicConstants.APPT_CANCELLED.equals(exist.getStatus())) {
      throw new BusinessException("预约已取消");
    }
    if (ClinicConstants.APPT_CHECKED_IN.equals(exist.getStatus())) {
      throw new BusinessException("已签到预约不可取消");
    }

    boolean wasBooked = ClinicConstants.APPT_BOOKED.equals(exist.getStatus());
    exist.setStatus(ClinicConstants.APPT_CANCELLED);
    if (StringUtils.isNotBlank(appointment.getRemark())) {
      exist.setRemark(appointment.getRemark());
    }
    updateById(exist);

    if (wasBooked && StringUtils.isNotBlank(exist.getSlotId())) {
      ScheduleSlot slot = scheduleSlotMapper.selectById(exist.getSlotId());
      if (slot != null) {
        int used = slot.getUsedQuota() == null ? 0 : slot.getUsedQuota();
        slot.setUsedQuota(Math.max(0, used - 1));
        scheduleSlotMapper.updateById(slot);
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Appointment checkIn(Appointment appointment) {
    if (appointment == null || StringUtils.isBlank(appointment.getId())) {
      throw new BusinessException("预约id不能为空");
    }
    Appointment exist = getById(appointment.getId());
    if (exist == null) {
      throw new BusinessException("预约不存在");
    }
    if (!ClinicConstants.APPT_BOOKED.equals(exist.getStatus())) {
      throw new BusinessException("仅已预约状态可签到");
    }

    exist.setStatus(ClinicConstants.APPT_CHECKED_IN);
    exist.setCheckinTime(LocalDateTime.now());
    updateById(exist);

    Encounter encounter = new Encounter();
    encounter.setPatientId(exist.getPatientId());
    encounter.setPatientName(exist.getPatientName());
    encounter.setPatientMobile(exist.getPatientMobile());
    encounter.setDeptId(exist.getDeptId());
    encounter.setDeptName(exist.getDeptName());
    encounter.setDoctorId(exist.getDoctorId());
    encounter.setDoctorName(exist.getDoctorName());
    encounter.setAppointmentId(exist.getId());
    encounter.setRegisterType("appointment");
    encounterService.createWaiting(encounter);

    return exist;
  }

  @Override
  public MyPage<Appointment> pageList(Appointment appointment) {
    QueryWrapper<Appointment> qw = new QueryWrapper<>();
    if (appointment != null) {
      if (appointment.getScheduleDate() != null) {
        qw.lambda().eq(Appointment::getScheduleDate, appointment.getScheduleDate());
      }
      if (StringUtils.isNotBlank(appointment.getStatus())) {
        qw.lambda().eq(Appointment::getStatus, appointment.getStatus());
      }
      if (StringUtils.isNotBlank(appointment.getPatientId())) {
        qw.lambda().eq(Appointment::getPatientId, appointment.getPatientId());
      }
      if (StringUtils.isNotBlank(appointment.getDoctorId())) {
        qw.lambda().eq(Appointment::getDoctorId, appointment.getDoctorId());
      }
      if (StringUtils.isNotBlank(appointment.getKeyword())) {
        String kw = appointment.getKeyword().trim();
        qw.lambda()
            .and(
                w ->
                    w.like(Appointment::getPatientName, kw)
                        .or()
                        .like(Appointment::getPatientMobile, kw)
                        .or()
                        .like(Appointment::getAppointmentNo, kw)
                        .or()
                        .like(Appointment::getDoctorName, kw));
      }
    }
    qw.lambda().orderByDesc(Appointment::getCreateTime);
    long current =
        appointment != null && appointment.getCurrentPage() != null
            ? appointment.getCurrentPage()
            : 1L;
    long size =
        appointment != null && appointment.getPageSize() != null ? appointment.getPageSize() : 10L;
    MyPage<Appointment> page = new MyPage<>(current, size);
    return baseMapper.selectPage(page, qw);
  }

  @Override
  public Appointment getDetail(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("预约id不能为空");
    }
    Appointment appointment = getById(id);
    if (appointment == null) {
      throw new BusinessException("预约不存在");
    }
    return appointment;
  }
}
