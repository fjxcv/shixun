import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import MainIndex from '../views/MainIndex.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  {
    path: '/MainIndex',
    name: 'MainIndex',
    component: MainIndex,
    redirect: '/Workbench',
    children: [
      { path: '/Workbench', component: () => import('@/views/Workbench.vue') },
      { path: '/UserInfo', component: () => import('@/views/UesrInfo.vue') },
      { path: '/ReservationRegister', component: () => import('@/views/ReservationRegister.vue') },
      { path: '/VisitRegister', component: () => import('@/views/VisitRegister.vue') },
      { path: '/CustomerInfo', component: () => import('@/views/CustomerInfo.vue') },
      { path: '/CheckinProcess', component: () => import('@/views/CheckinProcess.vue') },
      { path: '/CheckinApply', component: () => import('@/views/CheckinApply.vue') },
      { path: '/CheckinDetail', component: () => import('@/views/CheckinDetail.vue') },
      { path: '/CheckoutProcess', component: () => import('@/views/CheckoutProcess.vue') },
      { path: '/CheckoutApply', component: () => import('@/views/CheckoutApply.vue') },
      { path: '/CheckoutDetail', component: () => import('@/views/CheckoutDetail.vue') },
      { path: '/ContractTrack', component: () => import('@/views/ContractTrack.vue') },
      { path: '/ContractDetail', component: () => import('@/views/ContractDetail.vue') },
      { path: '/BedRoomType', component: () => import('@/views/BedRoomType.vue') },
      { path: '/SmartBed', component: () => import('@/views/SmartBed.vue') },
      { path: '/RoomTypeSetting', component: () => import('@/views/RoomTypeSetting.vue') },
      { path: '/LeaveManage', component: () => import('@/views/LeaveManage.vue') },
      { path: '/LeaveDetail', component: () => import('@/views/LeaveDetail.vue') },
      { path: '/MyTodo', component: () => import('@/views/MyTodo.vue') },
      { path: '/MyApply', component: () => import('@/views/MyApply.vue') },
      { path: '/NursingItem', component: () => import('@/views/NursingItem.vue') },
      { path: '/NursingPlain', component: () => import('@/views/NursingPlain.vue') },
      { path: '/NursingLevel', component: () => import('@/views/NursingLevel.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
