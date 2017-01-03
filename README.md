# RxFingerPrinter

[![Build Status](https://api.travis-ci.org/tbruyelle/RxPermissions.svg?branch=master)](https://travis-ci.org/tbruyelle/RxPermissions)

用rxjava简单封装了指纹识别，顺便撸了一个指纹控件

![image](https://github.com/Zweihui/RxFingerPrinter/blob/master/gif/ScreenShot.gif)

## Setup

测试机型推荐android版本6.0以上且具有指纹模块。

在项目build.gradle中配置 :

```gradle
repositories {
    jcenter() // If not already there
}
```
在Module中build.gradle中配置 :
```gradle
dependencies {
    compile  'com.zwh:RxFingerPrinter:1.0.1'
}
```

## Usage

创建一个 `RxFingerPrinter`实例  :

```java
RxFingerPrinter rxFingerPrinter = new RxFingerPrinter(this); // where this is an Activity instance
```
在需要开启指纹识别的地方执行begin方法并订阅:

```java
// 可以在oncreat方法中执行
Subscription subscription =
     rxFingerPrinter
    .begin()
    .subscribe(new Subscriber<Boolean>() {
      @Override
      public void onCompleted() {

      }

      @Override
      public void onError(Throwable e) {
          if(e instanceof FPerException){
            Toast.makeText(MainActivity.this,((FPerException) e).getDisplayMessage(),Toast.LENGTH_SHORT).show();
         }
      }

      @Override
      public void onNext(Boolean aBoolean) {
        if (aBoolean){
          Toast.makeText(MainActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
        }else {
          Toast.makeText(MainActivity.this, "指纹识别失败", Toast.LENGTH_SHORT).show();
        }
      }
    });
    rxfingerPrinter.addSubscription(this,subscription); //不要忘记把订阅返回的subscription添加到rxfingerPrinter里
```

 在onDestroy方法中执行取消订阅
 
```java
//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        rxfingerPrinter.unSubscribe(this);
    }
```
用FPerException封装了一下指纹识别时可能出现的异常，可以在订阅的Subscriber的`onError(Throwable e)`中获取该异常
```java
@Override
      public void onError(Throwable e) {
          if(e instanceof FPerException){//判断该异常是否为FPerException
            Toast.makeText(MainActivity.this,((FPerException) e).getDisplayMessage(),Toast.LENGTH_SHORT).show();
         }
```
可以根据```((FPerException) e).getCode() ```来获取对应的错误码，也可以直接调用```((FPerException) e).getDisplayMessage()```提示默认的错误信息。
```java
public String getDisplayMessage() {
        switch (code) {
            case SYSTEM_API_ERROR:
                return "系统API小于23";
            case PERMISSION_DENIED_ERROE:
                return "没有指纹识别权限";
            case HARDWARE_MISSIING_ERROR:
                return "没有指纹识别模块";
            case KEYGUARDSECURE_MISSIING_ERROR:
                return "没有开启锁屏密码";
            case NO_FINGERPRINTERS_ENROOLED_ERROR:
                return "没有指纹录入";
            case FINGERPRINTERS_FAILED_ERROR:
                return "指纹认证失败";
            default:
                return "";
        }
    }
 ```
