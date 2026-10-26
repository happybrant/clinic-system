<template>
  <div class="home-page">
    <section class="hero">
      <div>
        <h2>欢迎使用诊所管家</h2>
        <p>支撑预约 → 到诊 → 接诊开方 → 收费 → 发药的门诊主闭环</p>
      </div>
      <el-button type="primary" @click="$router.push('/patient')">进入患者档案</el-button>
    </section>
    <div class="cards">
      <div v-for="item in cards" :key="item.path" class="card" @click="$router.push(item.path)">
        <el-icon :size="28" color="var(--clinic-primary)"><component :is="item.icon" /></el-icon>
        <div class="card-title">{{ item.title }}</div>
        <div class="card-desc">{{ item.desc }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
const cards = [
  { path: '/appointment', title: '挂号', desc: '预约 / 现场挂号 / 到诊核销', icon: 'Tickets' },
  { path: '/outpatient', title: '门诊', desc: '候诊队列 / 接诊开方', icon: 'FirstAidKit' },
  { path: '/charge', title: '收费', desc: '收银 / 退费 / 日结', icon: 'Wallet' },
  { path: '/pharmacy', title: '药房', desc: '审方 / 发药', icon: 'Goods' },
  { path: '/patient', title: '患者', desc: '建档 / 档案详情 / 就诊史', icon: 'User' },
  { path: '/inventory', title: '库存', desc: '入库 / 盘点 / 近效期', icon: 'Box' },
]
</script>

<style scoped>
.home-page {
  height: 100%;
  overflow: auto;
  padding: 24px;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  border-radius: 12px;
  background:
    linear-gradient(120deg, rgba(43, 182, 115, 0.12), rgba(36, 52, 71, 0.08)),
    #fff;
  border: 1px solid var(--clinic-border);
  margin-bottom: 20px;
}

.hero h2 {
  margin: 0 0 8px;
  font-size: 22px;
}

.hero p {
  margin: 0;
  color: var(--clinic-muted);
  font-size: 14px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.card {
  background: #fff;
  border: 1px solid var(--clinic-border);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(31, 45, 61, 0.08);
}

.card-title {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 600;
}

.card-desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--clinic-muted);
}
</style>
