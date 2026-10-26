<template>
  <div class="main-layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">诊</div>
        <div class="brand-text">诊所管家</div>
      </div>
      <nav class="menu">
        <router-link
          v-for="item in menus"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: route.meta.menu === item.menu }"
        >
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </router-link>
      </nav>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <span class="clinic-name">体验诊所</span>
          <span class="divider">|</span>
          <span class="page-title">{{ route.meta.title || '' }}</span>
        </div>
        <div class="topbar-right">
          <el-icon :size="18"><Bell /></el-icon>
          <el-dropdown trigger="click" @command="onUserCommand">
            <span class="user-entry">
              <el-avatar :size="28">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ userStore.displayName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { path: '/home', menu: 'home', title: '首页', icon: 'HomeFilled' },
  { path: '/appointment', menu: 'appointment', title: '挂号', icon: 'Tickets' },
  { path: '/outpatient', menu: 'outpatient', title: '门诊', icon: 'FirstAidKit' },
  { path: '/charge', menu: 'charge', title: '收费', icon: 'Wallet' },
  { path: '/pharmacy', menu: 'pharmacy', title: '药房', icon: 'Goods' },
  { path: '/patient', menu: 'patient', title: '患者', icon: 'User' },
  { path: '/inventory', menu: 'inventory', title: '库存', icon: 'Box' },
  { path: '/report', menu: 'report', title: '统计', icon: 'DataAnalysis' },
  { path: '/system', menu: 'system', title: '设置', icon: 'Setting' },
]

const avatarText = computed(() => (userStore.displayName || 'U').slice(0, 1))

async function onUserCommand(cmd) {
  if (cmd === 'logout') {
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100%;
  min-height: 100vh;
  background: var(--clinic-bg);
}

.sidebar {
  width: var(--clinic-sidebar-w);
  background: linear-gradient(180deg, #243447 0%, #1a2533 100%);
  color: #c0c4cc;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.brand {
  padding: 14px 8px 18px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.brand-mark {
  width: 36px;
  height: 36px;
  margin: 0 auto 6px;
  border-radius: 10px;
  background: var(--clinic-primary);
  color: #fff;
  font-weight: 700;
  line-height: 36px;
  font-size: 16px;
}

.brand-text {
  font-size: 11px;
  color: #e5eaf3;
  letter-spacing: 0.5px;
}

.menu {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 4px;
  font-size: 12px;
  color: #a8abb2;
  transition: background 0.15s, color 0.15s;
}

.menu-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.04);
}

.menu-item.active {
  color: #fff;
  background: rgba(43, 182, 115, 0.2);
  box-shadow: inset 3px 0 0 var(--clinic-primary);
}

.main-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: var(--clinic-header-h);
  background: #fff;
  border-bottom: 1px solid var(--clinic-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--clinic-text);
}

.clinic-name {
  font-weight: 600;
}

.divider {
  color: #dcdfe6;
}

.page-title {
  color: var(--clinic-muted);
  font-size: 13px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 18px;
  color: var(--clinic-muted);
}

.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-name {
  font-size: 13px;
  color: var(--clinic-text);
}

.content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
