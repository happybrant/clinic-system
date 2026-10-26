package com.framework.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 诊所管理系统启动类。主类必须位于 com.framework.backend 包下，以便扫描框架组件。
 */
@SpringBootApplication
public class ClinicAdminApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClinicAdminApplication.class, args);
  }
}
