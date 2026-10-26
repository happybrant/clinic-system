<template>
  <div class="patient-page">
    <!-- 左侧患者列表 -->
    <aside class="list-pane">
      <div class="list-toolbar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索姓名/手机/档案号"
          prefix-icon="Search"
          @keyup.enter="reloadList"
          @clear="reloadList"
        />
        <el-button type="primary" @click="openCreate">建档</el-button>
      </div>
      <div v-loading="listLoading" class="list-body">
        <div
          v-for="item in patients"
          :key="item.id"
          class="list-item"
          :class="{ active: item.id === currentId }"
          @click="selectPatient(item.id)"
        >
          <div class="row1">
            <span class="name">{{ item.name }}</span>
            <span class="age">{{ formatAge(item) }}</span>
            <el-tag v-if="item.tags" size="small" type="warning" effect="plain">
              {{ firstTag(item.tags) }}
            </el-tag>
          </div>
          <div class="row2">
            <span>{{ item.mobile || '未填手机' }}</span>
            <span>{{ formatTime(item.lastVisitTime || item.createTime) }}</span>
          </div>
        </div>
        <el-empty v-if="!listLoading && !patients.length" description="暂无患者" :image-size="80" />
      </div>
      <div class="list-pager">
        <el-pagination
          v-model:current-page="page.currentPage"
          v-model:page-size="page.pageSize"
          small
          layout="prev, pager, next"
          :total="page.total"
          @current-change="loadList"
        />
      </div>
    </aside>

    <!-- 中间详情 -->
    <section class="detail-pane" v-loading="detailLoading">
      <template v-if="detail">
        <div class="detail-header">
          <div class="profile">
            <el-avatar :size="56" class="avatar">{{ detail.name?.slice(0, 1) }}</el-avatar>
            <div>
              <div class="title-row">
                <h2>{{ detail.name }}</h2>
                <span>{{ genderLabel(detail.gender) }}</span>
                <span>{{ formatAge(detail) }}</span>
                <span class="muted">档案号 {{ detail.patientNo || '-' }}</span>
                <el-tag v-if="detail.tags" size="small" type="success" effect="light">
                  {{ firstTag(detail.tags) || '普通' }}
                </el-tag>
              </div>
              <div class="stat-row">
                <span>就诊 <b>{{ detail.visitCount ?? 0 }}</b> 次</span>
                <span>累计消费 <b class="money">¥{{ formatMoney(detail.totalAmount) }}</b></span>
                <span>最近就诊 {{ formatDate(detail.lastVisitTime) || '-' }}</span>
              </div>
            </div>
          </div>
          <div class="actions">
            <el-button @click="openEdit">修改</el-button>
            <el-button type="primary" plain @click="$router.push('/appointment')">预约</el-button>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="档案详情" name="profile">
            <div class="info-grid">
              <div class="info-item"><label>出生日期</label><span>{{ detail.birthday || '-' }}</span></div>
              <div class="info-item"><label>建档日期</label><span>{{ formatDate(detail.createTime) || '-' }}</span></div>
              <div class="info-item"><label>证件类型</label><span>{{ detail.idType || '-' }}</span></div>
              <div class="info-item"><label>证件号码</label><span>{{ detail.idNumber || '-' }}</span></div>
              <div class="info-item"><label>手机号码</label><span>{{ detail.mobile || '-' }}</span></div>
              <div class="info-item"><label>婚姻状况</label><span>{{ detail.maritalStatus || '-' }}</span></div>
              <div class="info-item"><label>身高</label><span>{{ detail.heightCm != null ? detail.heightCm + ' cm' : '-' }}</span></div>
              <div class="info-item"><label>体重</label><span>{{ detail.weightKg != null ? detail.weightKg + ' kg' : '-' }}</span></div>
              <div class="info-item"><label>职业</label><span>{{ detail.occupation || '-' }}</span></div>
              <div class="info-item"><label>民族</label><span>{{ detail.nation || '-' }}</span></div>
              <div class="info-item"><label>来源</label><span>{{ detail.source || '-' }}</span></div>
              <div class="info-item full"><label>家庭住址</label><span>{{ detail.address || '-' }}</span></div>
              <div class="info-item full highlight">
                <label>既往史</label>
                <span>{{ detail.pastHistory || '-' }}</span>
              </div>
              <div class="info-item full highlight warn">
                <label>过敏史</label>
                <span>{{ detail.allergyHistory || '-' }}</span>
              </div>
              <div class="info-item full"><label>备注</label><span>{{ detail.remark || '-' }}</span></div>
            </div>

            <div class="block">
              <div class="block-title">家庭成员 / 联系人</div>
              <el-table :data="detail.contacts || []" size="small" empty-text="暂无联系人">
                <el-table-column prop="name" label="姓名" min-width="100" />
                <el-table-column prop="relation" label="关系" width="100" />
                <el-table-column prop="mobile" label="手机" min-width="120" />
                <el-table-column prop="remark" label="备注" min-width="140" />
              </el-table>
            </div>

            <div class="block">
              <div class="block-title">操作动态</div>
              <el-table :data="detail.logs || []" size="small" empty-text="暂无动态">
                <el-table-column prop="operateTime" label="时间" width="170">
                  <template #default="{ row }">{{ formatTime(row.operateTime) }}</template>
                </el-table-column>
                <el-table-column prop="operation" label="操作" width="100" />
                <el-table-column prop="content" label="内容" min-width="180" />
                <el-table-column prop="operatorName" label="操作人" width="100" />
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="预约记录" name="appt">
            <el-empty description="预约记录将在挂号模块联通后展示" :image-size="90" />
          </el-tab-pane>
          <el-tab-pane label="病历记录" name="emr">
            <el-empty description="病历记录将在门诊模块联通后展示" :image-size="90" />
          </el-tab-pane>
          <el-tab-pane label="收费记录" name="charge">
            <el-empty description="收费记录将在收费模块联通后展示" :image-size="90" />
          </el-tab-pane>
        </el-tabs>
      </template>
      <el-empty v-else description="请选择左侧患者查看档案" />
    </section>

    <!-- 右侧会员摘要 -->
    <aside class="side-pane" v-if="detail">
      <div class="member-card">
        <div class="member-level">{{ memberLevel }}</div>
        <div class="member-row">
          <div>
            <div class="label">累计消费</div>
            <div class="value money">¥{{ formatMoney(detail.totalAmount) }}</div>
          </div>
          <div>
            <div class="label">就诊次数</div>
            <div class="value">{{ detail.visitCount ?? 0 }}</div>
          </div>
        </div>
        <div class="member-links">
          <span>最近就诊 {{ formatDate(detail.lastVisitTime) || '暂无' }}</span>
        </div>
      </div>
      <div class="side-block">
        <div class="side-title">资产摘要</div>
        <div class="asset-item">
          <span>会员余额</span>
          <b>—</b>
        </div>
        <div class="asset-item">
          <span>积分</span>
          <b>—</b>
        </div>
        <p class="side-tip">会员储值 / 积分 / 卡项能力可后续扩展</p>
      </div>
      <div class="side-block">
        <div class="side-title">快捷操作</div>
        <el-button class="full-btn" @click="openEdit">编辑档案</el-button>
        <el-button class="full-btn" type="primary" plain @click="$router.push('/appointment')">
          去挂号预约
        </el-button>
      </div>
    </aside>

    <!-- 建档/编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '修改患者' : '新建档案'"
      width="720px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" style="width: 100%">
                <el-option label="男" value="M" />
                <el-option label="女" value="F" />
                <el-option label="未知" value="U" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机" prop="mobile">
              <el-input v-model="form.mobile" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证件号码">
              <el-input v-model="form.idNumber" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="form.tags" placeholder="如：金卡,普通" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身高(cm)">
              <el-input-number v-model="form.heightCm" :min="0" :max="300" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="form.weightKg" :min="0" :max="500" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="婚姻">
              <el-input v-model="form.maritalStatus" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职业">
              <el-input v-model="form.occupation" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="form.nation" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源">
              <el-input v-model="form.source" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="住址">
              <el-input v-model="form.address" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="既往史">
              <el-input v-model="form.pastHistory" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="过敏史">
              <el-input v-model="form.allergyHistory" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="contact-editor">
          <div class="contact-head">
            <span>联系人</span>
            <el-button link type="primary" @click="addContact">添加</el-button>
          </div>
          <div v-for="(c, idx) in form.contacts" :key="idx" class="contact-row">
            <el-input v-model="c.name" placeholder="姓名" />
            <el-input v-model="c.relation" placeholder="关系" />
            <el-input v-model="c.mobile" placeholder="手机" />
            <el-button link type="danger" @click="form.contacts.splice(idx, 1)">删除</el-button>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePatient">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { addPatient, getPatientDetail, pagePatients, updatePatient } from '@/api/patient'

const keyword = ref('')
const listLoading = ref(false)
const detailLoading = ref(false)
const patients = ref([])
const currentId = ref('')
const detail = ref(null)
const activeTab = ref('profile')
const page = reactive({ currentPage: 1, pageSize: 20, total: 0 })

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive(emptyForm())
const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

const memberLevel = computed(() => {
  const tag = firstTag(detail.value?.tags)
  if (tag) return tag.includes('会员') ? tag : `${tag}会员`
  const amount = Number(detail.value?.totalAmount || 0)
  if (amount >= 10000) return '金卡会员'
  if (amount >= 3000) return '银卡会员'
  if (amount > 0) return '普通会员'
  return '普通患者'
})

function emptyForm() {
  return {
    id: '',
    name: '',
    gender: 'M',
    mobile: '',
    birthday: '',
    idType: '身份证',
    idNumber: '',
    tags: '',
    heightCm: undefined,
    weightKg: undefined,
    maritalStatus: '',
    occupation: '',
    nation: '',
    source: '',
    address: '',
    pastHistory: '',
    allergyHistory: '',
    remark: '',
    contacts: [],
  }
}

function firstTag(tags) {
  if (!tags) return ''
  return String(tags).split(/[,，]/)[0]?.trim() || ''
}

function genderLabel(g) {
  if (g === 'M' || g === '男') return '男'
  if (g === 'F' || g === '女') return '女'
  return '未知'
}

function formatAge(p) {
  if (!p) return ''
  if (p.age != null) return `${p.age}岁`
  if (p.birthday) {
    const years = dayjs().diff(dayjs(p.birthday), 'year')
    return `${years}岁`
  }
  return ''
}

function formatMoney(v) {
  const n = Number(v || 0)
  return n.toFixed(2)
}

function formatDate(v) {
  return v ? dayjs(v).format('YYYY-MM-DD') : ''
}

function formatTime(v) {
  return v ? dayjs(v).format('YYYY-MM-DD HH:mm') : ''
}

async function loadList() {
  listLoading.value = true
  try {
    const res = await pagePatients({
      keyword: keyword.value,
      currentPage: page.currentPage,
      pageSize: page.pageSize,
    })
    const data = res.data || res
    patients.value = data.records || []
    page.total = Number(data.total || 0)
    if (!currentId.value && patients.value.length) {
      await selectPatient(patients.value[0].id)
    }
  } finally {
    listLoading.value = false
  }
}

function reloadList() {
  page.currentPage = 1
  loadList()
}

async function selectPatient(id) {
  currentId.value = id
  detailLoading.value = true
  try {
    const res = await getPatientDetail(id)
    detail.value = res.data || res
    activeTab.value = 'profile'
  } finally {
    detailLoading.value = false
  }
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit() {
  if (!detail.value) return
  Object.assign(form, emptyForm(), {
    ...detail.value,
    contacts: (detail.value.contacts || []).map((c) => ({ ...c })),
  })
  dialogVisible.value = true
}

function addContact() {
  form.contacts.push({ name: '', relation: '', mobile: '', remark: '' })
}

async function savePatient() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form }
    if (form.id) {
      await updatePatient(payload)
      ElMessage.success('更新成功')
    } else {
      await addPatient(payload)
      ElMessage.success('建档成功')
    }
    dialogVisible.value = false
    await loadList()
    if (form.id) await selectPatient(form.id)
  } finally {
    saving.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.patient-page {
  display: flex;
  height: 100%;
  background: #e9edf2;
}

.list-pane {
  width: 280px;
  background: #fff;
  border-right: 1px solid var(--clinic-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.list-toolbar {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid var(--clinic-border);
}

.list-body {
  flex: 1;
  overflow: auto;
}

.list-item {
  padding: 12px 14px;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
}

.list-item:hover {
  background: #f7faf8;
}

.list-item.active {
  background: rgba(43, 182, 115, 0.12);
  box-shadow: inset 3px 0 0 var(--clinic-primary);
}

.row1 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.name {
  font-weight: 600;
  font-size: 15px;
}

.age {
  color: var(--clinic-muted);
  font-size: 12px;
}

.row2 {
  display: flex;
  justify-content: space-between;
  color: var(--clinic-muted);
  font-size: 12px;
}

.list-pager {
  padding: 8px;
  border-top: 1px solid var(--clinic-border);
  display: flex;
  justify-content: center;
}

.detail-pane {
  flex: 1;
  min-width: 0;
  overflow: auto;
  padding: 16px 18px;
  background: #fff;
  margin: 10px 0 10px 10px;
  border-radius: 8px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--clinic-border);
}

.profile {
  display: flex;
  gap: 14px;
}

.avatar {
  background: var(--clinic-primary);
  color: #fff;
  font-size: 22px;
}

.title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.title-row h2 {
  margin: 0;
  font-size: 22px;
}

.muted {
  color: var(--clinic-muted);
  font-size: 13px;
}

.stat-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13px;
  color: #606266;
}

.stat-row b {
  color: var(--clinic-text);
}

.money {
  color: #e6a23c !important;
}

.detail-tabs {
  margin-top: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px 18px;
  padding: 8px 0 16px;
}

.info-item {
  display: flex;
  gap: 10px;
  font-size: 13px;
  min-height: 28px;
}

.info-item.full {
  grid-column: 1 / -1;
}

.info-item label {
  width: 72px;
  flex-shrink: 0;
  color: var(--clinic-muted);
}

.info-item.highlight span {
  color: #303133;
  font-weight: 500;
}

.info-item.warn span {
  color: #f56c6c;
}

.block {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px dashed var(--clinic-border);
}

.block-title {
  font-weight: 600;
  margin-bottom: 10px;
}

.side-pane {
  width: 240px;
  flex-shrink: 0;
  margin: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-card {
  background: linear-gradient(145deg, #2bb673, #1f9a5f 55%, #243447);
  color: #fff;
  border-radius: 10px;
  padding: 16px;
}

.member-level {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}

.member-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.member-row .label {
  font-size: 12px;
  opacity: 0.85;
}

.member-row .value {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 600;
}

.member-links {
  margin-top: 14px;
  font-size: 12px;
  opacity: 0.9;
}

.side-block {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  border: 1px solid var(--clinic-border);
}

.side-title {
  font-weight: 600;
  margin-bottom: 12px;
}

.asset-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  border-bottom: 1px solid #f2f3f5;
}

.side-tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--clinic-muted);
  line-height: 1.5;
}

.full-btn {
  width: 100%;
  margin-left: 0 !important;
  margin-bottom: 8px;
}

.contact-editor {
  margin-top: 4px;
}

.contact-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
}

.contact-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr auto;
  gap: 8px;
  margin-bottom: 8px;
}

@media (max-width: 1200px) {
  .side-pane {
    display: none;
  }
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
