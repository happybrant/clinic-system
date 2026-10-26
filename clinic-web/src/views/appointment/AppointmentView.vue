<template>
  <div class="appt-page">
    <el-tabs v-model="tab">
      <el-tab-pane label="今日预约" name="list" />
      <el-tab-pane label="预约挂号" name="book" />
      <el-tab-pane label="现场挂号" name="walkin" />
      <el-tab-pane label="号源维护" name="slot" />
    </el-tabs>

    <!-- 今日预约 -->
    <div v-show="tab === 'list'" class="panel">
      <div class="toolbar">
        <el-date-picker v-model="query.scheduleDate" type="date" value-format="YYYY-MM-DD" />
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
          <el-option label="已预约" value="Booked" />
          <el-option label="已签到" value="CheckedIn" />
          <el-option label="已取消" value="Cancelled" />
        </el-select>
        <el-input v-model="query.keyword" clearable placeholder="患者姓名/手机" style="width: 200px" @keyup.enter="loadList" />
        <el-button type="primary" @click="loadList">查询</el-button>
      </div>
      <el-table v-loading="listLoading" :data="rows" stripe height="100%">
        <el-table-column prop="appointmentNo" label="预约号" width="150" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="patientMobile" label="手机" width="120" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="deptName" label="科室" width="100" />
        <el-table-column prop="timePeriod" label="时段" width="90" />
        <el-table-column label="时间" min-width="140">
          <template #default="{ row }">{{ formatSlotTime(row) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'Booked'" link type="primary" @click="onCheckIn(row)">到诊</el-button>
            <el-button v-if="row.status === 'Booked'" link type="danger" @click="onCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="page.currentPage"
          v-model:page-size="page.pageSize"
          layout="total, prev, pager, next"
          :total="page.total"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 预约挂号 -->
    <div v-show="tab === 'book'" class="panel form-panel">
      <el-form :model="bookForm" label-width="90px" style="max-width: 640px">
        <el-form-item label="就诊日期">
          <el-date-picker v-model="bookForm.date" type="date" value-format="YYYY-MM-DD" @change="loadSlots" />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="bookForm.deptId" clearable placeholder="全部" style="width: 100%" @change="onDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="bookForm.doctorId" clearable placeholder="全部" style="width: 100%" @change="loadSlots">
            <el-option v-for="d in doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="号源">
          <el-radio-group v-model="bookForm.slotId" class="slot-group">
            <el-radio
              v-for="s in slots"
              :key="s.id"
              :value="s.id"
              :disabled="(s.usedQuota || 0) >= (s.totalQuota || 0)"
              border
            >
              {{ s.doctorName }} · {{ s.timePeriod }}
              {{ formatHm(s.startTime) }}-{{ formatHm(s.endTime) }}
              （余{{ Math.max(0, (s.totalQuota || 0) - (s.usedQuota || 0)) }}）
            </el-radio>
          </el-radio-group>
          <el-empty v-if="!slots.length" description="当日暂无可预约号源" :image-size="60" />
        </el-form-item>
        <el-form-item label="患者">
          <el-select
            v-model="bookForm.patientId"
            filterable
            remote
            clearable
            placeholder="输入姓名/手机搜索"
            :remote-method="searchPatients"
            :loading="patientSearching"
            style="width: 100%"
          >
            <el-option
              v-for="p in patientOptions"
              :key="p.id"
              :label="`${p.name}（${p.mobile || '无手机'}）`"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="bookForm.source" style="width: 100%">
            <el-option label="前台" value="front" />
            <el-option label="电话" value="phone" />
            <el-option label="线上" value="online" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="booking" @click="onBook">确认预约</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 现场挂号 -->
    <div v-show="tab === 'walkin'" class="panel form-panel">
      <el-form :model="walkForm" label-width="90px" style="max-width: 520px">
        <el-form-item label="患者">
          <el-select
            v-model="walkForm.patientId"
            filterable
            remote
            clearable
            placeholder="输入姓名/手机搜索"
            :remote-method="searchPatients"
            :loading="patientSearching"
            style="width: 100%"
          >
            <el-option
              v-for="p in patientOptions"
              :key="p.id"
              :label="`${p.name}（${p.mobile || '无手机'}）`"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="walkForm.deptId" style="width: 100%" @change="onWalkDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="walkForm.doctorId" style="width: 100%">
            <el-option v-for="d in walkDoctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="walking" @click="onWalkIn">进入候诊</el-button>
          <el-button @click="$router.push('/outpatient')">查看候诊队列</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 号源维护 -->
    <div v-show="tab === 'slot'" class="panel">
      <div class="toolbar">
        <el-date-picker v-model="slotQuery.scheduleDate" type="date" value-format="YYYY-MM-DD" />
        <el-button type="primary" @click="loadSlotList">查询</el-button>
        <el-button type="success" @click="openSlotDialog">新增号源</el-button>
      </div>
      <el-table v-loading="slotLoading" :data="slotRows" stripe>
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="deptName" label="科室" width="100" />
        <el-table-column prop="timePeriod" label="时段" width="90" />
        <el-table-column label="时间" min-width="140">
          <template #default="{ row }">{{ formatHm(row.startTime) }}-{{ formatHm(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="号源" width="100">
          <template #default="{ row }">{{ row.usedQuota || 0 }}/{{ row.totalQuota || 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'open' ? 'success' : 'info'" size="small">
              {{ row.status === 'open' ? '开放' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'open'" link type="primary" @click="toggleSlot(row, true)">开放</el-button>
            <el-button v-else link type="warning" @click="toggleSlot(row, false)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="slotDialog" title="新增号源" width="520px" destroy-on-close>
      <el-form :model="slotForm" label-width="90px">
        <el-form-item label="日期">
          <el-date-picker v-model="slotForm.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="slotForm.deptId" style="width: 100%" @change="onSlotDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="slotForm.doctorId" style="width: 100%" @change="onSlotDoctorChange">
            <el-option v-for="d in slotDoctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="slotForm.timePeriod" style="width: 100%">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
            <el-option label="晚上" value="晚上" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始">
          <el-time-picker v-model="slotForm.startTime" value-format="HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束">
          <el-time-picker v-model="slotForm.endTime" value-format="HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="号源数">
          <el-input-number v-model="slotForm.totalQuota" :min="1" :max="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="slotDialog = false">取消</el-button>
        <el-button type="primary" :loading="slotSaving" @click="saveSlot">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  bookAppointment,
  cancelAppointment,
  checkInAppointment,
  pageAppointments,
} from '@/api/appointment'
import { addSchedule, availableSlots, closeSchedule, openSchedule, pageSchedules } from '@/api/schedule'
import { listDepartments, listDoctors } from '@/api/master'
import { pagePatients } from '@/api/patient'
import { walkIn } from '@/api/encounter'

const tab = ref('list')
const listLoading = ref(false)
const rows = ref([])
const page = reactive({ currentPage: 1, pageSize: 20, total: 0 })
const query = reactive({
  scheduleDate: dayjs().format('YYYY-MM-DD'),
  status: '',
  keyword: '',
})

const departments = ref([])
const doctors = ref([])
const walkDoctors = ref([])
const slotDoctors = ref([])
const slots = ref([])
const patientOptions = ref([])
const patientSearching = ref(false)

const bookForm = reactive({
  date: dayjs().format('YYYY-MM-DD'),
  deptId: '',
  doctorId: '',
  slotId: '',
  patientId: '',
  source: 'front',
})
const booking = ref(false)

const walkForm = reactive({
  patientId: '',
  deptId: '',
  doctorId: '',
})
const walking = ref(false)

const slotQuery = reactive({ scheduleDate: dayjs().format('YYYY-MM-DD') })
const slotRows = ref([])
const slotLoading = ref(false)
const slotDialog = ref(false)
const slotSaving = ref(false)
const slotForm = reactive({
  scheduleDate: dayjs().format('YYYY-MM-DD'),
  deptId: '',
  deptName: '',
  doctorId: '',
  doctorName: '',
  timePeriod: '上午',
  startTime: '08:00:00',
  endTime: '12:00:00',
  totalQuota: 20,
})

function statusLabel(s) {
  return { Booked: '已预约', CheckedIn: '已签到', Cancelled: '已取消', NoShow: '爽约' }[s] || s
}
function statusType(s) {
  return { Booked: 'warning', CheckedIn: 'success', Cancelled: 'info' }[s] || ''
}
function formatHm(t) {
  if (!t) return ''
  return String(t).slice(0, 5)
}
function formatSlotTime(row) {
  return `${formatHm(row.startTime)}-${formatHm(row.endTime)}`
}

async function loadList() {
  listLoading.value = true
  try {
    const res = await pageAppointments({
      scheduleDate: query.scheduleDate,
      status: query.status || undefined,
      keyword: query.keyword || undefined,
      currentPage: page.currentPage,
      pageSize: page.pageSize,
    })
    const data = res.data || res
    rows.value = data.records || []
    page.total = Number(data.total || 0)
  } finally {
    listLoading.value = false
  }
}

async function loadMaster() {
  const [deptRes, docRes] = await Promise.all([listDepartments(), listDoctors()])
  departments.value = deptRes.data || deptRes || []
  doctors.value = docRes.data || docRes || []
  walkDoctors.value = doctors.value
  slotDoctors.value = doctors.value
}

async function onDeptChange(deptId) {
  bookForm.doctorId = ''
  const res = await listDoctors(deptId || undefined)
  doctors.value = res.data || res || []
  await loadSlots()
}

async function onWalkDeptChange(deptId) {
  walkForm.doctorId = ''
  const res = await listDoctors(deptId || undefined)
  walkDoctors.value = res.data || res || []
}

async function onSlotDeptChange(deptId) {
  slotForm.doctorId = ''
  slotForm.doctorName = ''
  const dept = departments.value.find((d) => d.id === deptId)
  slotForm.deptName = dept?.name || ''
  const res = await listDoctors(deptId || undefined)
  slotDoctors.value = res.data || res || []
}

function onSlotDoctorChange(doctorId) {
  const doc = slotDoctors.value.find((d) => d.id === doctorId)
  slotForm.doctorName = doc?.name || ''
  if (doc?.deptId && !slotForm.deptId) {
    slotForm.deptId = doc.deptId
    slotForm.deptName = doc.deptName || ''
  }
}

async function loadSlots() {
  bookForm.slotId = ''
  if (!bookForm.date) {
    slots.value = []
    return
  }
  const res = await availableSlots(bookForm.date, bookForm.doctorId || undefined)
  slots.value = res.data || res || []
}

async function searchPatients(kw) {
  if (!kw) {
    patientOptions.value = []
    return
  }
  patientSearching.value = true
  try {
    const res = await pagePatients({ keyword: kw, currentPage: 1, pageSize: 20 })
    const data = res.data || res
    patientOptions.value = data.records || []
  } finally {
    patientSearching.value = false
  }
}

async function onBook() {
  if (!bookForm.patientId || !bookForm.slotId) {
    ElMessage.warning('请选择患者和号源')
    return
  }
  booking.value = true
  try {
    await bookAppointment({
      patientId: bookForm.patientId,
      slotId: bookForm.slotId,
      source: bookForm.source,
    })
    ElMessage.success('预约成功')
    tab.value = 'list'
    await loadList()
  } finally {
    booking.value = false
  }
}

async function onCheckIn(row) {
  await checkInAppointment({ id: row.id })
  ElMessage.success('到诊成功，已进入候诊')
  await loadList()
}

async function onCancel(row) {
  await ElMessageBox.confirm(`确认取消预约「${row.patientName}」？`, '提示')
  await cancelAppointment({ id: row.id })
  ElMessage.success('已取消')
  await loadList()
}

async function onWalkIn() {
  if (!walkForm.patientId || !walkForm.doctorId) {
    ElMessage.warning('请选择患者和医生')
    return
  }
  const doctor = walkDoctors.value.find((d) => d.id === walkForm.doctorId)
  const dept = departments.value.find((d) => d.id === walkForm.deptId)
  walking.value = true
  try {
    await walkIn({
      patientId: walkForm.patientId,
      doctorId: walkForm.doctorId,
      doctorName: doctor?.name,
      deptId: walkForm.deptId || doctor?.deptId,
      deptName: dept?.name || doctor?.deptName,
    })
    ElMessage.success('已进入候诊队列')
  } finally {
    walking.value = false
  }
}

async function loadSlotList() {
  slotLoading.value = true
  try {
    const res = await pageSchedules({
      scheduleDate: slotQuery.scheduleDate,
      currentPage: 1,
      pageSize: 100,
    })
    const data = res.data || res
    slotRows.value = data.records || []
  } finally {
    slotLoading.value = false
  }
}

function openSlotDialog() {
  Object.assign(slotForm, {
    scheduleDate: dayjs().format('YYYY-MM-DD'),
    deptId: '',
    deptName: '',
    doctorId: '',
    doctorName: '',
    timePeriod: '上午',
    startTime: '08:00:00',
    endTime: '12:00:00',
    totalQuota: 20,
  })
  slotDoctors.value = doctors.value
  slotDialog.value = true
}

async function saveSlot() {
  if (!slotForm.doctorId || !slotForm.scheduleDate) {
    ElMessage.warning('请完善号源信息')
    return
  }
  slotSaving.value = true
  try {
    await addSchedule({ ...slotForm })
    ElMessage.success('号源已创建')
    slotDialog.value = false
    await loadSlotList()
  } finally {
    slotSaving.value = false
  }
}

async function toggleSlot(row, open) {
  if (open) await openSchedule(row.id)
  else await closeSchedule(row.id)
  ElMessage.success(open ? '已开放' : '已关闭')
  await loadSlotList()
}

onMounted(async () => {
  await loadMaster()
  await loadList()
  await loadSlots()
  await loadSlotList()
})
</script>

<style scoped>
.appt-page {
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

.panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.form-panel {
  padding-top: 8px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.pager {
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.slot-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.slot-group :deep(.el-radio) {
  margin-right: 0;
}
</style>
