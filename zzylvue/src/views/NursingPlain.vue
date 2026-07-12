<template>
  <!--  添加对话框组件，实现护理项目的新增  -->
  <el-dialog
      v-model="nursingPlainDialogVisible"
      title="护理计划信息"
      width="60%">

    <el-form  label-width="auto"
              style="margin-left:5%;margin-right:5%;max-width: 90%">
      <el-form-item label="护理计划名称">
        <el-input  v-model="nursingPlainForm.plainname"/>
      </el-form-item>
      <el-form-item label="护理项目">
        <el-table
            :data="nursingItemList"
            style="width: 100%" :fit="true">

          <el-table-column  label="计划名称">
             <template #default="scope">
               <el-input :model-value="scope.row.itemname"
                         disabled></el-input>
             </template>
          </el-table-column>
          <el-table-column label="服务时间">

            <template #default="scope">
              <el-time-picker style="width: 75%"
                              v-model="nursingPlainForm.plainitem.hlsj[scope.$index]"
                              :picker-options="{
                 selectableRange: '00:00:00 - 23:59:59' // 限定可选择的时间范围
                }"
                              format="HH:mm"
                              placeholder="选择">
              </el-time-picker>

            </template>

          </el-table-column>
          <el-table-column prop="unit" label="执行周期">
            <template #default="scope">
              <el-select  placeholder="选择" style="width: 70%"
                          v-model="nursingPlainForm.plainitem.hlzq[scope.$index]">
                <el-option label="天" value="天"/>
                <el-option label="周" value="周" />
                <el-option label="月" value="月" />
                <el-option label="季" value="季" />

              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="执行频次">
            <template #default="scope">
              <el-input size="small" style="width:40%"
                v-model="nursingPlainForm.plainitem.hlpc[scope.$index]"></el-input>
            </template>
          </el-table-column>

          <el-table-column label="操作">
            <template #default="scope">
              <el-button type="primary" size="small" @click="addItem(scope)">+</el-button>
              <el-button type="primary" size="small" @click="subItem(scope)">-</el-button>
            </template>

          </el-table-column>
        </el-table>
      </el-form-item>

      <el-form-item style="margin-left: 40%">
        <template #default="scope">
          <el-button type="primary" round @click="saveNursingPlain">确认</el-button>
          <el-button type="warning" round @click="closeNursingPlain">取消</el-button>
        </template>
      </el-form-item>
    </el-form>

  </el-dialog>

  <!--  table列表组件  -->
  护理项名称&nbsp;:&nbsp;
  <el-input style="width:20%;margin-right: 20px" v-model="plainCondForm.plainname"/>
  状态&nbsp;:&nbsp;
  <el-select style="width:20%;margin-right: 20px"
             placeholder="请选择" v-model="plainCondForm.islock">
    <el-option value="启用">启用</el-option>
    <el-option value="禁用">禁用</el-option>
  </el-select>
  <el-button type="primary" @click="loadNursingPlainPageList(1)">搜索</el-button>
  <hr/>

  <div style="text-align: left">
    <el-button type="primary"
               @click="openNursingPlainDialog">新增护理计划</el-button>
  </div>

    <!-- 护理计划列表 -->
  <el-table
      :data="nursingPlainList"
      style="width: 100%" :fit="true">
    <el-table-column type="index" width="50" />
    <el-table-column prop="plainname" label="计划名称"/>
    <el-table-column prop="createuser" label="创建人" width="180"/>
    <el-table-column prop="createtime" label="创建时间"/>
    <el-table-column prop="islock" label="状态" width="180"/>

    <el-table-column label="操作">
      <template #default="scope">
        <el-button type="warning" size="small"
                   @click="delNursingItem(scope.row.id)"
                   :disabled="scope.row.flag==1?true:false">删除</el-button>
        <el-button type="primary" size="small"
                   @click="showNursingPlainInfo(scope.row)"
                   :disabled="scope.row.flag==1?true:false">编辑</el-button>
        <el-button type="success" size="small"
                   @click="updateIsLock(scope.row)"
                   :disabled="scope.row.flag==1?true:false">
          {{scope.row.islock=="启用"?"禁用":"启用"}}
        </el-button>
        <el-button @click="showNursingPlain(scope.row)">
          查看
        </el-button>
      </template>
    </el-table-column>



  </el-table>
  <!--   分页组件 -->
  <el-pagination
      size="small"
      background
      layout="prev, pager, next"
      :total="total" @change="loadNursingPlainPageList"/>

<!-- //////////////////////查看护理计划和护理项对话框/////////////////////// -->
  <!--  添加对话框组件，实现护理项目的新增  -->
  <el-dialog
      v-model="showPlainDialogVisible"
      title="护理计划信息"
      width="60%">

        <div style="text-align: left;font-size: 18px">
          护理计划名称:
          {{showPlainForm.plainname}}
        </div>

        <el-table
            :data="showPlainForm.itemList"
            style="width: 100%" :fit="true">
          <el-table-column prop="itemname"  label="护理项名称"/>
          <el-table-column prop="hlsj" label="服务时间"/>
          <el-table-column prop="hlzq" label="执行周期"/>
          <el-table-column prop="hlpc" label="执行频次"/>

        </el-table>


  </el-dialog>
</template>

<script setup>

import {onMounted, reactive, ref} from "vue";
 import axios from "axios";
 //声明新增护理计划对话框状态
 const nursingPlainDialogVisible=ref(false);
 //声明变量保存处理新增和更新护理计划接口地址
 var url=null;
 //声明函数打开新增护理计划对话框
 function openNursingPlainDialog(){
   nursingPlainDialogVisible.value=true;
   url="saveNursingPlain";
   //调用函数加载护理项
   laodNursingItemPageList(1);

 }
 //声明护理项目列表集合数据
 const nursingItemList=ref([]);
 //定义函数分页加载护理项目列表
 function laodNursingItemPageList(pageNum){
   var condForm={
     pageNum:pageNum
   }
   axios.post("/nursingItemPage",condForm)
       .then(response=>{
         nursingItemList.value=response.data.nursimgItems;
         total.value=response.data.total;
       })
       .catch(error=>{
         console.log(error);
       })
 }
 //声明添加护理计划表单对象
 const nursingPlainForm=reactive({
   id:'',
   plainname:'',
   plainitem:{
     itemid:[],
     hlmc:[],
     hlsj:[],
     hlpc:[],
     hlzq:[]
   }
 });
 //声明集合对象保存需要添加为某个护理计划的护理项目数据
 const plainItemList=[];
 //定义函数实现护理计划中添加某个护理项
 function addItem(scope){
    var itemname=scope.row.itemname;
    var itemid=scope.row.id;
    var index=scope.$index;
    console.log(itemname);
    console.log(index);
    if(nursingPlainForm.plainitem.hlpc[index]>=1){
      plainItemList.push({
        hlmc:itemname,
        itemid:itemid,
        hlsj:nursingPlainForm.plainitem.hlsj[index],
        hlpc:nursingPlainForm.plainitem.hlpc[index],
        hlzq:nursingPlainForm.plainitem.hlzq[index]

      });
    }
    console.log(plainItemList);
 }

 //定义函删除某个护理计划下的某个护理项
  function subItem(scope){
    var index=scope.$index;
    //nursingPlainForm.plainitem.下所有数组清空
    nursingPlainForm.plainitem.hlsj[index]='';
    nursingPlainForm.plainitem.hlpc[index]='';
    nursingPlainForm.plainitem.hlzq[index]='';
    //从plainItemList数组中删除对应的护理项对象
    plainItemList.splice(index,1)
    console.log(plainItemList);
  }
  //定义函数发生请求，保存护理计划
  function saveNursingPlain(){
    nursingPlainForm.plainItemList=plainItemList;
    axios.post(url,nursingPlainForm)
    .then(response=>{
      if(response.data.code==200){
        //刷新列表
        loadNursingPlainPageList(1);
        //关闭对话框
        nursingPlainDialogVisible.value=false;
        //清空表单
        cleanNursingPlainForm();
      }
    })
    .catch(error=>{
      console.log(error);
    })

  }
  //声明护理计划列表和总记录数
  const nursingPlainList=ref([]);
  const total=ref(0);
  //声明分页查询提交查询参数的表单对象数据
  const plainCondForm=reactive({
    plainname:'',
    islock:'',
    pageNum:1,
    pageSize:10
  });
  //定义函数加载护理计划分页数据
  function loadNursingPlainPageList(pageNum){
    plainCondForm.pageNum=pageNum;
    axios.post("pageList",plainCondForm)
    .then(response=>{
      nursingPlainList.value=response.data.nursingPlains;
      total.value=response.data.total;
    })
    .catch(error=>{
      console.log(error);
    })
  }
  onMounted(function(){
    loadNursingPlainPageList(1);
  })
  //定义函数清空表单
  function cleanNursingPlainForm(){
    nursingPlainForm.id='';
    nursingPlainForm.plainitem='';
    // nursingPlainForm.plainitem.hlmc=[];
    // nursingPlainForm.plainitem.hlsj=[];
    // nursingPlainForm.plainitem.hlpc=[];
    // nursingPlainForm.plainitem.hlzq=[];
    nursingItemList.value=[];

  }
  //声明showPlainDialogVisible表示查看护理计划对回框
  const showPlainDialogVisible=ref(false);
  //声明对象保存需要查看的护理计划和护理项信息
  const showPlainForm=reactive({
    plainname:'',
    itemList:[]
  });
  //定义函数打开查看护理计划对话框
  function showNursingPlain(row){
    showPlainDialogVisible.value=true;
    var plainid=row.id;
    var plainname=row.plainname;
    showPlainForm.plainname=plainname;
    axios.get("/queryPlainItem?plainid="+plainid)
    .then(response=>{
      showPlainForm.itemList=response.data;
    })
    .catch(error=>{
      console.log(error);
    })
  }
  ///////////////定义函数打开护理计划回显对话框//////////////////
  function showNursingPlainInfo(row){
    nursingPlainForm.id=row.id;
    nursingPlainForm.plainname=row.plainname;
    url="updateNursingPlain";
    laodNursingItemPageList(1);
    //修改对话框状态
    nursingPlainDialogVisible.value=true;
  }
  //定义函数取消新增或者更新，关闭对话框
  function closeNursingPlain(){
    //修改对话框状态
    nursingPlainDialogVisible.value=false;
    cleanNursingPlainForm();
  }
</script>

<style scoped>

</style>