<template>

  <!--  新增护理等级对话框  -->
  <el-dialog
      v-model="nursingLevelDialogVisible"
      title="护理等级信息"
      width="40%">

    <el-form  label-width="auto"
              style="margin-left:5%;margin-right:5%;max-width: 600px">
      <el-form-item label="护理登记名称">
        <el-input  v-model="nursingLevelForm.levelname" />
      </el-form-item>
      <el-form-item label="护理执行计划">
        <el-select
            v-model="nursingLevelForm.plainid"
            placeholder="请选择"
              @change="doSelectChangeHandler">
          <el-option :value="item.id" v-for=" item in plainList">{{ item.plainname }}</el-option>

        </el-select>
      </el-form-item>
      <el-form-item label="护理费用">
        <el-input-number
            v-model="nursingLevelForm.money"
            :min="0"
            controls-position="right"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group  v-model="nursingLevelForm.islock">
          <el-radio value="禁用">禁用</el-radio>
          <el-radio value="启用">启用</el-radio>
        </el-radio-group>
      </el-form-item>


      <el-form-item label="登记说明">
        <el-input  type="textarea"  v-model="nursingLevelForm.description"/>
      </el-form-item>

      <el-form-item style="margin-left: 40%">
        <el-button type="primary" round @click="saveNursingLevel">确认</el-button>
        <el-button type="warning" round @click="saveUserInfo">取消</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>


  <!-- 打开新增对话框按钮组件 -->
  <div style="text-align: left">
    <el-button type="primary"
               @click="openNursingLevelDialog">新增护理等级</el-button>
  </div>
  <el-table
      :data="nursingLevelList"
      style="width: 100%" :fit="true">
    <el-table-column type="index" width="50" />
    <el-table-column prop="levelname" label="等级名称"/>
    <el-table-column prop="plainname" label="计划名称" />
    <el-table-column prop="money" label="费用" />
    <el-table-column prop="description" label="等级说明"  />
    <el-table-column prop="createuser" label="创建人"/>
    <el-table-column  label="状态">
      <template #default="scope" width="100">
          <span v-if="scope.row.islock=='启用'" style="color:dodgerblue">
            启用
          </span>
        <span v-else style="color:crimson">
            禁用
          </span>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="260">
      <template #default="scope">
        <el-button type="warning" size="small"
                   @click="delNursingItem(scope.row.id)">删除</el-button>
        <el-button type="primary" size="small"
                   @click="showNursingInfo(scope.row)">编辑</el-button>
        <el-button type="success" size="small" @click="updateIsLock(scope.row)">
          {{scope.row.islock=="启用"?"禁用":"启用"}}
        </el-button>
      </template>
    </el-table-column>
  </el-table>
  <!--   分页组件 -->
  <el-pagination
      size="small"
      background
      layout="prev, pager, next"
      :total="total" @change="doNusringLevelPage"/>
</template>

<script setup>

import {onMounted, reactive, ref} from "vue";
  import axios from "axios";
import {ElMessage} from "element-plus";
  //声明新增对话框状态
  const nursingLevelDialogVisible=ref(false);
  //声明函数打开新增对话框
  function openNursingLevelDialog(){
    nursingLevelDialogVisible.value=true;
    //加载护理计划列表
    loadNursingPlainList();
  }
  //声明护理计划列表集合
  const plainList=ref([]);
  //定义函数发生请求加载所有护理计划信息
  function loadNursingPlainList(){
    axios.get("/plainList")
    .then(response=>{
      plainList.value=response.data;
    })
    .catch(error=>{
      console.log(error);
    })
  }
  //声明进行表单数据绑定的对象
  const nursingLevelForm=reactive({
    id:'',
    levelname:'',
    plainid:'',
    money:'',
    islock:'',
    description:''
  });
  //定义护理计划下拉列表框回调函数,参数绑定选中的选项的id
  function  doSelectChangeHandler(id){
    axios.get("/totalPay?plainid="+id)
    .then(response=>{
      nursingLevelForm.money=response.data;
    })
    .catch(error=>{
      console.log(error);
    })
  }
  //定义函数发送请求保存护理等级
  function saveNursingLevel(){
    axios.post("/saveLevel",nursingLevelForm)
    .then(response=>{
      if(response.data.code==200){
        //关闭对话框
        nursingLevelDialogVisible.value=false;
        //刷新列表
        //清空表单
      }
      ElMessage(response.data.msg);
    })
    .catch(error=>{
      console.log(error);
    })
  }
  ///////////////////分页查询/////////////////////
  //声明集合列表和总记录数
  const nursingLevelList=ref([]);
  const total=ref(0);
  //声明对象封装查询条件参数
  const condLevelForm=reactive({
    levelname:'',
    islock:'',
    pageNum:1,
    pageSize:10
  });
  function loadNursingLevelList(pageNum){
    condLevelForm.pageNum=pageNum;
    axios.post("/levelList",condLevelForm)
    .then(response=>{
      nursingLevelList.value=response.data.nursingLevels;
      total.value=response.data.total;
    })
    .catch(error=>{
      console.log(error);
    })
  }
  onMounted(function (){
    loadNursingLevelList(1);
  })
  //定义分页按钮回调函数
  function doNusringLevelPage(pageNum){
    loadNursingLevelList(pageNum);
  }
</script>

<style scoped>

</style>