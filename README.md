# BNes Paper Nes模拟器
## 基于[BNes](https://github.com/moewhite19/BNes)编写的Paper插件，修复了原作者遗留的一些Bug
已测试Paper1.21.4
## 前置依赖
加*为必须依赖
* *CommandAPI 命令注册
* Vault 经济支持
* voicechat 游戏音轨输出

## 指令

* `/bnes reload`: 重载配置
* `/bnes close <实例名>`: 关闭指定游戏机
* `/bnes showfps`: 开关fps显示
* `/bnes map <实例名> [玩家]`: 获取指定游戏机地图
* `/bnes card <ROM名称>`: 获取游戏卡带
* `/bnes create <实例名称>`: 创建一个游戏机实例
* `/bnes debug`: 开启debug
* `/bnes menu [page]`: 打开游戏ROM列表，page为页码
* `/bnes rename <旧实例名称> <新实例名称>`: 实例名称改名
* `/bnes showfps`: 显示FPS

## 键位定义

* 游戏移动方向 > 手柄方向
* 跳跃 > 手柄A
* 交换副手 > 手柄B
* 左键地面 > select键
* 右键地图 > start键
* 重启游戏 > 按住跳跃右键地图

## 问题修复与改进

* 修复了在玩家面朝不同方位时，按键映射混乱的问题
* 命令注册更改为[CommandAPI](https://docs.commandapi.dev/intro)进行注册

## 引用
* [HalfNes](https://github.com/andrew-hoffman/halfnes)
