# 开发说明

**项目说明：本项目为赛事通（智能运动会系统）的后端项目，该项目具有子项目 _[帧界篮球（跨时空联合判定的篮球智能裁判系统）](#帧界篮球项目模块说明)_。**

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

1. **JDK 17+**
2. **MySQL**
3. **Maven 依赖（见 `pom.xml`）**
4. **NVIDIA 推理依赖（可选）**
    - [CUDA v11.8](https://developer.nvidia.com/cuda-11-8-0-download-archive)
    - [cuDNN v12.1](https://developer.nvidia.com/cudnn)
5. **Docker & SRS 流媒体服务器**
    - https://github.com/ossrs/srs

---

### 部署流程

#### 1. 克隆项目

```bash
git clone https://github.com/BLUEINGs/SaiShiTong.git
```
使用 IDEA 或其他编辑器打开项目。
#### 2. 初始化数据库
在 MySQL 中创建数据库（注意名称为 sports_meet）：
```sql
create database sports_meet;
```
创建完成后，在该数据库执行 ddl.md 中提供的所有 DDL 语句。
#### 3. 配置流媒体服务器
启动 SRS 或其他兼容的流媒体服务。
参考[官方部署文档](https://github.com/ossrs/srs)。
#### 4. 修改后端配置文件
编辑 sms-server/src/main/resources/application.yml：
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://hostname:port/sports_meet
    username: # 填写你的数据库账号
    password: # 填写你的数据库密码

oss:
  head-portrait:
    path: # 用户头像的本机存储路径
    url:  # 头像对外可访问的访问路径

asset:
  model:
    basketballYoloModel: # YOLO 模型文件的本机绝对路径
```
本项目已提供模型文件：
public/models/best.onnx

### 启动项目

可以选择：
#### A. IDEA 启动
直接运行 Spring Boot 主类即可。
#### B. Maven 打包后运行
mvn package
java -jar sms-server/target/*.jar

在前端系统中，将后端地址配置为当前服务的访问地址即可。

## 配套仓库：赛事通前端
本项目仅为后端项目，需搭配配套[前端项目](https://github.com/BLUEINGs/SaiShiTongFE)仓库使用。

## 依赖支持仓库：流媒体服务
本项目使用SRS作为流媒体服务器，仓库：[SRS](https://github.com/ossrs/srs)。

## 服务器环境
1. [服务器域名](http://blueing.moenya.net)
2. [yapi接口服务](http://blueing.moenya.net:3000)
3. [MySQL数据库](http://blueing.moenya.net:3306/sprots_meet)
4. [赛事通0.1.0公网部署版前端地址](http://blueing.moenya.net:90)
5. [赛事通0.1.0公网部署版后端地址](http://blueing.moenya.net:8080)
<br>***由于服务器硬件资源限制，公网版的帧界篮球功能暂不可用。***
