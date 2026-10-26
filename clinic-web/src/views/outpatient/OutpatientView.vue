<template>
  <div class="out-page">
    <div class="toolbar">
      <el-select v-model="doctorId" placeholder="选择医生" style="width: 200px" @change="loadQueue">
        <el-option v-for="d in doctors" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="status" clearable placeholder="状态" style="width: 140px" @change="loadQueue">
        <el-option label="候诊" value="Waiting" />
        <el-option label="接诊中" value="InConsult" />
        <el-option label="过号" value="Skipped" />
        <el-option label="已完成" value="Finished" />
      </el-select>
      <el-button type="primary" @click="loadQueue">刷新</el-button>
      <el-button type="success" :disabled="!doctorId" :loading="calling" @click="onCallNext">叫号下一位</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe height="100%">
      <el-table-column prop="queueNo" label="序号" width="80" />
      <el-table-column prop="encounterNo" label="就诊号" width="150" />
      <el-table-column prop="patientName" label="患者" width="100" />
      <el-table-column prop="patientGender" label="性别" width="70">
        <template #default="{ row }">{{ genderLabel(row.patientGender) }}</template>
      </el-table-column>
      <el-table-column prop="patientAge" label="年龄" width="70" />
      <el-table-column prop="patientMobile" label="手机" width="120" />
      <el-table-column prop="deptName" label="科室" width="100" />
      <el-table-column prop="registerType" label="来源" width="90">
        <template #default="{ row }">
          {{ row.registerType === 'walkin' ? '现场' : '预约' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="encType(row.status)" size="small">{{ encLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'Waiting' || row.status === 'Skipped'"
            link
            type="primary"
            @click="onStart(row)"
          >
            接诊
          </el-button>
          <el-button v-if="row.status === 'Waiting' || row.status === 'InConsult'" link type="warning" @click="onSkip(row)">
            过号
          </el-button>
          <el-button v-if="row.status === 'InConsult'" link type="success" @click="onFinish(row)">完成</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDoctors } from '@/api/master'
import {
  callNext,
  finishEncounter,
  pageEncounters,
  skipEncounter,
  startEncounter,
} from '@/api/encounter'

const doctors = ref([])
const doctorId = ref('')
const status = ref('Waiting')
const rows = ref([])
const loading = ref(false)
const calling = ref(false)

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

async function loadDoctors() {
  const res = await listDoctors()
  doctors.value = res.data || res || []
  if (!doctorId.value && doctors.value.length) {
    doctorId.value = doctors.value[0].id
  }
}

async function loadQueue() {
  loading.value = true
  try {
    const res = await pageEncounters({
      doctorId: doctorId.value || undefined,
      status: status.value || undefined,
      currentPage: 1,
      pageSize: 100,
    })
    const data = res.data || res
    rows.value = data.records || []
  } finally {
    loading.value = false
  }
}

async function onCallNext() {
  if (!doctorId.value) return
  calling.value = true
  try {
    await callNext({ doctorId: doctorId.value })
    ElMessage.success('已叫号并进入接诊')
    status.value = ''
    await loadQueue()
  } finally {
    calling.value = false
  }
}

async function onStart(row) {
  await startEncounter(row.id)
  ElMessage.success('开始接诊')
  await loadQueue()
}

async function onSkip(row) {
  await skipEncounter(row.id)
  ElMessage.success('已过号')
  await loadQueue()
}

async function onFinish(row) {
  await finishEncounter(row.id)
  ElMessage.success('就诊完成')
  await loadQueue()
}

onMounted(async () => {
  await loadDoctors()
  await loadQueue()
})
</script>

<style scoped>
.out-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  background: #fff;
  margin: 10px;
  border-radius: 8px;
  border: 1px solid var(--clinic-border);
  min-height: 0;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
</style>
