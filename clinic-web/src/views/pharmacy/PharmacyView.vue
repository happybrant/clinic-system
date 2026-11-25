<template>
  <div class="pharm-page">
    <div class="toolbar">
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px" @change="loadList">
        <el-option label="待发药" value="pending" />
        <el-option label="已发药" value="dispensed" />
        <el-option label="已驳回" value="rejected" />
      </el-select>
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="发药单/处方号/患者"
        style="width: 220px"
        @keyup.enter="loadList"
      />
      <el-button type="primary" @click="loadList">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe height="100%">
      <el-table-column prop="dispenseNo" label="发药单号" width="150" />
      <el-table-column prop="prescriptionNo" label="处方号" width="150" />
      <el-table-column prop="patientName" label="患者" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="pharmacistName" label="药师" width="100" />
      <el-table-column prop="dispenseTime" label="发药时间" width="170">
        <template #default="{ row }">{{ formatTime(row.dispenseTime) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'pending'" link type="success" @click="openDispense(row)">发药</el-button>
          <el-button v-if="row.status === 'pending'" link type="warning" @click="openReject(row)">驳回/缺药</el-button>
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

    <el-dialog v-model="detailVisible" title="发药单详情" width="760px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="发药单号">{{ detail.dispenseNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="处方号">{{ detail.prescriptionNo }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ detail.diagnosis || '-' }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ detail.doctorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="药师">{{ detail.pharmacistName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审方">{{ detail.audited ? '已审方' : '未审方' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.rejectReason" label="驳回原因" :span="2">
            {{ detail.rejectReason }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="section-title">处方药品</div>
        <el-table :data="previewItems" border size="small">
          <el-table-column prop="drugName" label="药品" min-width="140" />
          <el-table-column prop="spec" label="规格" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="unit" label="单位" width="70" />
          <el-table-column prop="usageMethod" label="用法" width="90" />
          <el-table-column prop="frequency" label="频次" width="100" />
        </el-table>

        <template v-if="detail.items?.length">
          <div class="section-title">实发批次</div>
          <el-table :data="detail.items" border size="small">
            <el-table-column prop="drugName" label="药品" min-width="140" />
            <el-table-column prop="batchNo" label="批次" width="120" />
            <el-table-column prop="quantity" label="数量" width="90" />
            <el-table-column prop="unit" label="单位" width="70" />
          </el-table>
        </template>
      </template>
      <template v-if="detail?.status === 'pending'" #footer>
        <el-button @click="onApprove">审方通过</el-button>
        <el-button type="warning" @click="openReject(detail)">驳回/缺药</el-button>
        <el-button type="primary" :loading="dispensing" @click="confirmDispense(detail)">确认发药</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回 / 缺药" width="460px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="如：缺药、用法不当等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="confirmReject">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  approveDispense,
  doDispense,
  getDispenseDetail,
  pageDispenses,
  rejectDispense,
} from '@/api/pharmacy'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const query = reactive({ status: 'pending', keyword: '' })
const rows = ref([])
const loading = ref(false)
const page = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const detailVisible = ref(false)
const detail = ref(null)
const dispensing = ref(false)

const rejectVisible = ref(false)
const rejectTarget = ref(null)
const rejectReason = ref('')
const rejecting = ref(false)

const previewItems = computed(() => {
  if (!detail.value) return []
  if (detail.value.prescriptionItems?.length) return detail.value.prescriptionItems
  return detail.value.items || []
})

function formatTime(v) {
  return v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-'
}
function statusLabel(s) {
  return { pending: '待发药', dispensed: '已发药', rejected: '已驳回' }[s] || s
}
function statusType(s) {
  return { pending: 'warning', dispensed: 'success', rejected: 'danger' }[s] || ''
}

async function loadList() {
  loading.value = true
  try {
    const res = await pageDispenses({
      status: query.status || undefined,
      keyword: query.keyword || undefined,
      currentPage: page.currentPage,
      pageSize: page.pageSize,
    })
    const data = res.data || res
    rows.value = data.records || []
    page.total = data.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  const res = await getDispenseDetail(row.id)
  detail.value = res.data || res
  detailVisible.value = true
}

async function openDispense(row) {
  await openDetail(row)
}

async function onApprove() {
  if (!detail.value?.id) return
  await approveDispense({
    id: detail.value.id,
    pharmacistName: userStore.displayName,
  })
  ElMessage.success('审方已通过')
  const res = await getDispenseDetail(detail.value.id)
  detail.value = res.data || res
  await loadList()
}

function openReject(row) {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写原因')
    return
  }
  rejecting.value = true
  try {
    await rejectDispense({
      id: rejectTarget.value.id,
      rejectReason: rejectReason.value.trim(),
      pharmacistName: userStore.displayName,
    })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    detailVisible.value = false
    await loadList()
  } finally {
    rejecting.value = false
  }
}

async function confirmDispense(row) {
  await ElMessageBox.confirm('确认按处方整单发药并扣减库存？', '发药确认', { type: 'warning' })
  dispensing.value = true
  try {
    await doDispense({
      id: row.id,
      pharmacistName: userStore.displayName,
    })
    ElMessage.success('发药成功')
    detailVisible.value = false
    await loadList()
  } finally {
    dispensing.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.pharm-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin: 10px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid var(--clinic-border);
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
}

.section-title {
  margin: 14px 0 8px;
  font-weight: 600;
  font-size: 14px;
}
</style>
