package com.framework.backend.emr.controller;

import com.framework.backend.annotation.Log;
import com.framework.backend.common.result.ResponseResult;
import com.framework.backend.emr.entity.Emr;
import com.framework.backend.emr.service.EmrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseResult
@RequestMapping("/clinic/emr")
@Tag(name = "EmrController", description = "电子病历")
public class EmrController {

  @Autowired private EmrService emrService;

  @Log(value = "保存病历草稿", module = "电子病历")
  @Operation(description = "按就诊保存/更新草稿病历")
  @PostMapping("/save")
  public Emr save(@RequestBody Emr emr) {
    return emrService.saveDraft(emr);
  }

  @Log(value = "提交病历", module = "电子病历")
  @Operation(description = "提交病历")
  @PostMapping("/submit/{id}")
  public void submit(@PathVariable String id) {
    emrService.submit(id);
  }

  @Operation(description = "按就诊查询病历")
  @GetMapping("/getByEncounter/{encounterId}")
  public Emr getByEncounter(@PathVariable String encounterId) {
    return emrService.getByEncounter(encounterId);
  }

  @Operation(description = "患者病历历史")
  @GetMapping("/historyByPatient/{patientId}")
  public List<Emr> historyByPatient(@PathVariable String patientId) {
    return emrService.historyByPatient(patientId);
  }
}
