package com.framework.backend.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.emr.entity.Emr;
import java.util.List;

public interface EmrService extends IService<Emr> {

  Emr saveDraft(Emr emr);

  void submit(String id);

  Emr getByEncounter(String encounterId);

  List<Emr> historyByPatient(String patientId);
}
