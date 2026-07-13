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
          // Element Plus ????????? ResizeObserver ??????????
          if (/ResizeObserver loop/i.test(message)) return false
          return true
        }
      }
    },
    proxy: {
      '/login': apiProxy,
      '/register': apiProxy,
      '/resetPwd': apiProxy,
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
      // ????????????? /levelList??????????
      '/saveLevel': apiProxy,
      '/levelList': apiProxy
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
