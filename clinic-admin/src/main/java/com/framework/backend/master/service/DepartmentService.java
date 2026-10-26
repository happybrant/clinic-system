package com.framework.backend.master.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.common.MyPage;
import com.framework.backend.master.entity.Department;
import java.util.List;

public interface DepartmentService extends IService<Department> {

  void add(Department department);

  void updateDepartment(Department department);

  MyPage<Department> pageList(Department department);

  Department getOne(String id);

  List<Department> listActive();

  void delete(String id);
}
