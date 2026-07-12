<template>
  <PageCard>
    <div class="floor-tabs">
      <el-radio-group v-if="floors.length" v-model="activeFloorId" @change="loadSmart">
        <el-radio-button v-for="f in floors" :key="f.id" :value="f.id">{{ f.name }}</el-radio-button>
      </el-radio-group>
    </div>

    <el-empty v-if="!loading && !smartRooms.length" description="暂无床位数据" />

    <div v-for="room in smartRooms" :key="room.roomNo" class="smart-room">
      <div class="smart-room-header">
        <span class="room-no">房间号：{{ room.roomNo }}</span>
        <span class="device-icons">
          <el-tooltip content="智能烟感" placement="top"><el-icon><Bell /></el-icon></el-tooltip>
          <el-icon><VideoCamera /></el-icon>
          <el-icon><Connection /></el-icon>
        </span>
        <span class="env-info">门锁状态：{{ room.doorStatus || '-' }}</span>
        <span class="env-info">温度：{{ room.temperature || '-' }}</span>
        <span class="env-info">湿度：{{ room.humidity || '-' }}</span>
        <span class="env-info">报警状态：{{ room.alarmStatus || '-' }}</span>
      </div>
      <el-row :gutter="12">
        <el-col v-for="(bed, idx) in (room.beds || [])" :key="idx" :span="8">
          <el-card :class="['smart-bed-card', { alert: bed.alert }]">
            <div class="bed-head">
              <span>床位号：{{ bed.bedNo }}</span>
              <span class="device-mini"><el-icon><Watch /></el-icon><el-icon><Cpu /></el-icon></span>
            </div>
            <div class="bed-body">
              <div class="bed-icon">{{ bed.elderName && bed.elderName !== '--' ? '老人' : '空床' }}</div>
              <div class="bed-info">
                <div>老人姓名：{{ bed.elderName || '--' }}</div>
                <div class="status-label">{{ bed.stateLabel || '--' }}</div>
                <template v-if="bed.elderName && bed.elderName !== '--'">
                  <div v-if="bed.stateLabel === '已离床'">
                    离床次数：{{ bed.leaveCount }}次
                    <div>离床时间：{{ bed.leaveTime }}</div>
                  </div>
                  <div v-else>
                    心率：{{ bed.heartRate }}次/分
                    <div>呼吸率：{{ bed.breathRate }}次/分</div>
                  </div>
                </template>
                <div v-else class="empty-bed">当前床位没有安排老人</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </PageCard>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Bell, VideoCamera, Connection, Watch, Cpu } from '@element-plus/icons-vue'
import PageCard from '@/components/PageCard.vue'

const floors = ref([])
const activeFloorId = ref(null)
const smartRooms = ref([])
const loading = ref(false)

onMounted(() => initFloors())

function initFloors() {
  loading.value = true
  axios.get('/bed/floors')
    .then(res => {
      floors.value = res.data?.code === 200 ? (res.data?.data || []) : []
      if (floors.value.length) {
        activeFloorId.value = Number(floors.value[0].id)
        loadSmart()
      }
    })
    .catch(() => {
      floors.value = []
      smartRooms.value = []
      ElMessage.error('加载楼层失败')
    })
    .finally(() => { loading.value = false })
}

function loadSmart() {
  const floorId = Number(activeFloorId.value)
  if (!floorId) return
  axios.get('/bed/smart', { params: { floorId } })
    .then(res => {
      smartRooms.value = res.data?.code === 200 ? (res.data?.data || []) : []
    })
    .catch(() => {
      smartRooms.value = []
      ElMessage.error('加载智能床位失败')
    })
}
</script>

<style scoped>
.floor-tabs { margin-bottom: 20px; }
.smart-room { margin-bottom: 24px; }
.smart-room-header {
  display: flex; flex-wrap: wrap; align-items: center; gap: 16px;
  margin-bottom: 12px; color: #666; font-size: 14px;
}
.room-no { font-weight: 600; color: #333; }
.device-icons { display: flex; gap: 8px; color: #409eff; }
.env-info { white-space: nowrap; }
.smart-bed-card { min-height: 140px; }
.smart-bed-card.alert { background: #fef0f0; border-color: #fbc4c4; }
.bed-head { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
.device-mini { display: flex; gap: 4px; color: #409eff; }
.bed-body { display: flex; gap: 12px; align-items: flex-start; }
.bed-icon { font-size: 14px; line-height: 1.2; color: #409eff; font-weight: 600; min-width: 36px; }
.bed-info { flex: 1; font-size: 13px; color: #666; }
.status-label { color: #409eff; margin: 4px 0; }
.empty-bed { color: #999; margin-top: 4px; }
</style>