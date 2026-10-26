package com.framework.backend.prescription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.framework.backend.prescription.entity.PrescriptionItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {}
