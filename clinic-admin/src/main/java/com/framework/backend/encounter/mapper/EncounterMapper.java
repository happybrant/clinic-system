package com.framework.backend.encounter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.framework.backend.encounter.entity.Encounter;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EncounterMapper extends BaseMapper<Encounter> {}
