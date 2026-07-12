<template>

    <!--  添加对话框组件，实现护理项目的新增  -->
    <el-dialog
        v-model="nursingItemDialogVisible"
        title="护理项目信息"
        width="40%">

      <el-form  label-width="auto"
               style="margin-left:5%;margin-right:5%;max-width: 600px">
        <el-form-item label="护理项目名称">
          <el-input  v-model="nursingItemForm.itemname" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="nursingItemForm.price"
                           :precision="2" :step="0.1" :max="10" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input  v-model="nursingItemForm.unit" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number
              v-model="nursingItemForm.sort"
              :min="1"
              :max="10"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="nursingItemForm.islock">
            <el-radio value="禁用">禁用</el-radio>
            <el-radio value="启用">启用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="照片">
          <!-- action后台处理文件上传请求的url接口地址 -->
          <el-upload
              class="avatar-uploader"
              action="http://localhost:8080/upload"
              :show-file-list="false"
              name="mf"
              :on-success="handleNursingPhotoSuccess">
            <img v-if="imageUrl" :src="imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="护理项目描述">
          <el-input v-model="nursingItemForm.description" type="textarea" />
        </el-form-item>

        <el-form-item style="margin-left: 40%">
          <el-button type="primary" round @click="saveNursingItem">确认</el-button>
          <el-button type="warning" round @click="saveUserInfo">取消</el-button>
        </el-form-item>
      </el-form>

    </el-dialog>
    <!--  table列表组件  -->
    护理项名称&nbsp;:&nbsp;
    <el-input style="width:20%;margin-right: 20px" v-model="condForm.itemname"/>
    状态&nbsp;:&nbsp;
      <el-select style="width:20%;margin-right: 20px"
                 placeholder="请选择" v-model="condForm.islock">
        <el-option value="启用">启用</el-option>
        <el-option value="禁用">禁用</el-option>
      </el-select>
    <el-button type="primary" @click="loadNursingItemList">搜索</el-button>
    <hr/>
    <div style="text-align: left">
      <el-button type="primary"
                 @click="openNursingItemDialog">添加护理项目</el-button>
    </div>
    <el-table
        :data="nursingItemList"
        style="width: 100%" :fit="true">
      <el-table-column type="index" width="50" />
      <el-table-column prop="itemname" label="名称"/>
      <el-table-column prop="price" label="价格"  width="100"/>
      <el-table-column prop="unit" label="单位"  width="100"/>
      <el-table-column prop="sort" label="排序"  width="100"/>
      <el-table-column  label="图片">
        <template #default="scope">
          <img :src="scope.row.image" width="35px" height="35px"/>
        </template>
      </el-table-column>
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
      <el-table-column label="操作">
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
        :total="total" @change="doNusringItemPage"/>
</template>

<script setup>

import {onMounted, reactive, ref} from "vue";
import axios from "axios";
import {ElMessage} from "element-plus";
  //声明对话框状态
  const nursingItemDialogVisible=ref(false);
  //声明护理项目表单对象
  const nursingItemForm=reactive({
    id:'',
    itemname:'',
    price:'',
    unit:'',
    sort:'',
    islock:'',
    image:'',
    description:''
  });
  //声明变量保存处理添加和更新请求的url接口
  var url=null;
  //定义函数打开护理项目信息对话框
  function openNursingItemDialog(){
    //当打开信息护理项目信息对话框的时候
    url="/saveNursingItme";
    nursingItemDialogVisible.value=true;
  }
  //声明图片回显的url
  const imageUrl=ref(null);
  //定义函发送图片上传请求，处理图片上传后的回显
  function handleNursingPhotoSuccess(path){
    imageUrl.value=path;
    nursingItemForm.image=path;
  }
  //定义函数发生请求，保存护理项信息
  function saveNursingItem(){
    axios.post(url,nursingItemForm)
    .then(response=>{
      if(response.data.code==200){
        //关闭对话框，清空表单
        nursingItemDialogVisible.value=false;
        cleanNursingItemForm();
        //调用函数刷新列表
        doNusringItemPage(1);
      }
      ElMessage(response.data.msg)
    })
    .catch(error=>{
      console.log(error);
    });
  }
  //定义函数清空对话框
  function cleanNursingItemForm(){
    nursingItemForm.id="";
    nursingItemForm.itemname="";
    nursingItemForm.price="";
    nursingItemForm.sort="";
    nursingItemForm.unit="";
    nursingItemForm.image="";
    nursingItemForm.description="";
    nursingItemForm.islock="";

  }
  //声明查询条件表单对象
  const condForm=reactive({
    itemname:'',
    islock:'',
    pageNum:1,
    pageSize:10
  })
  //声明检查项列表
  const nursingItemList=ref([]);
  //声明数据库总记录数
  const total=ref(0);
  //定义函数加载护理项列表
  function loadNursingItemList(){
    axios.post("/nursingItemPage",condForm)
    .then(response=>{
      nursingItemList.value=response.data.nursimgItems;
      total.value=response.data.total;
    })
    .catch(error=>{
      console.log(error);
    })
  }
  //加载调用函数
  onMounted(function(){
    loadNursingItemList();
  })
  //定义分页按钮的会调用函数
  function doNusringItemPage(pageNum){
    condForm.pageNum=pageNum;
    loadNursingItemList();
  }
  /////////////////////////////////////////////////////////////////////////////////
  //定义函数回显当前护理项目信息
  function showNursingInfo(row){
    console.log(row);
    nursingItemForm.id=row.id;
    nursingItemForm.itemname=row.itemname;
    nursingItemForm.price=row.price;
    nursingItemForm.sort=row.sort;
    nursingItemForm.unit=row.unit;
    nursingItemForm.image=row.image;
    nursingItemForm.description=row.description;
    nursingItemForm.islock=row.islock;
    //打开对话框
    nursingItemDialogVisible.value=true;
    imageUrl.value=row.image;
    //当打开护理信息回显对话框
    url="/updateNursingItme";

  }
  //定义函数实现护理项删除
  function delNursingItem(id){
    axios.get("/deleteNursingItme?id="+id)
    .then(response=>{
      if(response.data.code==200){
        //刷新列表
        doNusringItemPage(1);
      }
      ElMessage(response.data.msg);
    })
    .catch(errpr=>{
      console.log(error);
    })
  }
  //定义函数实现状态的更新
  function updateIsLock(row){
    var islockForm={
      id:row.id
    }

    if(row.islock=="禁用"){
      islockForm.islock="启用"
    }else{
      islockForm.islock="禁用"
    }
    axios.post("/updateNursingItme",islockForm)
    .then(response=>{
      if(response.data.code==200){
        //刷新列表
        doNusringItemPage(1);
      }
      ElMessage(response.data.msg);
    })
    .catch(error=>{
      console.log(error);
    });
  }
</script>

<style scoped>

</style>