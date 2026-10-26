<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-brand">
        <div class="mark">诊</div>
        <h1>诊所管家</h1>
        <p>单店门诊 · 预约挂号 · 病历处方 · 收费发药</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="onSubmit">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="账号" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="onSubmit">
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  account: 'admin',
  password: 'admin123',
})
const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 20% 20%, rgba(43, 182, 115, 0.18), transparent 40%),
    radial-gradient(circle at 80% 0%, rgba(36, 52, 71, 0.2), transparent 35%),
    linear-gradient(135deg, #eef5f1 0%, #f7f8fa 50%, #e8eef5 100%);
}

.login-panel {
  width: 420px;
  padding: 40px 36px 32px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(31, 45, 61, 0.12);
  backdrop-filter: blur(6px);
}

.login-brand {
  text-align: center;
  margin-bottom: 28px;
}

.mark {
  width: 52px;
  height: 52px;
  margin: 0 auto 12px;
  border-radius: 14px;
  background: var(--clinic-primary);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  line-height: 52px;
}

.login-brand h1 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #1f2d3d;
}

.login-brand p {
  margin: 0;
  font-size: 13px;
  color: var(--clinic-muted);
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
