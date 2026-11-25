<template>
  <div class="inv-page">
    <el-tabs v-model="tab" @tab-change="onTabChange">
      <el-tab-pane label="库存查询" name="stock" />
      <el-tab-pane label="采购入库" name="inbound" />
      <el-tab-pane label="盘点" name="stocktake" />
      <el-tab-pane label="出入库流水" name="bound" />
      <el-tab-pane label="近效期" name="expire" />
    </el-tabs>

    <div v-show="tab === 'stock'" class="panel">
      <div class="toolbar">
        <el-input
          v-model="stockQuery.drugName"
          clearable
          placeholder="药品/编码/批次"
          style="width: 220px"
          @keyup.enter="loadStock"
        />
        <el-button type="primary" @click="loadStock">查询</el-button>
      </div>
      <el-table v-loading="stockLoading" :data="stockRows" stripe height="100%">
        <el-table-column prop="drugCode" label="编码" width="120" />
        <el-table-column prop="drugName" label="药品" min-width="140" />
        <el-table-column prop="batchNo" label="批次" width="120" />
        <el-table-column prop="expireDate" label="效期" width="120" />
        <el-table-column prop="quantity" label="库存" width="90" />
        <el-table-column prop="costPrice" label="成本价" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 'active' ? '正常' : row.status }}</template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="stockPage.currentPage"
          v-model:page-size="stockPage.pageSize"
          layout="total, prev, pager, next"
          :total="stockPage.total"
          @current-change="loadStock"
        />
      </div>
    </div>

    <div v-show="tab === 'inbound'" class="panel">
      <div class="toolbar">
        <el-select v-model="inboundQuery.status" clearable placeholder="状态" style="width: 140px">
          <el-option label="草稿" value="Draft" />
          <el-option label="已过账" value="Posted" />
        </el-select>
        <el-button type="primary" @click="loadInbounds">查询</el-button>
        <el-button type="success" @click="openCreateInbound">新建入库</el-button>
      </div>
      <el-table v-loading="inboundLoading" :data="inboundRows" stripe height="100%">
        <el-table-column prop="boundNo" label="入库单号" width="150" />
        <el-table-column prop="supplierName" label="供应商" width="140" />
        <el-table-column prop="totalAmount" label="金额" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'Posted' ? 'success' : 'info'">
              {{ row.status === 'Posted' ? '已过账' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openInboundDetail(row)">详情</el-button>
            <el-button v-if="isInboundDraft(row)" link type="warning" @click="openEditInbound(row)">
              编辑
            </el-button>
            <el-button
              v-if="isInboundDraft(row)"
              link
              type="success"
              :loading="postingId === row.id"
              @click="onPost(row)"
            >
              过账
            </el-button>
            <span v-else class="posted-tip">已过账不可改</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="inboundPage.currentPage"
          v-model:page-size="inboundPage.pageSize"
          layout="total, prev, pager, next"
          :total="inboundPage.total"
          @current-change="loadInbounds"
        />
      </div>
    </div>

    <div v-show="tab === 'stocktake'" class="panel">
      <div class="toolbar">
        <el-select v-model="stocktakeQuery.status" clearable placeholder="状态" style="width: 140px">
          <el-option label="草稿" value="Draft" />
          <el-option label="已过账" value="Posted" />
        </el-select>
        <el-input
          v-model="stocktakeQuery.keyword"
          clearable
          placeholder="单号/操作人"
          style="width: 200px"
          @keyup.enter="loadStocktakes"
        />
        <el-button type="primary" @click="loadStocktakes">查询</el-button>
        <el-button type="success" @click="openCreateStocktake">新建盘点</el-button>
      </div>
      <el-table v-loading="stocktakeLoading" :data="stocktakeRows" stripe height="100%">
        <el-table-column prop="stocktakeNo" label="盘点单号" width="150" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'Posted' ? 'success' : 'info'">
              {{ row.status === 'Posted' ? '已过账' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="postTime" label="过账时间" width="170">
          <template #default="{ row }">{{ formatTime(row.postTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openStocktakeDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'Draft'"
              link
              type="success"
              :loading="stocktakePostingId === row.id"
              @click="onPostStocktake(row)"
            >
              过账
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="stocktakePage.currentPage"
          v-model:page-size="stocktakePage.pageSize"
          layout="total, prev, pager, next"
          :total="stocktakePage.total"
          @current-change="loadStocktakes"
        />
      </div>
    </div>

    <div v-show="tab === 'bound'" class="panel">
      <div class="toolbar">
        <el-select v-model="boundQuery.boundType" clearable placeholder="类型" style="width: 150px">
          <el-option label="采购入库" value="PurchaseIn" />
          <el-option label="发药出库" value="DispenseOut" />
          <el-option label="盘点调整" value="StocktakeAdjust" />
        </el-select>
        <el-select v-model="boundQuery.status" clearable placeholder="状态" style="width: 140px">
          <el-option label="草稿" value="Draft" />
          <el-option label="已过账" value="Posted" />
        </el-select>
        <el-input
          v-model="boundQuery.keyword"
          clearable
          placeholder="单号/供应商/备注"
          style="width: 200px"
          @keyup.enter="loadBounds"
        />
        <el-button type="primary" @click="loadBounds">查询</el-button>
      </div>
      <el-table v-loading="boundLoading" :data="boundRows" stripe height="100%">
        <el-table-column prop="boundNo" label="单据号" width="150" />
        <el-table-column prop="boundType" label="类型" width="110">
          <template #default="{ row }">{{ boundTypeLabel(row.boundType) }}</template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="140" />
        <el-table-column prop="totalAmount" label="金额" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'Posted' ? 'success' : 'info'">
              {{ row.status === 'Posted' ? '已过账' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="postTime" label="过账时间" width="170">
          <template #default="{ row }">{{ formatTime(row.postTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openBoundDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="boundPage.currentPage"
          v-model:page-size="boundPage.pageSize"
          layout="total, prev, pager, next"
          :total="boundPage.total"
          @current-change="loadBounds"
        />
      </div>
    </div>

    <div v-show="tab === 'expire'" class="panel">
      <el-table v-loading="expireLoading" :data="expireRows" stripe height="100%">
        <el-table-column prop="drugName" label="药品" min-width="140" />
        <el-table-column prop="batchNo" label="批次" width="120" />
        <el-table-column prop="expireDate" label="效期" width="120" />
        <el-table-column prop="quantity" label="库存" width="90" />
      </el-table>
    </div>

    <el-dialog
      v-model="createVisible"
      :title="createForm.id ? '编辑入库' : '新建入库'"
      width="820px"
      destroy-on-close
    >
      <el-form label-width="80px">
        <el-form-item label="供应商">
          <el-select
            v-model="createForm.supplierId"
            clearable
            filterable
            placeholder="可选，从供应商档案选择"
            style="width: 100%"
            @change="onSupplierPick"
          >
            <el-option
              v-for="s in suppliers"
              :key="s.id"
              :label="s.supplierName"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" />
        </el-form-item>
      </el-form>
      <div class="toolbar">
        <span class="section-title">明细</span>
        <el-button size="small" @click="addItem">添加药品</el-button>
      </div>
      <el-table :data="createForm.items" border size="small">
        <el-table-column label="药品" min-width="180">
          <template #default="{ row, $index }">
            <el-select
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
          </template>
        </el-table-column>
        <el-table-column label="批次" width="120">
          <template #default="{ row }">
            <el-input v-model="row.batchNo" />
          </template>
        </el-table-column>
        <el-table-column label="效期" width="150">
          <template #default="{ row }">
            <el-date-picker v-model="row.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="0.01" :precision="2" :controls="false" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="进价" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.unitPrice" :min="0" :precision="4" :controls="false" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="" width="60">
          <template #default="{ $index }">
            <el-button link type="danger" @click="createForm.items.splice($index, 1)">删</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button :loading="saving" @click="onSaveInbound(false)">仅保存</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveInbound(true)">保存并过账</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="boundDetailTitle" width="720px" destroy-on-close>
      <template v-if="inboundDetail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="单号">{{ inboundDetail.boundNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ boundTypeLabel(inboundDetail.boundType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ inboundDetail.status === 'Posted' ? '已过账' : '草稿' }}
          </el-descriptions-item>
          <el-descriptions-item label="供应商">{{ inboundDetail.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ formatMoney(inboundDetail.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ inboundDetail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="inboundDetail.items || []" border size="small" style="margin-top: 12px">
          <el-table-column prop="drugName" label="药品" min-width="140" />
          <el-table-column prop="batchNo" label="批次" width="120" />
          <el-table-column prop="expireDate" label="效期" width="120" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column prop="unitPrice" label="单价" width="90" />
          <el-table-column prop="amount" label="金额" width="90" />
        </el-table>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="isPurchaseInboundDraft(inboundDetail)"
          type="warning"
          @click="editFromDetail"
        >
          编辑草稿
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stocktakeCreateVisible" title="新建盘点" width="860px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="备注">
          <el-input v-model="stocktakeForm.remark" />
        </el-form-item>
      </el-form>
      <div class="toolbar">
        <span class="section-title">盘点明细</span>
        <el-button size="small" @click="addStocktakeItem">添加批次</el-button>
      </div>
      <el-table :data="stocktakeForm.items" border size="small">
        <el-table-column label="批次库存" min-width="220">
          <template #default="{ row, $index }">
            <el-select
              v-model="row.batchId"
              filterable
              remote
              clearable
              placeholder="搜索药品/批次"
              :remote-method="searchBatches"
              :loading="batchSearching"
              style="width: 100%"
              @change="(id) => onPickBatch($index, id)"
            >
              <el-option
                v-for="b in batchOptions"
                :key="b.id"
                :label="`${b.drugName} / ${b.batchNo}（账存 ${b.quantity}）`"
                :value="b.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="账存" width="90">
          <template #default="{ row }">{{ row.bookQty ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="实盘" width="110">
          <template #default="{ row }">
            <el-input-number
              v-model="row.actualQty"
              :min="0"
              :precision="2"
              :controls="false"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="90">
          <template #default="{ row }">
            {{
              row.batchId != null && row.actualQty != null
                ? Number(row.actualQty - (row.bookQty || 0)).toFixed(2)
                : '-'
            }}
          </template>
        </el-table-column>
        <el-table-column label="" width="60">
          <template #default="{ $index }">
            <el-button link type="danger" @click="stocktakeForm.items.splice($index, 1)">删</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="stocktakeCreateVisible = false">取消</el-button>
        <el-button :loading="stocktakeSaving" @click="onSaveStocktake(false)">仅保存</el-button>
        <el-button type="primary" :loading="stocktakeSaving" @click="onSaveStocktake(true)">
          保存并过账
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stocktakeDetailVisible" title="盘点单详情" width="760px" destroy-on-close>
      <template v-if="stocktakeDetail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="单号">{{ stocktakeDetail.stocktakeNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ stocktakeDetail.status === 'Posted' ? '已过账' : '草稿' }}
          </el-descriptions-item>
          <el-descriptions-item label="操作人">{{ stocktakeDetail.operatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="过账时间">{{ formatTime(stocktakeDetail.postTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ stocktakeDetail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="stocktakeDetail.items || []" border size="small" style="margin-top: 12px">
          <el-table-column prop="drugName" label="药品" min-width="140" />
          <el-table-column prop="batchNo" label="批次" width="120" />
          <el-table-column prop="bookQty" label="账存" width="90" />
          <el-table-column prop="actualQty" label="实盘" width="90" />
          <el-table-column prop="diffQty" label="差异" width="90" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  expireWarn,
  getBoundDetail,
  getInboundDetail,
  getStocktakeDetail,
  pageBounds,
  pageInbounds,
  pageStock,
  pageStocktakes,
  postInbound,
  postStocktake,
  saveInbound,
  saveStocktake,
} from '@/api/inventory'
import { listSuppliers, pageDrugs } from '@/api/master'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const tab = ref('stock')

const stockQuery = reactive({ drugName: '' })
const stockRows = ref([])
const stockLoading = ref(false)
const stockPage = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const inboundQuery = reactive({ status: '' })
const inboundRows = ref([])
const inboundLoading = ref(false)
const inboundPage = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const postingId = ref('')

const stocktakeQuery = reactive({ status: '', keyword: '' })
const stocktakeRows = ref([])
const stocktakeLoading = ref(false)
const stocktakePage = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const stocktakePostingId = ref('')
const stocktakeCreateVisible = ref(false)
const stocktakeForm = reactive({ remark: '', items: [] })
const stocktakeSaving = ref(false)
const batchOptions = ref([])
const batchSearching = ref(false)
const stocktakeDetailVisible = ref(false)
const stocktakeDetail = ref(null)

const expireRows = ref([])
const expireLoading = ref(false)

const createVisible = ref(false)
const createForm = reactive({ id: '', supplierId: '', supplierName: '', remark: '', items: [] })
const saving = ref(false)
const drugOptions = ref([])
const drugSearching = ref(false)
const suppliers = ref([])

const boundQuery = reactive({ boundType: '', status: '', keyword: '' })
const boundRows = ref([])
const boundLoading = ref(false)
const boundPage = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const detailVisible = ref(false)
const inboundDetail = ref(null)
const boundDetailTitle = computed(() => {
  if (!inboundDetail.value) return '单据详情'
  return `${boundTypeLabel(inboundDetail.value.boundType)}详情`
})

function formatMoney(v) {
  return Number(v || 0).toFixed(2)
}
function formatTime(v) {
  return v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-'
}
function isInboundDraft(row) {
  const status = row?.status
  return status === 'Draft' || status === 'draft' || status === 'DRAFT'
}
function isPurchaseInboundDraft(row) {
  return isInboundDraft(row) && (!row.boundType || row.boundType === 'PurchaseIn')
}
function boundTypeLabel(t) {
  return {
    PurchaseIn: '采购入库',
    DispenseOut: '发药出库',
    StocktakeAdjust: '盘点调整',
  }[t] || t || '-'
}
function blankItem() {
  return { drugId: '', drugName: '', drugCode: '', batchNo: '', expireDate: '', quantity: 1, unitPrice: 0, unit: '' }
}

function onTabChange(name) {
  if (name === 'stock') loadStock()
  else if (name === 'inbound') loadInbounds()
  else if (name === 'stocktake') loadStocktakes()
  else if (name === 'bound') loadBounds()
  else loadExpire()
}

function blankStocktakeItem() {
  return { batchId: '', drugId: '', drugName: '', batchNo: '', bookQty: 0, actualQty: 0 }
}

async function loadStock() {
  stockLoading.value = true
  try {
    const res = await pageStock({
      drugName: stockQuery.drugName || undefined,
      currentPage: stockPage.currentPage,
      pageSize: stockPage.pageSize,
    })
    const data = res.data || res
    stockRows.value = data.records || []
    stockPage.total = data.total || 0
  } finally {
    stockLoading.value = false
  }
}

async function loadInbounds() {
  inboundLoading.value = true
  try {
    const res = await pageInbounds({
      status: inboundQuery.status || undefined,
      currentPage: inboundPage.currentPage,
      pageSize: inboundPage.pageSize,
    })
    const data = res.data || res
    inboundRows.value = data.records || []
    inboundPage.total = data.total || 0
  } finally {
    inboundLoading.value = false
  }
}

async function loadExpire() {
  expireLoading.value = true
  try {
    const res = await expireWarn()
    expireRows.value = res.data || res || []
  } finally {
    expireLoading.value = false
  }
}

function openCreateInbound() {
  createForm.id = ''
  createForm.supplierId = ''
  createForm.supplierName = ''
  createForm.remark = ''
  createForm.items = [blankItem()]
  drugOptions.value = []
  createVisible.value = true
}

function onSupplierPick(id) {
  const s = suppliers.value.find((x) => x.id === id)
  createForm.supplierName = s?.supplierName || ''
}

async function openEditInbound(row) {
  const res = await getInboundDetail(row.id)
  const detail = res.data || res
  createForm.id = detail.id
  createForm.supplierId = detail.supplierId || ''
  createForm.supplierName = detail.supplierName || ''
  createForm.remark = detail.remark || ''
  createForm.items = (detail.items || []).map((it) => ({
    drugId: it.drugId,
    drugName: it.drugName,
    drugCode: it.drugCode,
    batchNo: it.batchNo,
    expireDate: it.expireDate || '',
    quantity: Number(it.quantity || 0),
    unitPrice: Number(it.unitPrice || 0),
    unit: it.unit || '',
  }))
  if (!createForm.items.length) createForm.items = [blankItem()]
  drugOptions.value = createForm.items
    .filter((it) => it.drugId)
    .map((it) => ({
      id: it.drugId,
      drugName: it.drugName,
      drugCode: it.drugCode,
      spec: '',
      unit: it.unit,
      costPrice: it.unitPrice,
    }))
  createVisible.value = true
}

function addItem() {
  createForm.items.push(blankItem())
}

async function searchDrugs(keyword) {
  if (!keyword) {
    drugOptions.value = []
    return
  }
  drugSearching.value = true
  try {
    const res = await pageDrugs({ keyword, status: 'active', currentPage: 1, pageSize: 20 })
    const data = res.data || res
    drugOptions.value = data.records || []
  } finally {
    drugSearching.value = false
  }
}

function onPickDrug(index, drugId) {
  const drug = drugOptions.value.find((d) => d.id === drugId)
  if (!drug) return
  const row = createForm.items[index]
  row.drugId = drug.id
  row.drugName = drug.drugName
  row.drugCode = drug.drugCode
  row.unit = drug.unit
  row.unitPrice = Number(drug.costPrice || 0)
}

async function onSaveInbound(andPost) {
  const items = createForm.items.filter((it) => it.drugId && it.batchNo && Number(it.quantity) > 0)
  if (!items.length) {
    ElMessage.warning('请完善入库明细（药品、批次、数量）')
    return
  }
  saving.value = true
  try {
    const res = await saveInbound({
      id: createForm.id || undefined,
      supplierId: createForm.supplierId || undefined,
      supplierName: createForm.supplierName,
      remark: createForm.remark,
      items,
    })
    const bound = res.data || res
    if (andPost && bound?.id) {
      await postInbound(bound.id)
      ElMessage.success('入库已过账，库存已增加')
    } else {
      ElMessage.success(createForm.id ? '入库草稿已更新' : '入库草稿已保存')
    }
    createVisible.value = false
    tab.value = 'inbound'
    await loadInbounds()
    await loadStock()
  } finally {
    saving.value = false
  }
}

async function onPost(row) {
  await ElMessageBox.confirm('确认过账？过账后将增加批次库存。', '入库过账', { type: 'warning' })
  postingId.value = row.id
  try {
    await postInbound(row.id)
    ElMessage.success('过账成功')
    await loadInbounds()
    await loadStock()
  } finally {
    postingId.value = ''
  }
}

async function openInboundDetail(row) {
  const res = await getInboundDetail(row.id)
  inboundDetail.value = res.data || res
  detailVisible.value = true
}

async function openBoundDetail(row) {
  const res = await getBoundDetail(row.id)
  inboundDetail.value = res.data || res
  detailVisible.value = true
}

async function loadBounds() {
  boundLoading.value = true
  try {
    const res = await pageBounds({
      boundType: boundQuery.boundType || undefined,
      status: boundQuery.status || undefined,
      keyword: boundQuery.keyword || undefined,
      currentPage: boundPage.currentPage,
      pageSize: boundPage.pageSize,
    })
    const data = res.data || res
    boundRows.value = data.records || []
    boundPage.total = data.total || 0
  } finally {
    boundLoading.value = false
  }
}

function editFromDetail() {
  if (!inboundDetail.value) return
  detailVisible.value = false
  openEditInbound(inboundDetail.value)
}

async function loadSuppliers() {
  const res = await listSuppliers()
  suppliers.value = res.data || res || []
}

async function loadStocktakes() {
  stocktakeLoading.value = true
  try {
    const res = await pageStocktakes({
      status: stocktakeQuery.status || undefined,
      keyword: stocktakeQuery.keyword || undefined,
      currentPage: stocktakePage.currentPage,
      pageSize: stocktakePage.pageSize,
    })
    const data = res.data || res
    stocktakeRows.value = data.records || []
    stocktakePage.total = data.total || 0
  } finally {
    stocktakeLoading.value = false
  }
}

function openCreateStocktake() {
  stocktakeForm.remark = ''
  stocktakeForm.items = [blankStocktakeItem()]
  batchOptions.value = []
  stocktakeCreateVisible.value = true
}

function addStocktakeItem() {
  stocktakeForm.items.push(blankStocktakeItem())
}

async function searchBatches(keyword) {
  batchSearching.value = true
  try {
    const res = await pageStock({
      drugName: keyword || undefined,
      status: 'active',
      currentPage: 1,
      pageSize: 30,
    })
    const data = res.data || res
    batchOptions.value = data.records || []
  } finally {
    batchSearching.value = false
  }
}

function onPickBatch(index, batchId) {
  const batch = batchOptions.value.find((b) => b.id === batchId)
  if (!batch) return
  const row = stocktakeForm.items[index]
  row.batchId = batch.id
  row.drugId = batch.drugId
  row.drugName = batch.drugName
  row.batchNo = batch.batchNo
  row.bookQty = Number(batch.quantity || 0)
  row.actualQty = Number(batch.quantity || 0)
}

async function onSaveStocktake(andPost) {
  const items = stocktakeForm.items.filter((it) => it.batchId)
  if (!items.length) {
    ElMessage.warning('请至少选择一个库存批次')
    return
  }
  stocktakeSaving.value = true
  try {
    const res = await saveStocktake({
      remark: stocktakeForm.remark,
      operatorName: userStore.displayName,
      items,
    })
    const sheet = res.data || res
    if (andPost && sheet?.id) {
      await postStocktake(sheet.id)
      ElMessage.success('盘点已过账，库存已调整')
    } else {
      ElMessage.success('盘点草稿已保存')
    }
    stocktakeCreateVisible.value = false
    tab.value = 'stocktake'
    await loadStocktakes()
    await loadStock()
  } finally {
    stocktakeSaving.value = false
  }
}

async function onPostStocktake(row) {
  await ElMessageBox.confirm('确认过账？过账后将按实盘数量调整库存。', '盘点过账', {
    type: 'warning',
  })
  stocktakePostingId.value = row.id
  try {
    await postStocktake(row.id)
    ElMessage.success('盘点过账成功')
    await loadStocktakes()
    await loadStock()
  } finally {
    stocktakePostingId.value = ''
  }
}

async function openStocktakeDetail(row) {
  const res = await getStocktakeDetail(row.id)
  stocktakeDetail.value = res.data || res
  stocktakeDetailVisible.value = true
}

onMounted(async () => {
  await loadSuppliers()
  await loadStock()
})
</script>

<style scoped>
.inv-page {
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
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
}

.posted-tip {
  margin-left: 4px;
  color: var(--clinic-muted);
  font-size: 12px;
}
</style>
