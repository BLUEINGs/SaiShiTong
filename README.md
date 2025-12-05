# 开发说明

**项目说明：本项目为赛事通（智能运动会系统），该项目具有子项目 _[帧界篮球（跨时空联合判定的篮球智能裁判系统）](#贞界篮球项目模块说明)_。**

## 帧界篮球项目模块说明
### 源代码位置
1. [YOLO推理代码java版](sms-server/src/main/java/com/blueing/sports_meet_system/service/imp/DetectorServiceA.java)
2. [CO_Tracker跟踪器](sms-server/src/main/java/com/blueing/sports_meet_system/service/imp/BallTrackerService.java)
3. [YOLOCO_STBIR事件生成](sms-server/src/main/java/com/blueing/sports_meet_system/service/imp/FrameLogger.java)
### 模型文件
1. [RepVit+P2345YOLO](public/models/best.onnx)
### 实验报告
1. [改进的YOLO](public/experiments/improved_YOLO)
2. [COTracker&YOLOCO_STBIR](public/experiments/COTracker&YOLOCO_STBIR)

## 项目部署
### 依赖环境
1. jdk17+
2. MySQL
3. 各种[Maven依赖](pom.xml)
### 部署流程
首先，使用shell克隆[项目仓库](https://github.com/BLUEINGs/SaiShiTong.git)，并使用IDEA打开项目；或直接直接使用IDEA打开克隆并打开项目。
```shell
git clone https://github.com/BLUEINGs/SaiShiTong.git
```
然后，在MySQL中创建sports_meet数据库，
```sql
create database sprots_meet;
```
执行完毕后在该表执行[数据表DDL](ddl.md)中的ddl语句。<br>
最后，修改[后端配置文件](sms-server/src/main/resources/application.yml)，包括：
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://hostname:port/sports_meet
    username: #配置为你的
    password: #配置为你的

oss:
  head-portrait :
    path: #管理系统中的用户头像的保存位置
    url: #处理后可被访问的文件位置

  asset:
    model:
      basketballYoloModel: #帧界篮球的YOLO模型位置，此路径必须为本机绝对路径
```
其中，本项目提供已经训练的[ImprovedYOLO模型](public/models/best.onnx)。
### 启动
直接在IDEA中启动本项目或maven:package后运行jar包启动，启动后访问http://localhost:8080/index.html即可。

## 服务器环境
1. [服务器域名](http://blueing.moenya.net)
2. [yapi接口服务](http://blueing.moenya.net:3000)
3. [MySQL数据库](http://blueing.moenya.net:3306/sprots_meet)
4. [赛事通0.1.0公网部署版前端地址](http://blueing.moenya.net:90)
5. [赛事通0.1.0公网部署版后端地址](http://blueing.moenya.net:8080)
<br>***由于服务器配置原因，公网版帧界篮球路由项不可用***
