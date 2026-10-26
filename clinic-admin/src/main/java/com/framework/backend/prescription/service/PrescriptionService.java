package com.framework.backend.prescription.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.prescription.entity.Prescription;

public interface PrescriptionService extends IService<Prescription> {

  Prescription saveDraft(Prescription prescription);

  void submit(String id);

  void voidRx(String id);

  MyPage<Prescription> pageList(Prescription query);

  Prescription getDetail(String id);
}
