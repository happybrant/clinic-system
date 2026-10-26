<template>
  <div class="page-placeholder">
    <div class="box">
      <el-icon :size="40" color="var(--clinic-primary)"><Tools /></el-icon>
      <h3>{{ route.meta.title || '模块' }}</h3>
      <p>{{ route.meta.tip || '功能开发中，敬请期待' }}</p>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<style scoped>
.box {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--clinic-border);
  min-width: 320px;
}

.box h3 {
  margin: 12px 0 8px;
  font-size: 18px;
}

.box p {
  margin: 0;
  color: var(--clinic-muted);
  font-size: 13px;
}
</style>
