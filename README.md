# SimpleService
Android组件通信，优雅的实现跨进程组件间通信。

[![](https://jitpack.io/v/xiangning17/simpleservice.svg)](https://jitpack.io/#xiangning17/simpleservice)

基于组件服务接口下沉为公共依赖的组件通信模型，服务提供组件内部实现并通过SimpleService发布服务，服务使用者通过SimpleService获取服务。

使用apt技术自动生成AIDL文件以及模板类，让跨进程服务与进程间服务使用体验基本一致。下面简单介绍SimpleService的使用。


### 一、普通组件服务发布使用流程如下：

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



### 二、跨进程组件服务发布流程，重头戏来了，如下：

**每个进程在使用跨进程服务相关接口前，都需要先初始化远程服务，提供context，一般可在Appiliction的onCreate方法初始化：**
```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 【变化1】使用跨进程服务之前需要先初始化
        SimpleService.initRemoteService(this)
    }
}
```
具体发布流程：

```kotlin
// 1. 公共依赖模块，声明服务接口
// 【变化2】为跨进程服务接口添加‘RemoteServive’注解，SimplieService将自动生成AIDL类型
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
// 【变化3】发布服务改为publishRemoteService方法的调用
SimpleService.publishRemoteService(IMusic::class.java, MusicService())

// 4. 其他组件模块（跨进程），获取并使用远程服务
// 【变化4】获取远程服务接口变为getRemoteServiceWait，此处使用同步等待5s的方式获取。
// 还提供回调以及协程的异步方式获取，分别是bindRemoteService和getRemoteService
val music = SimpleService.getRemoteServiceWait(IMusic::class.java, 5000)
music.play("千里之外")
```
**以上，今4个简单的变动，便完成了跨进程服务的发布使用，增量变动只有一个，就是初始化远程服务，其余部分都和写本地服务一样！**


### 三、跨进程组件服务发布支持Parcelable类型参数

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


### 四、也支持AIDL的修饰符，oneway、in、out、inout

```kotlin
@RemoteService
interface IMusic {
  // play不需要返回值，可以声明为oneway
  @OneWay
  fun play(info: MusicInfo)
  // 读取原始的音频数据，参数data声明为out
  fun getRawData(info: MusicInfo, @Out data: ByteArray)
}
```


### 五、回调也是支持的

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
  fun getRawData(info: MusicInfo, @Out data: ByteArray)
}
```

以上，基本上让跨进程服务和进程间服务使用无太大差别了，需要做的仅仅是加几个注解。



### 六、开始之前

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

### 七、高级用法
除以上基本能力外，SimpleService还提供以下特性。

开始之前，先定义几个概念：
* 远程服务：指被`@RemoteService`修饰的接口
* 远程服务实现：指‘远程服务’的实现类，如`MusicService`就是对`IMusic`的远程服务实现
* 远程服务Binder：指对‘远程服务’自动生成的Binder类型，用于跨进程的Binder服务传递
* 远程服务代理：指远程服务使用者实际拿到的对象，是对远程服务Binder的一个代理实现

### 1. 远程服务重新发布能力
通过SimpleService获取远程服务时，我们实际拿到的是一个代理类型，即为**远程服务代理**，这个类型是根据@RemoteService注解处理的接口自动生成的。其实现了被注解的接口，且代理了**远程服务Binder**。因为返回给服务使用者的对象是代理对象，所以我们能做到更新具体执行的**远程服务Binder**指向。这个特性可用于远程服务的重新发布。如下：
```kotlin
// 组件A，发布IMusic为MusicService
SimpleService.publishRemoteService(IMusic::class.java, MusicService())

// 组件B，获取并使用IMusic
val music = SimpleService.getRemoteServiceWait(IMusic::class.java, 5000)
music.play("千里之外")

// 组件A，重新发布IMusic为MusicServiceNew
SimpleService.publishRemoteService(IMusic::class.java, MusicServiceNew())

// 组件B，此时不用再次获取IMusic，直接就可以使用到MusicServiceNew的服务，由SimpleService内部自动更新了远程服务的指向
music.play("美丽新世界")

```

这个特性除了在组件A想主动更新服务实现时，对于组件A所在进程异常退出后又重启发布服务的情况，也能进行**断线重连**。

甚至，如果你想对本地服务也实现这种服务更新，也可以将其通过远程服务的方式进行发布^_^。


### 2. 自定义远程服务调用时的异常处理
由于我们返回给使用者的`远程服务代理`代理了对`远程服务Binder`的访问，那其内部必然是要处理RemoteException等调用异常的，目前的方式是通过`IMethodErrorHandler`接口把具体调用时的异常处理让上层进行实现，类似动态代理。现在默认情况下，对所有`远程服务代理`的处理都是使用默认提供的`DefaultValueMethodErrorHandler`,该处理器捕获所有异常，并返回默认的返回值。你如果想自定义该异常处，比如需求上不能吃掉某些异常，需要再次把抛出去，或者针对某些特殊类型异常做特定处理等等，可以通过以下方式设置错误处理器：
```kotlin
// 组件B，使用者注册IMusic远程服务的调用错误处理器（同一个进程内全局生效）
SimpleService.registerMethodErrorHandler(IMusic::class.java, errorHandler)
```


### 3. 获取一个`远程服务代理`的`远程服务Binder`
在服务使用端，有时希望获取到`远程服务代理`对象代理的`远程服务代理`，比如某个接口的一些回调参数，我们想使用`RemoteCallbackList`进行管理时（参考项目源码的`RemoteServiceManagerImpl`），需要`IInterface`类型的对象，可以通过以下方式：
```kotlin
// 组件B，获取服务
val music = SimpleService.getRemoteServiceWait(IMusic::class.java, 5000)

// 组件A，后续某个时机想拿到‘远程服务代理’对象的`远程服务Binder`
val musicBinder: IMusicBinder = SimpleService.getServiceRemoteInterface(IMusic::class.java, music)

```


#### 八、其他

* 依赖kotlin 1.5.21和kotlin coroutines 1.5.0
* 向前兼容到Andrioid Gradle Plugin版本3.3.0
