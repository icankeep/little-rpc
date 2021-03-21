## little-rpc
自己动手写的一个rpc小框架

1. 注册中心
    - 使用Zookeeper
        - 负载均衡策略
        - zkClient的配置选择
1. 序列化
    - [使用kyro](https://github.com/EsotericSoftware/kryo)
      - 注册类（如果要部署多台机器，需要显示关闭注册功能）
      - 线程安全问题 
    - [使用protostuff](https://github.com/protostuff/protostuff)
1. 数据传输（网络Socket连接、发包收包、压缩、粘包半包）
    - Netty
    - 使用原生Socket
1. 代理问题
1. 配置解析
    - 注解配置
    - xml配置
    - 代码配置