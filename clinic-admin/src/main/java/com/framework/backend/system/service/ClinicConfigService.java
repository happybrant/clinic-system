package com.framework.backend.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.framework.backend.system.entity.ClinicConfig;
import java.util.List;

public interface ClinicConfigService extends IService<ClinicConfig> {

  ClinicConfig getByKey(String configKey);

  void updateConfig(ClinicConfig config);

  List<ClinicConfig> listAll();
}
