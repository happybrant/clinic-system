package com.framework.backend.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.framework.backend.master.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {}
