package com.framework.backend.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.framework.backend.inventory.entity.StocktakeItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StocktakeItemMapper extends BaseMapper<StocktakeItem> {}
