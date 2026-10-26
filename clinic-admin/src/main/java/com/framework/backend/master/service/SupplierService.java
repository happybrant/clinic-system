package com.framework.backend.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.master.entity.Supplier;
import java.util.List;

public interface SupplierService extends IService<Supplier> {

  void add(Supplier supplier);

  void updateSupplier(Supplier supplier);

  MyPage<Supplier> pageList(Supplier supplier);

  Supplier getOne(String id);

  List<Supplier> listActive();

  void delete(String id);
}
