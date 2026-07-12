# -*- coding: utf-8 -*-
from pathlib import Path

ZH = {
    "logo_icon": "\u4e2d",
    "logo_title": "\u4e2d\u5dde\u517b\u8001",
    "admin": "\u7ba1\u7406\u5458",
    "change_pwd": "\u4fee\u6539\u5bc6\u7801",
    "logout": "\u9000\u51fa\u767b\u5f55",
    "old_pwd": "\u539f\u5bc6\u7801",
    "new_pwd": "\u65b0\u5bc6\u7801",
    "confirm_new_pwd": "\u786e\u8ba4\u65b0\u5bc6\u7801",
    "please_input": "\u8bf7\u8f93\u5165",
    "cancel": "\u53d6\u6d88",
    "confirm": "\u786e\u5b9a",
    "pwd_mismatch": "\u4e24\u6b21\u65b0\u5bc6\u7801\u8f93\u5165\u4e0d\u4e00\u81f4",
    "change_ok": "\u4fee\u6539\u6210\u529f",
}

MAIN_INDEX = f"""<template>
  <el-container class="main-layout">
    <el-header class="top-header" height="56px">
      <div class="logo-area">
        <div class="logo-icon">{ZH['logo_icon']}</div>
        <div class="logo-text">
          <div class="logo-title">{ZH['logo_title']}</div>
          <div class="logo-sub">ZHONG ZHOU YANG LAO</div>
        </div>
      </div>
      <div class="top-menu">
        <span
          v-for="item in topMenus"
          :key="item.module"
          :class="['top-menu-item', {{ active: activeModule === item.module }}]"
          @click="switchModule(item)"
        >{{{{ item.name }}}}</span>
      </div>
      <div class="top-right">
        <el-dropdown trigger="click">
          <span class="user-dropdown">
            <el-avatar :size="28" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span>{{{{ realName || '{ZH['admin']}' }}}}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="pwdDialogVisible = true">{ZH['change_pwd']}</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">{ZH['logout']}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <el-aside v-if="sideMenus.length" :width="collapsed ? '64px' : '200px'" class="side-aside">
        <el-menu
          :default-active="route.path"
          :collapse="collapsed"
          router
          class="side-menu"
        >
          <template v-for="(item, idx) in sideMenus" :key="idx">
            <el-sub-menu v-if="item.children && !item.path" :index="item.name + idx">
              <template #title>{{{{ item.name }}}}</template>
              <el-menu-item
                v-for="sub in item.children"
                :key="sub.path"
                :index="sub.path"
              >{{{{ sub.name }}}}</el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.path">{{{{ item.name }}}}</el-menu-item>
          </template>
        </el-menu>
        <div class="aside-footer">
          <el-icon class="footer-icon" @click="handleLogout"><SwitchButton /></el-icon>
          <el-icon class="footer-icon" @click="collapsed = !collapsed"><Fold /></el-icon>
        </div>
      </el-aside>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <div class="page-footer">Copyright @ 2019-2020 Tencent. All Rights Reserved</div>

    <el-dialog v-model="pwdDialogVisible" title="{ZH['change_pwd']}" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="{ZH['old_pwd']}" required>
          <el-input v-model="pwdForm.oldpwd" type="password" show-password placeholder="{ZH['please_input']}" />
        </el-form-item>
        <el-form-item label="{ZH['new_pwd']}" required>
          <el-input v-model="pwdForm.newpwd" type="password" show-password placeholder="{ZH['please_input']}" />
        </el-form-item>
        <el-form-item label="{ZH['confirm_new_pwd']}" required>
          <el-input v-model="pwdForm.newpwd2" type="password" show-password placeholder="{ZH['please_input']}" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">{ZH['cancel']}</el-button>
        <el-button type="primary" @click="updateUserPwd">{ZH['confirm']}</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import {{ computed, onMounted, reactive, ref, watch }} from 'vue'
import {{ useRoute, useRouter }} from 'vue-router'
import axios from 'axios'
import {{ ElMessage }} from 'element-plus'
import {{ topMenus, findModuleByPath, getSideMenus }} from '@/config/menu'

const route = useRoute()
const router = useRouter()
const realName = ref('{ZH['admin']}')
const collapsed = ref(false)
const pwdDialogVisible = ref(false)
const pwdForm = reactive({{ oldpwd: '', newpwd: '', newpwd2: '' }})

const activeModule = ref('workbench')
const sideMenus = computed(() => getSideMenus(activeModule.value))

watch(() => route.path, (path) => {{
  activeModule.value = findModuleByPath(path)
}}, {{ immediate: true }})

onMounted(() => {{
  loadLoginUserInfo()
}})

function switchModule(item) {{
  activeModule.value = item.module
  const flat = []
  const walk = (list) => list.forEach(i => {{
    if (i.path) flat.push(i)
    if (i.children) walk(i.children)
  }})
  walk(item.children || [])
  router.push(item.children?.length ? (flat[0]?.path || item.path) : item.path)
}}

function loadLoginUserInfo() {{
  axios.get('/loadInfo').then(res => {{
    if (res.data?.realname) realName.value = res.data.realname
    else if (res.data?.uname) realName.value = res.data.uname
  }}).catch(() => {{}})
}}

function updateUserPwd() {{
  if (pwdForm.newpwd !== pwdForm.newpwd2) {{
    ElMessage.warning('{ZH['pwd_mismatch']}')
    return
  }}
  axios.post('/updatePwd', pwdForm).then(res => {{
    ElMessage.success(res.data.msg || '{ZH['change_ok']}')
    if (res.data.code === 200) {{
      pwdForm.oldpwd = ''
      pwdForm.newpwd = ''
      pwdForm.newpwd2 = ''
      pwdDialogVisible.value = false
    }}
  }})
}}

function handleLogout() {{
  axios.post('/logout').finally(() => {{
    router.replace('/')
  }})
}}
</script>

<style scoped>
.main-layout {{
  min-height: 100vh;
  background: #f5f7fa;
}}
.top-header {{
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 20px;
}}
.logo-area {{
  display: flex;
  align-items: center;
  min-width: 160px;
}}
.logo-icon {{
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9a56, #4ecdc4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 8px;
}}
.logo-title {{ font-size: 16px; font-weight: 600; color: #333; }}
.logo-sub {{ font-size: 10px; color: #999; }}
.top-menu {{
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  padding: 0 16px;
  overflow: hidden;
}}
.top-menu-item {{
  cursor: pointer;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  padding: 4px 0;
}}
.top-menu-item.active {{ color: #409eff; font-weight: 600; }}
.top-menu-item:hover {{ color: #409eff; }}
.top-right {{
  display: flex;
  align-items: center;
  gap: 12px;
}}
.user-dropdown {{
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}}
.side-aside {{
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}}
.side-menu {{ border-right: none; flex: 1; }}
.aside-footer {{
  display: flex;
  justify-content: space-around;
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}}
.footer-icon {{ cursor: pointer; font-size: 18px; color: #666; }}
.main-content {{ padding: 16px; }}
.page-footer {{
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 12px;
  background: #f5f7fa;
}}
</style>
"""

TARGETS = [
    Path(r"D:\vsc-maven\zzyl-project\zzylvue\src\views\MainIndex.vue"),
    Path(r"D:\vsc-maven\zzylvue\src\views\MainIndex.vue"),
]

CHECKIN_SOURCE = Path(r"D:\vsc-maven\zzyl-project\zzylvue\src\views\CheckinApply.vue")
CHECKIN_TARGETS = [
    CHECKIN_SOURCE,
    Path(r"D:\vsc-maven\zzylvue\src\views\CheckinApply.vue"),
]

SYNC_FILES = [
    "src/components/CheckinSteps.vue",
    "src/config/menu.js",
    "src/views/CheckinProcess.vue",
    "src/views/CheckoutApply.vue",
]


def write_utf8(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")
    print(f"Wrote {path}")


def main():
    for target in TARGETS:
        write_utf8(target, MAIN_INDEX)

    checkin_text = CHECKIN_SOURCE.read_text(encoding="utf-8")
    for target in CHECKIN_TARGETS:
        write_utf8(target, checkin_text)

    src_root = Path(r"D:\vsc-maven\zzyl-project\zzylvue")
    alt_root = Path(r"D:\vsc-maven\zzylvue")
    for rel in SYNC_FILES:
        src = src_root / rel
        if not src.exists():
            print(f"SKIP missing {src}")
            continue
        text = src.read_text(encoding="utf-8")
        write_utf8(alt_root / rel, text)


if __name__ == "__main__":
    main()
