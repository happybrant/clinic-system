package com.framework.backend.master.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.master.entity.Department;
import com.framework.backend.master.entity.Doctor;
import com.framework.backend.master.mapper.DoctorMapper;
import com.framework.backend.master.service.DepartmentService;
import com.framework.backend.master.service.DoctorService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

  @Autowired private DepartmentService departmentService;

  @Override
  public void add(Doctor doctor) {
    if (doctor == null || StringUtils.isBlank(doctor.getName())) {
      throw new BusinessException("医生姓名不能为空");
    }
    fillDeptName(doctor);
    if (StringUtils.isBlank(doctor.getStatus())) {
      doctor.setStatus(ClinicConstants.STATUS_ACTIVE);
    }
    if (doctor.getCanSchedule() == null) {
      doctor.setCanSchedule(true);
    }
    save(doctor);
  }

  @Override
  public void updateDoctor(Doctor doctor) {
    if (doctor == null || StringUtils.isBlank(doctor.getId())) {
      throw new BusinessException("医生id不能为空");
    }
    Doctor exist = getById(doctor.getId());
    if (exist == null) {
      throw new BusinessException("医生不存在");
    }
    fillDeptName(doctor);
    updateById(doctor);
  }

  @Override
  public MyPage<Doctor> pageList(Doctor doctor) {
    QueryWrapper<Doctor> qw = new QueryWrapper<>();
    if (doctor != null && StringUtils.isNotBlank(doctor.getKeyword())) {
      String kw = doctor.getKeyword().trim();
      qw.lambda()
          .and(
              w ->
                  w.like(Doctor::getName, kw)
                      .or()
                      .like(Doctor::getEmpNo, kw)
                      .or()
                      .like(Doctor::getMobile, kw));
    }
    if (doctor != null && StringUtils.isNotBlank(doctor.getDeptId())) {
      qw.lambda().eq(Doctor::getDeptId, doctor.getDeptId());
    }
    if (doctor != null && StringUtils.isNotBlank(doctor.getStatus())) {
      qw.lambda().eq(Doctor::getStatus, doctor.getStatus());
    }
    qw.lambda().orderByDesc(Doctor::getCreateTime);
    long current =
        doctor != null && doctor.getCurrentPage() != null ? doctor.getCurrentPage() : 1L;
    long size = doctor != null && doctor.getPageSize() != null ? doctor.getPageSize() : 10L;
    MyPage<Doctor> page = new MyPage<>(current, size);
    return baseMapper.selectPage(page, qw);
  }

  @Override
  public Doctor getOne(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("医生id不能为空");
    }
    Doctor doctor = getById(id);
    if (doctor == null) {
      throw new BusinessException("医生不存在");
    }
    return doctor;
  }

  @Override
  public List<Doctor> listActive(String deptId) {
    QueryWrapper<Doctor> qw = new QueryWrapper<>();
    qw.lambda().eq(Doctor::getStatus, ClinicConstants.STATUS_ACTIVE);
    if (StringUtils.isNotBlank(deptId)) {
      qw.lambda().eq(Doctor::getDeptId, deptId);
    }
    qw.lambda().orderByAsc(Doctor::getName);
    return list(qw);
  }

  @Override
  public void delete(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("医生id不能为空");
    }
    Doctor exist = getById(id);
    if (exist == null) {
      throw new BusinessException("医生不存在");
    }
    Doctor update = new Doctor();
    update.setId(id);
    update.setStatus(ClinicConstants.STATUS_DISABLED);
    updateById(update);
  }

  private void fillDeptName(Doctor doctor) {
    if (StringUtils.isBlank(doctor.getDeptId())) {
      return;
    }
    if (StringUtils.isNotBlank(doctor.getDeptName())) {
      return;
    }
    Department dept = departmentService.getById(doctor.getDeptId());
    if (dept != null) {
      doctor.setDeptName(dept.getName());
    }
  }
}
