package com.framework.backend.master.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.ClinicConstants;
import com.framework.backend.common.MyPage;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.master.entity.Department;
import com.framework.backend.master.mapper.DepartmentMapper;
import com.framework.backend.master.service.DepartmentService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
    implements DepartmentService {

  @Override
  public void add(Department department) {
    if (department == null || StringUtils.isBlank(department.getName())) {
      throw new BusinessException("科室名称不能为空");
    }
    if (StringUtils.isBlank(department.getCode())) {
      throw new BusinessException("科室编码不能为空");
    }
    checkCodeUnique(department.getCode(), null);
    if (StringUtils.isBlank(department.getStatus())) {
      department.setStatus(ClinicConstants.STATUS_ACTIVE);
    }
    if (department.getSort() == null) {
      department.setSort(0);
    }
    save(department);
  }

  @Override
  public void updateDepartment(Department department) {
    if (department == null || StringUtils.isBlank(department.getId())) {
      throw new BusinessException("科室id不能为空");
    }
    Department exist = getById(department.getId());
    if (exist == null) {
      throw new BusinessException("科室不存在");
    }
    if (StringUtils.isNotBlank(department.getCode())) {
      checkCodeUnique(department.getCode(), department.getId());
    }
    updateById(department);
  }

  @Override
  public MyPage<Department> pageList(Department department) {
    QueryWrapper<Department> qw = new QueryWrapper<>();
    if (department != null && StringUtils.isNotBlank(department.getKeyword())) {
      String kw = department.getKeyword().trim();
      qw.lambda()
          .and(w -> w.like(Department::getName, kw).or().like(Department::getCode, kw));
    }
    if (department != null && StringUtils.isNotBlank(department.getStatus())) {
      qw.lambda().eq(Department::getStatus, department.getStatus());
    }
    qw.lambda().orderByAsc(Department::getSort).orderByDesc(Department::getCreateTime);
    long current =
        department != null && department.getCurrentPage() != null
            ? department.getCurrentPage()
            : 1L;
    long size =
        department != null && department.getPageSize() != null ? department.getPageSize() : 10L;
    MyPage<Department> page = new MyPage<>(current, size);
    return baseMapper.selectPage(page, qw);
  }

  @Override
  public Department getOne(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("科室id不能为空");
    }
    Department department = getById(id);
    if (department == null) {
      throw new BusinessException("科室不存在");
    }
    return department;
  }

  @Override
  public List<Department> listActive() {
    QueryWrapper<Department> qw = new QueryWrapper<>();
    qw.lambda()
        .eq(Department::getStatus, ClinicConstants.STATUS_ACTIVE)
        .orderByAsc(Department::getSort)
        .orderByAsc(Department::getName);
    return list(qw);
  }

  @Override
  public void delete(String id) {
    if (StringUtils.isBlank(id)) {
      throw new BusinessException("科室id不能为空");
    }
    Department exist = getById(id);
    if (exist == null) {
      throw new BusinessException("科室不存在");
    }
    Department update = new Department();
    update.setId(id);
    update.setStatus(ClinicConstants.STATUS_DISABLED);
    updateById(update);
  }

  private void checkCodeUnique(String code, String excludeId) {
    QueryWrapper<Department> qw = new QueryWrapper<>();
    qw.lambda().eq(Department::getCode, code);
    if (StringUtils.isNotBlank(excludeId)) {
      qw.lambda().ne(Department::getId, excludeId);
    }
    if (count(qw) > 0) {
      throw new BusinessException("科室编码已存在");
    }
  }
}
