package com.framework.backend.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.system.entity.ClinicConfig;
import com.framework.backend.system.mapper.ClinicConfigMapper;
import com.framework.backend.system.service.ClinicConfigService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ClinicConfigServiceImpl extends ServiceImpl<ClinicConfigMapper, ClinicConfig>
    implements ClinicConfigService {

  @Override
  public ClinicConfig getByKey(String configKey) {
    if (StringUtils.isBlank(configKey)) {
      throw new BusinessException("参数键不能为空");
    }
    QueryWrapper<ClinicConfig> qw = new QueryWrapper<>();
    qw.lambda().eq(ClinicConfig::getConfigKey, configKey);
    return getOne(qw, false);
  }

  @Override
  public void updateConfig(ClinicConfig config) {
    if (config == null || StringUtils.isBlank(config.getId())) {
      throw new BusinessException("参数id不能为空");
    }
    updateById(config);
  }

  @Override
  public List<ClinicConfig> listAll() {
    QueryWrapper<ClinicConfig> qw = new QueryWrapper<>();
    qw.lambda().orderByAsc(ClinicConfig::getConfigKey);
    return list(qw);
  }
}
