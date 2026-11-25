<template>
  <div class="charge-page">
    <el-tabs v-model="tab" @tab-change="onTabChange">
      <el-tab-pane label="待收费处方" name="pending" />
      <el-tab-pane label="收费单" name="sheets" />
    </el-tabs>

    <!-- 待收费：已提交处方 -->
    <div v-show="tab === 'pending'" class="panel">
      <div class="toolbar">
        <el-input
          v-model="pendingQuery.keyword"
          clearable
          placeholder="处方号/患者"
          style="width: 220px"
          @keyup.enter="loadPending"
        />
        <el-button type="primary" @click="loadPending">查询</el-button>
      </div>
      <el-table v-loading="pendingLoading" :data="pendingRows" stripe height="100%">
        <el-table-column prop="prescriptionNo" label="处方号" width="150" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="deptName" label="科室" width="100" />
        <el-table-column prop="diagnosis" label="诊断" min-width="140" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="药品金额" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.submitTime || row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :loading="creatingId === row.id" @click="onCreateAndPay(row)">
              生成并收银
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="pendingPage.currentPage"
          v-model:page-size="pendingPage.pageSize"
          layout="total, prev, pager, next"
          :total="pendingPage.total"
          @current-change="loadPending"
        />
      </div>
    </div>

    <!-- 收费单列表 -->
    <div v-show="tab === 'sheets'" class="panel">
      <div class="toolbar">
        <el-select v-model="sheetQuery.status" clearable placeholder="状态" style="width: 140px">
          <el-option label="未支付" value="Unpaid" />
          <el-option label="已支付" value="Paid" />
          <el-option label="已取消" value="Cancelled" />
        </el-select>
        <el-input
          v-model="sheetQuery.keyword"
          clearable
          placeholder="单号/患者"
          style="width: 220px"
          @keyup.enter="loadSheets"
        />
        <el-button type="primary" @click="loadSheets">查询</el-button>
      </div>
      <el-table v-loading="sheetLoading" :data="sheetRows" stripe height="100%">
        <el-table-column prop="chargeNo" label="收费单号" width="150" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="totalAmount" label="应收" width="100">
          <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="实收" width="100">
          <template #default="{ row }">¥{{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100">
          <template #default="{ row }">{{ payMethodLabel(row.payMethod) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="chargeType(row.status)">{{ chargeLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170">
          <template #default="{ row }">{{ formatTime(row.payTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'Unpaid'" link type="success" @click="openPay(row)">收银</el-button>
            <el-button v-if="row.status === 'Unpaid'" link type="danger" @click="onCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="sheetPage.currentPage"
          v-model:page-size="sheetPage.pageSize"
          layout="total, prev, pager, next"
          :total="sheetPage.total"
          @current-change="loadSheets"
        />
      </div>
    </div>

    <!-- 收银 -->
    <el-dialog v-model="payVisible" title="收银" width="480px" destroy-on-close>
      <div v-if="payTarget" class="pay-summary">
        <div>收费单：{{ payTarget.chargeNo }}</div>
        <div>患者：{{ payTarget.patientName }}</div>
        <div class="amount">应收：¥{{ formatMoney(payTarget.totalAmount) }}</div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="支付方式" required>
          <el-radio-group v-model="payForm.payMethod">
            <el-radio value="cash">现金</el-radio>
            <el-radio value="wechat">微信</el-radio>
            <el-radio value="alipay">支付宝</el-radio>
            <el-radio value="card">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="payForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="收费单详情" width="720px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="收费单号">{{ detail.chargeNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ chargeLabel(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
          <el-descriptions-item label="处方号">{{ detail.prescriptionNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="应收">¥{{ formatMoney(detail.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="实收">¥{{ formatMoney(detail.paidAmount) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payMethodLabel(detail.payMethod) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatTime(detail.payTime) }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items || []" border size="small" style="margin-top: 12px">
          <el-table-column prop="itemType" label="类型" width="90">
            <template #default="{ row }">{{ itemTypeLabel(row.itemType) }}</template>
          </el-table-column>
          <el-table-column prop="itemName" label="项目" min-width="160" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="unitPrice" label="单价" width="90">
            <template #default="{ row }">¥{{ formatMoney(row.unitPrice) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="90">
            <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { pagePrescriptions } from '@/api/prescription'
import {
  cancelCharge,
  createChargeFromPrescription,
  getChargeDetail,
  pageCharges,
  payCharge,
} from '@/api/charge'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const tab = ref('pending')

const pendingQuery = reactive({ keyword: '' })
const pendingRows = ref([])
const pendingLoading = ref(false)
const pendingPage = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const creatingId = ref('')

const sheetQuery = reactive({ keyword: '', status: '' })
const sheetRows = ref([])
const sheetLoading = ref(false)
const sheetPage = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const payVisible = ref(false)
const payTarget = ref(null)
const payForm = reactive({ payMethod: 'wechat', remark: '' })
const paying = ref(false)

const detailVisible = ref(false)
const detail = ref(null)

function formatMoney(v) {
  return Number(v || 0).toFixed(2)
}
function formatTime(v) {
  return v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-'
}
function chargeLabel(s) {
  return {
    Unpaid: '未支付',
    Paid: '已支付',
    Cancelled: '已取消',
    PartialRefunded: '部分退费',
    Refunded: '已退费',
  }[s] || s
}
function chargeType(s) {
  return { Unpaid: 'warning', Paid: 'success', Cancelled: 'info', Refunded: 'danger' }[s] || ''
}
function payMethodLabel(m) {
  return { cash: '现金', wechat: '微信', alipay: '支付宝', card: '银行卡', mix: '混合' }[m] || m || '-'
}
function itemTypeLabel(t) {
  return { register: '挂号', consult: '诊疗', drug: '药品', other: '其他' }[t] || t
}

async function loadPending() {
  pendingLoading.value = true
  try {
    const res = await pagePrescriptions({
      status: 'Submitted',
      keyword: pendingQuery.keyword || undefined,
      currentPage: pendingPage.currentPage,
      pageSize: pendingPage.pageSize,
    })
    const data = res.data || res
    pendingRows.value = data.records || []
    pendingPage.total = data.total || 0
  } finally {
    pendingLoading.value = false
  }
}

async function loadSheets() {
  sheetLoading.value = true
  try {
    const res = await pageCharges({
      status: sheetQuery.status || undefined,
      keyword: sheetQuery.keyword || undefined,
      currentPage: sheetPage.currentPage,
      pageSize: sheetPage.pageSize,
    })
    const data = res.data || res
    sheetRows.value = data.records || []
    sheetPage.total = data.total || 0
  } finally {
    sheetLoading.value = false
  }
}

function onTabChange(name) {
  if (name === 'pending') loadPending()
  else loadSheets()
}

async function onCreateAndPay(row) {
  creatingId.value = row.id
  try {
    const res = await createChargeFromPrescription(row.id)
    const sheet = res.data || res
    ElMessage.success('收费单已生成')
    await loadPending()
    openPay(sheet)
  } finally {
    creatingId.value = ''
  }
}

function openPay(row) {
  payTarget.value = row
  payForm.payMethod = 'wechat'
  payForm.remark = ''
  payVisible.value = true
}

async function confirmPay() {
  if (!payTarget.value?.id) return
  if (!payForm.payMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }
  paying.value = true
  try {
    await payCharge({
      id: payTarget.value.id,
      payMethod: payForm.payMethod,
      remark: payForm.remark,
      cashierName: userStore.displayName,
    })
    ElMessage.success('收款成功，已进入待发药')
    payVisible.value = false
    tab.value = 'sheets'
    await loadSheets()
    await loadPending()
  } finally {
    paying.value = false
  }
}

async function openDetail(row) {
  const res = await getChargeDetail(row.id)
  detail.value = res.data || res
  detailVisible.value = true
}

async function onCancel(row) {
  await ElMessageBox.confirm('确认取消该未支付收费单？', '取消收费单', { type: 'warning' })
  await cancelCharge(row.id)
  ElMessage.success('已取消')
  await loadSheets()
}

onMounted(() => {
  loadPending()
})
</script>

<style scoped>
.charge-page {
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

.panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
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

.pay-summary {
  margin-bottom: 12px;
  line-height: 1.8;
  font-size: 14px;
}

.pay-summary .amount {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 600;
  color: var(--clinic-primary);
}
</style>
