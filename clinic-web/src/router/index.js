import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const Layout = () => import('@/layout/MainLayout.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
        meta: { title: '首页', menu: 'home' },
      },
      {
        path: 'appointment',
        name: 'Appointment',
        component: () => import('@/views/appointment/AppointmentView.vue'),
        meta: { title: '挂号', menu: 'appointment' },
      },
      {
        path: 'outpatient',
        name: 'Outpatient',
        component: () => import('@/views/outpatient/OutpatientView.vue'),
        meta: { title: '门诊', menu: 'outpatient' },
      },
      {
        path: 'outpatient/consult/:id',
        name: 'Consult',
        component: () => import('@/views/outpatient/ConsultView.vue'),
        meta: { title: '接诊', menu: 'outpatient' },
      },
      {
        path: 'charge',
        name: 'Charge',
        component: () => import('@/views/charge/ChargeView.vue'),
        meta: { title: '收费', menu: 'charge' },
      },
      {
        path: 'pharmacy',
        name: 'Pharmacy',
        component: () => import('@/views/pharmacy/PharmacyView.vue'),
        meta: { title: '药房', menu: 'pharmacy' },
      },
      {
        path: 'patient',
        name: 'Patient',
        component: () => import('@/views/patient/PatientArchive.vue'),
        meta: { title: '患者', menu: 'patient' },
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/inventory/InventoryView.vue'),
        meta: { title: '库存', menu: 'inventory' },
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/placeholder/ModulePlaceholder.vue'),
        meta: { title: '统计', menu: 'report', tip: '门诊量 / 收入统计' },
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/views/system/SystemMasterView.vue'),
        meta: { title: '设置', menu: 'system' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  NProgress.start()
  if (to.meta.public) {
    next()
    return
  }
  if (!getToken()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

router.afterEach(() => NProgress.done())

export default router
