package com.framework.backend.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.framework.backend.common.exception.BusinessException;
import com.framework.backend.system.entity.ClinicSerial;
import com.framework.backend.system.mapper.ClinicSerialMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务单据号生成：前缀 + yyyyMMdd + 4 位流水。
 */
@Service
public class SerialNoService {

  private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
  private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

  @Autowired private ClinicSerialMapper clinicSerialMapper;

  @Transactional(rollbackFor = Exception.class)
  public String next(String prefix) {
    String day = LocalDate.now().format(DAY);
    String serialKey = prefix + ":" + day;
    ReentrantLock lock = locks.computeIfAbsent(serialKey, k -> new ReentrantLock());
    lock.lock();
    try {
      QueryWrapper<ClinicSerial> qw = new QueryWrapper<>();
      qw.lambda().eq(ClinicSerial::getSerialKey, serialKey);
      ClinicSerial serial = clinicSerialMapper.selectOne(qw);
      int nextVal;
      if (serial == null) {
        serial = new ClinicSerial();
        serial.setSerialKey(serialKey);
        serial.setCurrentVal(1);
        clinicSerialMapper.insert(serial);
        nextVal = 1;
      } else {
        nextVal = serial.getCurrentVal() + 1;
        serial.setCurrentVal(nextVal);
        clinicSerialMapper.updateById(serial);
      }
      return prefix + day + String.format("%04d", nextVal);
    } catch (Exception e) {
      throw new BusinessException("生成单据号失败：" + e.getMessage());
    } finally {
      lock.unlock();
    }
  }
}
