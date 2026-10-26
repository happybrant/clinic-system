<template>
  <div class="sys-page">
    <el-tabs v-model="tab">
      <el-tab-pane label="科室" name="dept" />
      <el-tab-pane label="医生" name="doctor" />
      <el-tab-pane label="药品" name="drug" />
      <el-tab-pane label="收费项目" name="charge" />
    </el-tabs>

    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="关键字" style="width: 220px" @keyup.enter="reload" />
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button type="success" @click="openCreate">新增</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" stripe height="100%">
      <template v-if="tab === 'dept'">
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 'active' ? '启用' : '停用' }}</template>
        </el-table-column>
      </template>
      <template v-else-if="tab === 'doctor'">
        <el-table-column prop="empNo" label="工号" width="100" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="title" label="职称" width="100" />
        <el-table-column prop="deptName" label="科室" />
        <el-table-column prop="mobile" label="手机" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 'active' ? '启用' : '停用' }}</template>
        </el-table-column>
      </template>
      <template v-else-if="tab === 'drug'">
        <el-table-column prop="drugCode" label="编码" width="120" />
        <el-table-column prop="drugName" label="名称" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="salePrice" label="售价" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 'active' ? '启用' : '停用' }}</template>
        </el-table-column>
      </template>
      <template v-else>
        <el-table-column prop="itemCode" label="编码" width="120" />
        <el-table-column prop="itemName" label="名称" />
        <el-table-column prop="itemType" label="类型" width="100" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 'active' ? '启用' : '停用' }}</template>
        </el-table-column>
      </template>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑' : '新增'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <template v-if="tab === 'dept'">
          <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        </template>
        <template v-else-if="tab === 'doctor'">
          <el-form-item label="工号"><el-input v-model="form.empNo" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="职称"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="科室">
            <el-select v-model="form.deptId" style="width: 100%" @change="onDeptPick">
              <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="手机"><el-input v-model="form.mobile" /></el-form-item>
        </template>
        <template v-else-if="tab === 'drug'">
          <el-form-item label="编码"><el-input v-model="form.drugCode" /></el-form-item>
          <el-form-item label="名称"><el-input v-model="form.drugName" /></el-form-item>
          <el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item>
          <el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item>
          <el-form-item label="售价"><el-input-number v-model="form.salePrice" :min="0" :precision="2" /></el-form-item>
        </template>
        <template v-else>
          <el-form-item label="编码"><el-input v-model="form.itemCode" /></el-form-item>
          <el-form-item label="名称"><el-input v-model="form.itemName" /></el-form-item>
          <el-form-item label="类型">
            <el-select v-model="form.itemType" style="width: 100%">
              <el-option label="挂号" value="register" />
              <el-option label="诊疗" value="consult" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        </template>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addChargeItem,
  addDepartment,
  addDoctor,
  addDrug,
  listDepartments,
  pageChargeItems,
  pageDepartments,
  pageDoctors,
  pageDrugs,
  updateChargeItem,
  updateDepartment,
  updateDoctor,
  updateDrug,
} from '@/api/master'

const tab = ref('dept')
const keyword = ref('')
const loading = ref(false)
const rows = ref([])
const departments = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({})

watch(tab, () => {
  keyword.value = ''
  reload()
})

function emptyForm() {
  if (tab.value === 'dept') return { code: '', name: '', sort: 0, remark: '', status: 'active' }
  if (tab.value === 'doctor')
    return { empNo: '', name: '', title: '', deptId: '', deptName: '', mobile: '', status: 'active', canSchedule: true }
  if (tab.value === 'drug')
    return { drugCode: '', drugName: '', spec: '', unit: '盒', salePrice: 0, status: 'active' }
  return { itemCode: '', itemName: '', itemType: 'consult', price: 0, status: 'active' }
}

function onDeptPick(id) {
  const d = departments.value.find((x) => x.id === id)
  form.deptName = d?.name || ''
}

async function reload() {
  loading.value = true
  try {
    const payload = { keyword: keyword.value, currentPage: 1, pageSize: 100 }
    let res
    if (tab.value === 'dept') res = await pageDepartments(payload)
    else if (tab.value === 'doctor') res = await pageDoctors(payload)
    else if (tab.value === 'drug') res = await pageDrugs(payload)
    else res = await pageChargeItems(payload)
    const data = res.data || res
    rows.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, emptyForm(), { ...row })
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (tab.value === 'dept') {
      if (form.id) await updateDepartment(form)
      else await addDepartment(form)
    } else if (tab.value === 'doctor') {
      if (form.id) await updateDoctor(form)
      else await addDoctor(form)
    } else if (tab.value === 'drug') {
      if (form.id) await updateDrug(form)
      else await addDrug(form)
    } else {
      if (form.id) await updateChargeItem(form)
      else await addChargeItem(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await reload()
    if (tab.value === 'dept') {
      const res = await listDepartments()
      departments.value = res.data || res || []
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const res = await listDepartments()
  departments.value = res.data || res || []
  await reload()
})
</script>

<style scoped>
.sys-page {
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
  gap: 10px;
  margin-bottom: 12px;
}
</style>
