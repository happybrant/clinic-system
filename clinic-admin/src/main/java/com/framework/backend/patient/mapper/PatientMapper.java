package com.framework.backend.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.framework.backend.patient.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {}
