<template>
  <div v-loading="bootLoading" class="consult-page">
    <header class="consult-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回队列
        </el-button>
        <div v-if="encounter" class="patient-brief">
          <strong>{{ encounter.patientName }}</strong>
          <span>{{ genderLabel(encounter.patientGender) }}</span>
          <span>{{ encounter.patientAge != null ? `${encounter.patientAge}岁` : '-' }}</span>
          <span>{{ encounter.patientMobile || '-' }}</span>
          <el-tag size="small" :type="encType(encounter.status)">{{ encLabel(encounter.status) }}</el-tag>
          <span class="muted">{{ encounter.encounterNo }} · {{ encounter.doctorName }} · {{ encounter.deptName }}</span>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="encounter?.status === 'InConsult'"
          type="success"
          :loading="finishing"
          @click="onFinish"
        >
          完成就诊
        </el-button>
      </div>
    </header>

    <el-alert
      v-if="patient?.allergyHistory"
      class="allergy-alert"
      type="warning"
      :closable="false"
      show-icon
      :title="`过敏史：${patient.allergyHistory}`"
    />

    <el-tabs v-model="tab" class="consult-tabs">
      <el-tab-pane label="病历" name="emr" />
      <el-tab-pane label="处方" name="rx" />
      <el-tab-pane label="历史病历" name="history" />
    </el-tabs>

    <!-- 病历 -->
    <div v-show="tab === 'emr'" class="panel">
      <div class="panel-toolbar">
        <el-tag size="small" :type="emr.status === 'submitted' ? 'success' : 'info'">
          {{ emr.status === 'submitted' ? '已提交' : '草稿' }}
        </el-tag>
        <div class="spacer" />
        <el-button :disabled="emrReadonly || savingEmr" :loading="savingEmr" @click="onSaveEmr">保存草稿</el-button>
        <el-button
          type="primary"
          :disabled="emrReadonly || !emr.id || submittingEmr"
          :loading="submittingEmr"
          @click="onSubmitEmr"
        >
          提交病历
        </el-button>
      </div>
      <el-form :model="emr" label-width="88px" class="emr-form" :disabled="emrReadonly">
        <el-form-item label="主诉">
          <el-input v-model="emr.chiefComplaint" type="textarea" :rows="2" placeholder="患者主要不适与持续时间" />
        </el-form-item>
        <el-form-item label="现病史">
          <el-input v-model="emr.presentIllness" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="既往史">
              <el-input v-model="emr.pastHistory" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="过敏史">
              <el-input v-model="emr.allergyHistory" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="查体">
          <el-input v-model="emr.physicalExam" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="16">
            <el-form-item label="诊断">
              <el-input v-model="emr.diagnosis" placeholder="临床诊断" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="ICD">
              <el-input v-model="emr.diagnosisCode" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="处置">
          <el-input v-model="emr.treatment" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="医嘱建议">
          <el-input v-model="emr.advice" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
    </div>

    <!-- 处方 -->
    <div v-show="tab === 'rx'" class="panel rx-panel">
      <div class="rx-layout">
        <aside class="rx-list">
          <div class="panel-toolbar">
            <span class="section-title">本诊处方</span>
            <el-button type="primary" size="small" :disabled="!canEditRx" @click="onNewRx">新开处方</el-button>
          </div>
          <el-scrollbar>
            <div
              v-for="item in rxList"
              :key="item.id"
              class="rx-item"
              :class="{ active: currentRx?.id === item.id }"
              @click="selectRx(item.id)"
            >
              <div class="rx-item-top">
                <span>{{ item.prescriptionNo }}</span>
                <el-tag size="small" :type="rxType(item.status)">{{ rxLabel(item.status) }}</el-tag>
              </div>
              <div class="rx-item-meta">
                ¥{{ formatMoney(item.totalAmount) }} · {{ formatTime(item.createTime) }}
              </div>
            </div>
            <el-empty v-if="!rxList.length" description="暂无处方" :image-size="56" />
          </el-scrollbar>
        </aside>

        <section class="rx-editor">
          <template v-if="currentRx || creatingRx">
            <div class="panel-toolbar">
              <el-tag v-if="currentRx" size="small" :type="rxType(currentRx.status)">
                {{ rxLabel(currentRx.status) }}
              </el-tag>
              <el-tag v-else size="small" type="info">新草稿</el-tag>
              <div class="spacer" />
              <el-button
                :disabled="!rxEditable || savingRx"
                :loading="savingRx"
                @click="onSaveRx"
              >
                保存草稿
              </el-button>
              <el-button
                type="primary"
                :disabled="!rxEditable || !currentRx?.id || submittingRx"
                :loading="submittingRx"
                @click="onSubmitRx"
              >
                提交处方
              </el-button>
              <el-button
                type="danger"
                plain
                :disabled="!canVoidRx || voidingRx"
                :loading="voidingRx"
                @click="onVoidRx"
              >
                作废
              </el-button>
            </div>

            <el-form label-width="72px" class="rx-meta" :disabled="!rxEditable">
              <el-form-item label="诊断">
                <el-input v-model="rxForm.diagnosis" placeholder="可带入病历诊断" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="rxForm.remark" />
              </el-form-item>
            </el-form>

            <div class="panel-toolbar">
              <span class="section-title">药品明细</span>
              <el-button size="small" :disabled="!rxEditable" @click="addRxItem">添加药品</el-button>
            </div>

            <el-table :data="rxForm.items" border size="small" class="rx-table">
              <el-table-column label="药品" min-width="180">
                <template #default="{ row, $index }">
                  <el-select
                    v-if="rxEditable"
                    v-model="row.drugId"
                    filterable
                    remote
                    clearable
                    placeholder="搜索药品"
                    :remote-method="searchDrugs"
                    :loading="drugSearching"
                    style="width: 100%"
                    @change="(id) => onPickDrug($index, id)"
                  >
                    <el-option
                      v-for="d in drugOptions"
                      :key="d.id"
                      :label="`${d.drugName}${d.spec ? ' / ' + d.spec : ''}`"
                      :value="d.id"
                    />
                  </el-select>
                  <span v-else>{{ row.drugName }}{{ row.spec ? ` / ${row.spec}` : '' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="用法" width="110">
                <template #default="{ row }">
                  <el-select v-if="rxEditable" v-model="row.usageMethod" placeholder="用法" style="width: 100%">
                    <el-option v-for="u in usageOptions" :key="u" :label="u" :value="u" />
                  </el-select>
                  <span v-else>{{ row.usageMethod || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="剂量" width="90">
                <template #default="{ row }">
                  <el-input v-if="rxEditable" v-model="row.dosage" placeholder="如 1片" />
                  <span v-else>{{ row.dosage || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="频次" width="120">
                <template #default="{ row }">
                  <el-select v-if="rxEditable" v-model="row.frequency" placeholder="频次" style="width: 100%">
                    <el-option v-for="f in freqOptions" :key="f" :label="f" :value="f" />
                  </el-select>
                  <span v-else>{{ row.frequency || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="天数" width="80">
                <template #default="{ row }">
                  <el-input-number
                    v-if="rxEditable"
                    v-model="row.days"
                    :min="1"
                    :controls="false"
                    style="width: 100%"
                  />
                  <span v-else>{{ row.days ?? '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="90">
                <template #default="{ row }">
                  <el-input-number
                    v-if="rxEditable"
                    v-model="row.quantity"
                    :min="0.01"
                    :precision="2"
                    :controls="false"
                    style="width: 100%"
                    @change="recalcItem(row)"
                  />
                  <span v-else>{{ row.quantity }}</span>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="90">
                <template #default="{ row }">¥{{ formatMoney(row.unitPrice) }}</template>
              </el-table-column>
              <el-table-column label="金额" width="90">
                <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
              </el-table-column>
              <el-table-column v-if="rxEditable" label="" width="60" fixed="right">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removeRxItem($index)">删</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="rx-total">合计：¥{{ formatMoney(rxTotal) }}</div>
          </template>
          <el-empty v-else description="请选择或新开处方" :image-size="72" />
        </section>
      </div>
    </div>

    <!-- 历史病历 -->
    <div v-show="tab === 'history'" class="panel">
      <el-table v-loading="historyLoading" :data="historyList" stripe>
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="diagnosis" label="诊断" min-width="160" />
        <el-table-column prop="chiefComplaint" label="主诉" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'submitted' ? 'success' : 'info'">
              {{ row.status === 'submitted' ? '已提交' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openHistory(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="historyVisible" title="历史病历" width="640px" destroy-on-close>
      <el-descriptions v-if="historyDetail" :column="1" border size="small">
        <el-descriptions-item label="诊断">{{ historyDetail.diagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主诉">{{ historyDetail.chiefComplaint || '-' }}</el-descriptions-item>
        <el-descriptions-item label="现病史">{{ historyDetail.presentIllness || '-' }}</el-descriptions-item>
        <el-descriptions-item label="既往史">{{ historyDetail.pastHistory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过敏史">{{ historyDetail.allergyHistory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="查体">{{ historyDetail.physicalExam || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处置">{{ historyDetail.treatment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="医嘱">{{ historyDetail.advice || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { finishEncounter, getEncounterDetail } from '@/api/encounter'
import { getEmrByEncounter, historyEmrByPatient, saveEmr, submitEmr } from '@/api/emr'
import {
  getPrescriptionDetail,
  pagePrescriptions,
  savePrescription,
  submitPrescription,
  voidPrescription,
} from '@/api/prescription'
import { getPatientDetail } from '@/api/patient'
import { pageDrugs } from '@/api/master'

const route = useRoute()
const router = useRouter()

const bootLoading = ref(false)
const finishing = ref(false)
const encounter = ref(null)
const patient = ref(null)
const tab = ref('emr')

const emr = reactive(emptyEmr())
const savingEmr = ref(false)
const submittingEmr = ref(false)

const rxList = ref([])
const currentRx = ref(null)
const creatingRx = ref(false)
const rxForm = reactive({ diagnosis: '', remark: '', items: [] })
const savingRx = ref(false)
const submittingRx = ref(false)
const voidingRx = ref(false)
const drugOptions = ref([])
const drugSearching = ref(false)

const historyList = ref([])
const historyLoading = ref(false)
const historyVisible = ref(false)
const historyDetail = ref(null)

const usageOptions = ['口服', '外用', '含服', '雾化吸入', '肌肉注射', '静脉注射', '静脉滴注']
const freqOptions = ['一日一次', '一日两次', '一日三次', '一日四次', '必要时', '每晚一次']

const emrReadonly = computed(
  () => emr.status === 'submitted' || encounter.value?.status === 'Finished'
)
const canEditRx = computed(() => encounter.value?.status === 'InConsult')
const rxEditable = computed(() => {
  if (!canEditRx.value) return false
  if (creatingRx.value && !currentRx.value) return true
  return currentRx.value?.status === 'Draft'
})
const canVoidRx = computed(() => {
  if (!currentRx.value?.id) return false
  return currentRx.value.status === 'Draft' || currentRx.value.status === 'Submitted'
})
const rxTotal = computed(() =>
  (rxForm.items || []).reduce((sum, row) => sum + Number(row.amount || 0), 0)
)

function emptyEmr() {
  return {
    id: '',
    encounterId: '',
    patientId: '',
    doctorId: '',
    doctorName: '',
    chiefComplaint: '',
    presentIllness: '',
    pastHistory: '',
    allergyHistory: '',
    physicalExam: '',
    diagnosis: '',
    diagnosisCode: '',
    treatment: '',
    advice: '',
    status: 'draft',
  }
}

function genderLabel(g) {
  if (g === 'M' || g === '男') return '男'
  if (g === 'F' || g === '女') return '女'
  return g || '-'
}
function encLabel(s) {
  return { Waiting: '候诊', InConsult: '接诊中', Skipped: '过号', Finished: '已完成' }[s] || s
}
function encType(s) {
  return { Waiting: 'warning', InConsult: 'primary', Skipped: 'info', Finished: 'success' }[s] || ''
}
function rxLabel(s) {
  return {
    Draft: '草稿',
    Submitted: '已提交',
    Charged: '已收费',
    Dispensing: '发药中',
    Dispensed: '已发药',
    Voided: '已作废',
    Refunded: '已退费',
    Returned: '已退药',
  }[s] || s
}
function rxType(s) {
  return {
    Draft: 'info',
    Submitted: 'warning',
    Charged: 'success',
    Dispensed: 'success',
    Voided: 'danger',
  }[s] || ''
}
function formatMoney(v) {
  const n = Number(v || 0)
  return n.toFixed(2)
}
function formatTime(v) {
  return v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-'
}

function goBack() {
  router.push('/outpatient')
}

function applyEmr(data) {
  Object.assign(emr, emptyEmr(), data || {})
  if (!emr.allergyHistory && patient.value?.allergyHistory) {
    emr.allergyHistory = patient.value.allergyHistory
  }
  if (!emr.pastHistory && patient.value?.pastHistory) {
    emr.pastHistory = patient.value.pastHistory
  }
}

function blankRxItem() {
  return {
    drugId: '',
    drugCode: '',
    drugName: '',
    spec: '',
    unit: '',
    dosage: '',
    frequency: '一日三次',
    days: 3,
    quantity: 1,
    unitPrice: 0,
    amount: 0,
    usageMethod: '口服',
  }
}

function resetRxForm(detail) {
  rxForm.diagnosis = detail?.diagnosis || emr.diagnosis || ''
  rxForm.remark = detail?.remark || ''
  rxForm.items = (detail?.items || []).map((it) => ({
    ...blankRxItem(),
    ...it,
    quantity: Number(it.quantity || 0),
    unitPrice: Number(it.unitPrice || 0),
    amount: Number(it.amount || 0),
    days: it.days ?? 3,
  }))
  if (!rxForm.items.length) rxForm.items = [blankRxItem()]
}

async function loadAll() {
  const id = route.params.id
  if (!id) return
  bootLoading.value = true
  try {
    const encRes = await getEncounterDetail(id)
    encounter.value = encRes.data || encRes

    const [patientRes, emrRes] = await Promise.all([
      getPatientDetail(encounter.value.patientId),
      getEmrByEncounter(id),
    ])
    patient.value = patientRes.data || patientRes
    const emrData = emrRes?.data
    if (emrData?.id) {
      applyEmr(emrData)
    } else {
      applyEmr({
        encounterId: encounter.value.id,
        patientId: encounter.value.patientId,
        doctorId: encounter.value.doctorId,
        doctorName: encounter.value.doctorName,
        allergyHistory: patient.value?.allergyHistory || '',
        pastHistory: patient.value?.pastHistory || '',
        status: 'draft',
      })
    }
    await loadRxList()
    await loadHistory()
  } finally {
    bootLoading.value = false
  }
}

async function loadRxList(preferId) {
  if (!encounter.value?.id) return
  const res = await pagePrescriptions({
    encounterId: encounter.value.id,
    currentPage: 1,
    pageSize: 50,
  })
  const data = res.data || res
  rxList.value = data.records || []
  const targetId = preferId || currentRx.value?.id
  if (targetId && rxList.value.some((r) => r.id === targetId)) {
    await selectRx(targetId)
  } else if (rxList.value.length) {
    await selectRx(rxList.value[0].id)
  } else {
    currentRx.value = null
    creatingRx.value = false
    resetRxForm(null)
  }
}

async function loadHistory() {
  if (!encounter.value?.patientId) return
  historyLoading.value = true
  try {
    const res = await historyEmrByPatient(encounter.value.patientId)
    const list = res.data || res || []
    historyList.value = list.filter((h) => h.encounterId !== encounter.value.id)
  } finally {
    historyLoading.value = false
  }
}

async function onSaveEmr() {
  savingEmr.value = true
  try {
    const payload = {
      ...emr,
      encounterId: encounter.value.id,
      patientId: encounter.value.patientId,
      doctorId: encounter.value.doctorId,
      doctorName: encounter.value.doctorName,
    }
    const res = await saveEmr(payload)
    applyEmr(res.data || res)
    ElMessage.success('病历草稿已保存')
  } finally {
    savingEmr.value = false
  }
}

async function onSubmitEmr() {
  if (!emr.id) {
    await onSaveEmr()
  }
  if (!emr.id) return
  await ElMessageBox.confirm('提交后病历不可再修改，确认提交？', '提交病历', { type: 'warning' })
  submittingEmr.value = true
  try {
    await submitEmr(emr.id)
    emr.status = 'submitted'
    ElMessage.success('病历已提交')
  } finally {
    submittingEmr.value = false
  }
}

function onNewRx() {
  creatingRx.value = true
  currentRx.value = null
  resetRxForm({ diagnosis: emr.diagnosis || '', items: [blankRxItem()] })
}

async function selectRx(id) {
  creatingRx.value = false
  const res = await getPrescriptionDetail(id)
  currentRx.value = res.data || res
  resetRxForm(currentRx.value)
  drugOptions.value = (currentRx.value.items || [])
    .filter((it) => it.drugId)
    .map((it) => ({
      id: it.drugId,
      drugName: it.drugName,
      drugCode: it.drugCode,
      spec: it.spec,
      unit: it.unit,
      salePrice: it.unitPrice,
    }))
}

function addRxItem() {
  rxForm.items.push(blankRxItem())
}

function removeRxItem(index) {
  rxForm.items.splice(index, 1)
  if (!rxForm.items.length) rxForm.items.push(blankRxItem())
}

function recalcItem(row) {
  const qty = Number(row.quantity || 0)
  const price = Number(row.unitPrice || 0)
  row.amount = Number((qty * price).toFixed(2))
}

async function searchDrugs(keyword) {
  if (!keyword) {
    drugOptions.value = []
    return
  }
  drugSearching.value = true
  try {
    const res = await pageDrugs({
      keyword,
      status: 'active',
      currentPage: 1,
      pageSize: 20,
    })
    const data = res.data || res
    drugOptions.value = data.records || []
  } finally {
    drugSearching.value = false
  }
}

function onPickDrug(index, drugId) {
  const row = rxForm.items[index]
  const drug = drugOptions.value.find((d) => d.id === drugId)
  if (!drug) return
  row.drugId = drug.id
  row.drugCode = drug.drugCode
  row.drugName = drug.drugName
  row.spec = drug.spec
  row.unit = drug.unit
  row.unitPrice = Number(drug.salePrice || 0)
  if (!row.quantity) row.quantity = 1
  recalcItem(row)
}

async function onSaveRx() {
  const items = rxForm.items.filter((it) => it.drugId)
  if (!items.length) {
    ElMessage.warning('请至少添加一种药品')
    return
  }
  items.forEach(recalcItem)
  savingRx.value = true
  try {
    const payload = {
      id: currentRx.value?.id,
      encounterId: encounter.value.id,
      patientId: encounter.value.patientId,
      patientName: encounter.value.patientName,
      doctorId: encounter.value.doctorId,
      doctorName: encounter.value.doctorName,
      deptName: encounter.value.deptName,
      rxType: 'western',
      diagnosis: rxForm.diagnosis,
      remark: rxForm.remark,
      items,
    }
    const res = await savePrescription(payload)
    const saved = res.data || res
    ElMessage.success('处方草稿已保存')
    creatingRx.value = false
    await loadRxList(saved.id)
  } finally {
    savingRx.value = false
  }
}

async function onSubmitRx() {
  if (!currentRx.value?.id) {
    ElMessage.warning('请先保存处方草稿')
    return
  }
  await ElMessageBox.confirm('提交后进入待收费，确认提交？', '提交处方', { type: 'warning' })
  submittingRx.value = true
  try {
    await submitPrescription(currentRx.value.id)
    ElMessage.success('处方已提交')
    await loadRxList(currentRx.value.id)
  } finally {
    submittingRx.value = false
  }
}

async function onVoidRx() {
  if (!currentRx.value?.id) return
  await ElMessageBox.confirm('确认作废该处方？', '作废处方', { type: 'warning' })
  voidingRx.value = true
  try {
    await voidPrescription(currentRx.value.id)
    ElMessage.success('处方已作废')
    await loadRxList()
  } finally {
    voidingRx.value = false
  }
}

function openHistory(row) {
  historyDetail.value = row
  historyVisible.value = true
}

async function onFinish() {
  await ElMessageBox.confirm('确认完成本次就诊？', '完成就诊', { type: 'warning' })
  finishing.value = true
  try {
    await finishEncounter(encounter.value.id)
    ElMessage.success('就诊已完成')
    router.push('/outpatient')
  } finally {
    finishing.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.consult-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin: 10px;
  padding: 0 12px 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid var(--clinic-border);
}

.consult-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--clinic-border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.patient-brief {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.patient-brief .muted,
.muted {
  color: var(--clinic-muted);
}

.allergy-alert {
  margin-top: 10px;
}

.consult-tabs {
  margin-top: 4px;
}

.panel {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding-bottom: 8px;
}

.panel-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.spacer {
  flex: 1;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
}

.emr-form {
  max-width: 960px;
}

.rx-panel {
  overflow: hidden;
  display: flex;
}

.rx-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 12px;
  width: 100%;
  min-height: 420px;
  height: 100%;
}

.rx-list {
  border: 1px solid var(--clinic-border);
  border-radius: 8px;
  padding: 8px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.rx-item {
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 6px;
}

.rx-item:hover {
  background: #f5f7fa;
}

.rx-item.active {
  background: rgba(43, 182, 115, 0.12);
  box-shadow: inset 3px 0 0 var(--clinic-primary);
}

.rx-item-top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
}

.rx-item-meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--clinic-muted);
}

.rx-editor {
  border: 1px solid var(--clinic-border);
  border-radius: 8px;
  padding: 10px;
  min-width: 0;
  overflow: auto;
}

.rx-meta {
  max-width: 720px;
}

.rx-table {
  width: 100%;
}

.rx-total {
  margin-top: 10px;
  text-align: right;
  font-weight: 600;
}

@media (max-width: 960px) {
  .rx-layout {
    grid-template-columns: 1fr;
  }
}
</style>
