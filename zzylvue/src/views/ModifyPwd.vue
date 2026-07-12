<template>
  <el-form label-width="auto" style="width:50%;margin-left: 25%;margin-top: 10%">
    <el-form-item label="原始密码" >
      <div class="input-wrapper">
        <el-input type="password" v-model="pwdForm.oldpwd"/>
        <span style="color: red" class="required-mark">*</span>
      </div>
    </el-form-item>
    <el-form-item label="新密码">
      <div class="input-wrapper">
        <el-input type="password"  v-model="pwdForm.newpwd"/>
        <span style="color: red" class="required-mark">*</span>
      </div>
    </el-form-item>
    <el-form-item label="确认密码">
      <div class="input-wrapper">
        <el-input type="password"  v-model="pwdForm.newpwd2"/>
        <span style="color: red" class="required-mark">*</span>
      </div>
    </el-form-item>
    <el-form-item style="margin-left: 30%">
      <el-button type="primary" round @click="updateUserPwd">确认</el-button>
      <el-button type="success" round @click="cleanPwdForm">重置</el-button>

    </el-form-item>
  </el-form>
</template>

<script setup>
  //声明用户密码更新的表单对象
  import {reactive} from "vue";
  import {ElMessage} from "element-plus";
  import axios from "axios";

  const pwdForm=reactive({
    oldpwd:'',
    newpwd:'',
    newpwd2:''
  });
  //定义函数提交密码跟新请求
  function updateUserPwd(){
    //验证两次新密码是否相同
    if(pwdForm.newpwd!=pwdForm.newpwd2){
      ElMessage("新密码两次输入不一致......")
      return;
    }
    axios.post("/updatePwd",pwdForm)
    .then(response=>{
      if(response.data.code==200){
        //清空表单
        cleanPwdForm();
      }
      ElMessage(response.data.msg);
    })
    .catch(error=>{
      console.log(error);
    })
  }
  //定义清空表单的函数
  function cleanPwdForm(){
    pwdForm.oldpwd="";
    pwdForm.newpwd="";
    pwdForm.newpwd2="";

  }
</script>

<style scoped>
.input-wrapper {
  display: flex;
  align-items: center; /* 使星号和输入框垂直居中 */
  width:80%
}
.required-mark {
  margin-right: 10px; /* 根据需要调整间距 */
}
</style>