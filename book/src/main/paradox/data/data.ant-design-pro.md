# 实战：为Ant Design Pro提供后端接口

**Ant Design Pro** 是一个开箱即用的中台前端/设计解决方案，官方地址：[https://pro.ant.design/index-cn](https://pro.ant.design/index-cn)。

## 设置 Ant Design Pro

修改 `ant-design-pro/web/config/config.js` 文件，在末尾右大括号（`}`）上方添加 `proxy` 设置API代理访问路径路径：

```javascript
  proxy: {
    '/api': {
      target: 'http://localhost:22222',
      changeOrigin: true,
    },
  },
```

使用 `start:nomock` 启动Ant Design Pro
```
npm run start:no-mock
```

## 打包、部署

TODO

## 总结

TODO
