<template>
  <PageCard>
    <div class="bed-toolbar">
      <el-radio-group v-if="floors.length" v-model="activeFloorId" @change="loadRooms">
        <el-radio-button v-for="f in floors" :key="f.id" :value="f.id">{{ f.name }}</el-radio-button>
      </el-radio-group>
      <div class="bed-legend">
        <span><i class="dot idle" />空闲</span>
        <span><i class="dot occupied" />已入住</span>
        <span><i class="dot leave" />请假中</span>
      </div>
      <div class="bed-actions">
        <el-button type="danger" plain @click="addFloor">+ 新增楼层</el-button>
        <el-button type="primary" @click="openRoomDialog()">+ 新增房间</el-button>
      </div>
    </div>

    <el-empty v-if="!floors.length" description="暂无楼层，请先新增楼层" />

    <el-row v-else :gutter="16">
      <el-col v-for="item in roomList" :key="item.room.id" :span="8" style="margin-bottom: 16px">
        <el-card class="room-card">
          <div class="room-header">
            <div>
              <div>房间号：{{ item.room.roomNo }}</div>
              <div class="room-type">房间类型：{{ item.room.roomTypeName }}</div>
            </div>
            <div class="room-ops">
              <el-button link type="danger" @click="deleteRoom(item.room.id)">删除</el-button>
              <el-button link type="primary" @click="openRoomDialog(item.room)">编辑</el-button>
              <el-button type="primary" size="small" @click="openBedDialog(item.room)">新增床位</el-button>
            </div>
          </div>
          <div v-if="item.beds.length" class="bed-grid">
            <div v-for="bed in item.beds" :key="bed.id" :class="['bed-item', bedStatusClass(bed.status)]">
              <div class="bed-no">床位号：{{ bed.bedNo }}</div>
              <div class="bed-elder">{{ bed.status === '空闲' ? '空闲' : (bed.elderName || '--') }}</div>
              <div class="bed-actions-mini">
                <el-button link size="small" @click="openBedDialog(item.room, bed)">编辑</el-button>
                <el-button link size="small" type="danger" @click="deleteBed(bed.id)">删除</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="当前房间没有安排床位" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="roomDialog" :title="roomForm.id ? '编辑房间' : '新增房间'" width="480px">
      <el-form label-width="90px">
        <el-form-item label="房间号"><el-input v-model="roomForm.roomNo" maxlength="5" show-word-limit /></el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="roomForm.roomTypeName" style="width:100%">
            <el-option label="四人间" value="四人间" />
            <el-option label="三人间" value="三人间" />
            <el-option label="两人间" value="两人间" />
            <el-option label="普通单人间" value="普通单人间" />
            <el-option label="豪华单人间" value="豪华单人间" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialog=false">取消</el-button>
        <el-button type="primary" @click="saveRoom">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bedDialog" :title="bedForm.id ? '编辑床位' : '新增床位'" width="480px">
      <el-form label-width="90px">
        <el-form-item label="床位号"><el-input v-model="bedForm.bedNo" maxlength="10" show-word-limit /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="bedForm.status" style="width: 100%" @change="onBedStatusChange">
            <el-option label="空闲" value="空闲" />
            <el-option label="已入住" value="已入住" />
            <el-option label="请假中" value="请假中" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="bedForm.status !== '空闲'" label="老人姓名"><el-input v-model="bedForm.elderName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bedDialog=false">取消</el-button>
        <el-button type="primary" @click="saveBed">确定</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageCard from '@/components/PageCard.vue'

const floors = ref([])
const activeFloorId = ref(null)
const roomList = ref([])
const roomDialog = ref(false)
const bedDialog = ref(false)
const roomForm = reactive({ id: null, floorId: null, roomNo: '', roomTypeName: '四人间' })
const bedForm = reactive({ id: null, roomId: null, bedNo: '', elderName: '', status: '空闲' })

onMounted(() => initFloors())

function bedStatusClass(status) {
  return status === '已入住' ? 'occupied' : status === '请假中' ? 'leave' : 'idle'
}

function onBedStatusChange(status) {
  if (status === '空闲') bedForm.elderName = ''
}

async function initFloors() {
  try {
    const res = await axios.get('/bed/floors')
    if (res.data?.code !== 200) {
      floors.value = []
      return
    }
    floors.value = res.data?.data || []
    if (floors.value.length) {
      activeFloorId.value = Number(floors.value[0].id)
      loadRooms()
    }
  } catch {
    floors.value = []
    ElMessage.error('加载楼层失败')
  }
}

function loadRooms() {
  const floorId = Number(activeFloorId.value)
  if (!floorId) return
  axios.get('/bed/rooms', { params: { floorId } }).then(res => {
    if (res.data?.code !== 200) {
      roomList.value = []
      return
    }
    roomList.value = (res.data?.data || [])
      .filter(item => item && item.room)
      .map(item => ({ room: item.room, beds: item.beds || [] }))
  }).catch(() => {
    roomList.value = []
    ElMessage.error('加载房间失败')
  })
}

function addFloor() {
  const nums = floors.value.map(f => parseInt(String(f.name).replace(/\D/g, ''), 10)).filter(n => !isNaN(n))
  const next = (nums.length ? Math.max(...nums) : 0) + 1
  const name = next + '楼'
  axios.post('/bed/floor/save', { name, sortNum: next }).then(res => {
    if (res.data?.code === 200) {
      ElMessage.success('新增成功')
      initFloors()
    } else {
      ElMessage.warning(res.data?.msg || '新增失败')
    }
  }).catch(() => ElMessage.error('新增楼层失败'))
}

function openRoomDialog(room) {
  Object.assign(roomForm, room || { id: null, floorId: activeFloorId.value, roomNo: '', roomTypeName: '四人间' })
  roomDialog.value = true
}

function saveRoom() {
  if (!roomForm.roomNo) {
    ElMessage.warning('请输入房间号')
    return
  }
  roomForm.floorId = Number(activeFloorId.value)
  axios.post('/bed/room/save', roomForm).then(res => {
    if (res.data?.code === 200) {
      ElMessage.success('保存成功')
      roomDialog.value = false
      loadRooms()
    } else {
      ElMessage.error(res.data?.msg || '保存失败')
    }
  }).catch(() => ElMessage.error('保存失败'))
}

function deleteRoom(id) {
  ElMessageBox.confirm('删除房间将同时删除其床位，确认继续？', '提示', { type: 'warning' })
    .then(() => axios.get('/bed/room/delete', { params: { id } }))
    .then(res => {
      if (!res) return
      if (res.data?.code === 200) {
        ElMessage.success('删除成功')
        loadRooms()
      } else ElMessage.error(res.data?.msg || '删除失败')
    })
    .catch(err => { if (err !== 'cancel' && err !== 'close') ElMessage.error('删除失败') })
}

function openBedDialog(room, bed) {
  Object.assign(bedForm, bed || { id: null, roomId: room.id, bedNo: '', elderName: '', status: '空闲' })
  bedDialog.value = true
}

function saveBed() {
  if (!bedForm.bedNo) {
    ElMessage.warning('请输入床位号')
    return
  }
  if (bedForm.status !== '空闲' && !bedForm.elderName) {
    ElMessage.warning('已入住/请假中必须填写老人姓名')
    return
  }
  axios.post('/bed/bed/save', bedForm).then(res => {
    if (res.data?.code === 200) {
      ElMessage.success('保存成功')
      bedDialog.value = false
      loadRooms()
    } else {
      ElMessage.error(res.data?.msg || '保存失败')
    }
  }).catch(() => ElMessage.error('保存失败'))
}

function deleteBed(id) {
  axios.get('/bed/bed/delete', { params: { id } }).then(res => {
    if (res.data?.code === 200) {
      ElMessage.success('删除成功')
      loadRooms()
    } else ElMessage.error(res.data?.msg || '删除失败')
  }).catch(() => ElMessage.error('删除失败'))
}
</script>

<style scoped>
.bed-toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 16px; margin-bottom: 16px; }
.bed-legend { display: flex; gap: 16px; color: #666; font-size: 13px; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; }
.dot.idle { background: #ccc; }
.dot.occupied { background: #67c23a; }
.dot.leave { background: #e6a23c; }
.bed-actions { margin-left: auto; }
.room-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.room-type { color: #999; font-size: 13px; }
.bed-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.bed-item { border: 1px solid #eee; border-radius: 4px; padding: 8px; font-size: 13px; }
.bed-item.occupied { border-color: #b3e19d; background: #f0f9eb; }
.bed-item.leave { border-color: #f3d19e; background: #fdf6ec; }
.bed-elder { color: #666; margin: 4px 0; }
</style>