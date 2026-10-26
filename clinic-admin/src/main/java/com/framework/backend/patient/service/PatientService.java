package com.framework.backend.patient.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.patient.entity.Patient;
import com.framework.backend.patient.entity.PatientLog;

public interface PatientService extends IService<Patient> {

  void add(Patient patient);

  void updatePatient(Patient patient);

  MyPage<Patient> pageList(Patient patient);

  Patient getDetail(String id);

  void addLog(PatientLog log);
}
