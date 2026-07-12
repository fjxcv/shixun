const { defineConfig } = require('@vue/cli-service')
const path = require('path')

const apiProxy = {
  target: 'http://localhost:8080',
  changeOrigin: true
}

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 80,
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: (error) => {
          const message = error?.message || String(error)
          // Element Plus 等组件触发的浏览器 ResizeObserver 已知告警，不影响功能
          if (/ResizeObserver loop/i.test(message)) return false
          return true
        }
      }
    },
    proxy: {
      '/login': apiProxy,
      '/logout': apiProxy,
      '/loadInfo': apiProxy,
      '/showInfo': apiProxy,
      '/updateUser': apiProxy,
      '/updatePwd': apiProxy,
      '/sysMenus': apiProxy,
      '/upload': apiProxy,
      '/uploads': apiProxy,
      '/checkin': apiProxy,
      '/checkout': apiProxy,
      '/workbench': apiProxy,
      '/customer': apiProxy,
      '/contract': apiProxy,
      '/leave': apiProxy,
      '/bed': apiProxy,
      '/roomType': apiProxy,
      '/reservation': apiProxy,
      '/visit': apiProxy,
      '/collab': apiProxy,
      '/chat01': apiProxy,
      '/chat02': apiProxy,
      '/saveNursingItme': apiProxy,
      '/nursingItemPage': apiProxy,
      '/updateNursingItme': apiProxy,
      '/deleteNursingItme': apiProxy,
      '/saveLevel': apiProxy,
      '/levelList': apiProxy,
      '/saveNursingPlain': apiProxy,
      '/pageList': apiProxy,
      '/updateNursingPlain': apiProxy,
      '/plainList': apiProxy,
      '/totalPay': apiProxy,
      '/queryPlainItem': apiProxy
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  }
})
