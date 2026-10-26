package com.framework.backend.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.master.entity.Doctor;
import java.util.List;

public interface DoctorService extends IService<Doctor> {

  void add(Doctor doctor);

  void updateDoctor(Doctor doctor);

  MyPage<Doctor> pageList(Doctor doctor);

  Doctor getOne(String id);

  List<Doctor> listActive(String deptId);

  void delete(String id);
}
