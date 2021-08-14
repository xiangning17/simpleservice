# simpleservice
Android组件通信，优雅的实现跨进程组件间通信。

[![](https://jitpack.io/v/xiangning17/simpleservice.svg)](https://jitpack.io/#xiangning17/simpleservice)

基于组件服务接口下沉为公共依赖的组件通信模型，服务提供组件内部实现并通过SimpleService发布服务，服务使用者通过SimpleService获取服务。

使用apt技术自动生成模板类，让跨进程服务与进程间服务使用体验基本一致。



#### 一、普通组件服务发布使用流程如下：

```kotlin
// 1. 公共依赖模块，声明服务接口
interface IMusic {
  fun play(name: String)
}

// 2. 音乐组件模块，实现服务接口
class MusicService: IMusic {
  override fun play(name: String) {
    Logging.d("MusicService", "play $name")
  }
}

// 3. 音乐组件模块，发布服务
SimpleService.publishService(IMusic::class.java, MusicService())

// 4. 其他组件模块，获取使用服务
val music = SimpleService.getService(IMusic::class.java)
music.play("千里之外")
```



#### 二、跨进程组件服务发布流程，仅仅3个小改动，如下：

```kotlin
// 1. 公共依赖模块，声明服务接口
// 【变化1】为跨进程服务接口添加‘RemoteServive’注解，SimplieService将自动生成AIDL类型
@RemoteService
interface IMusic {
  fun play(name: String)
}

// 2. 音乐组件模块，实现服务接口
class MusicService: IMusic {
  override fun play(name: String) {
    Logging.d("MusicService", "play $name")
  }
}

// 3. 音乐组件模块，发布远程服务
// 【变化2】发布服务改为publishRemoteService方法的调用
SimpleService.publishRemoteService(IMusic::class.java, MusicService())

// 4. 其他组件模块（跨进程），获取并使用远程服务
// 【变化3】获取远程服务接口变为getRemoteServiceWait，此处使用同步等待5s的方式获取，还提供回调以及协程的异步方式获取
val music = SimpleService.getRemoteServiceWait(IMusic::class.java, 5000)
music.play("千里之外")
```



#### 三、跨进程组件服务发布支持Parcelable类型参数

```kotlin
// 声明音乐信息类型
// 使用‘ParcelableAidl’注解修饰Pacelable类型，SimpleService将自动生成AIDL声明
@ParcelableAidl
@Parcelize
class MusicInfo(var name: String, var author: String): Parcelable

// 增强服务接口，让play接口接收到更丰富的信息以便进行更准确的音乐匹配
@RemoteService
interface IMusic {
  fun play(info: MusicInfo)
}
```



#### 四、也支持AIDL的修饰符，oneway、in、out、inout

```kotlin
@RemoteService
interface IMusic {
  // play不需要返回值，可以声明为oneway
  @OneWay
  fun play(info: MusicInfo)
  // 读取原始的音频数据，参数data声明为out
  fun getRawData(@Out data: ByteArray)
}
```



#### 五、回调也是支持的

```kotlin
// 声明播放进度回调接口
@RemoteService
interface OnPlayProgressUpdate {
  fun onProgress(progress: Int)
}

@RemoteService
interface IMusic {
  // play参数中添加播放进度回调
  @OneWay
  fun play(info: MusicInfo, onProgress: OnPlayProgressUpdate)
  fun getRawData(@Out data: ByteArray)
}
```



以上，基本上让跨进程服务和进程间服务使用无太大差别了，需要做的仅仅是加几个注解。



#### 六、开始之前

当然，在使用之前我们需要做一些基本的项目配置，如下：

1. 在项目根目录的build.gradle中添加jitpack的maven仓库以及插件依赖:
```groovy
buildscript {
  	// 统一simple-service库的版本号
    ext.simple_service_version = "1.0.5"
  
    repositories {
        maven { url 'https://jitpack.io' }
    }
  
    dependencies {
      	// 声明simple-service的gradle插件依赖
        classpath("com.github.xiangning17.simpleservice:plugin:$simple_service_version")
    }
}

allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

2. 在远程服务**接口声明**的基础模块build.gradle中：
```groovy
// 启用simple-service插件
apply plugin: 'simple-service'

dependencies {
  
  	// 添加库依赖，并暴露给上层组件
    api "com.github.xiangning17.simpleservice:core:$simple_service_version"
  
    // 声明注解处理器，用于解析RemoteService等注解，并生成相关代理类型
  	// 如果需要处理被注解的kotlin类，需要使用kapt
    annotationProcessor "com.github.xiangning17.simpleservice:annotation-processor:$simple_service_version"
}
```

3. 在app模块的build.gradle中：

```groovy
// app模块启用simple-service插件，用于设置RemoteServiceBridg的进程
apply plugin: 'simple-service'

// 通过以下配置指定RemoteServiceBridge远程服务桥所在的进程
// RemoteServiceBridge是RemoteServiceManager远程服务管理器对外的窗口，是一个Android Service。
// 当其他进程想要连接RemoteServiceManager时，就通过绑定RemoteServiceBridge服务得到服务进程的单例RemoteServiceManager对象，
// 然后其他进程就可以进行远程服务的查询获取以及发布监听了。

// 这个配置项可以省略，则默认就是applicationId，也就是主进程。
// 但是即便省略该项配置，在app模块启用'simple-service'插件的步骤还是不能省略，
// 不然不能成功设置RemoteServiceBridge服务所在的进程
simpleService {
    bridgeServiceProcess = "me.xiangning.simpleservice"
}
```
