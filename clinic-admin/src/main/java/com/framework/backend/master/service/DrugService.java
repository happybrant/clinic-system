package com.framework.backend.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.master.entity.Drug;

public interface DrugService extends IService<Drug> {

  void add(Drug drug);

  void updateDrug(Drug drug);

  MyPage<Drug> pageList(Drug drug);

  Drug getOne(String id);

  void delete(String id);
}
