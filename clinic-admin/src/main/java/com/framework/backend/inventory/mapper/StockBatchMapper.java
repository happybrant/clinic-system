package com.framework.backend.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.framework.backend.inventory.entity.StockBatch;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockBatchMapper extends BaseMapper<StockBatch> {}
