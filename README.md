# RxFingerPrinter

[![Build Status](https://api.travis-ci.org/tbruyelle/RxPermissions.svg?branch=master)](https://travis-ci.org/tbruyelle/RxPermissions)

用rxjava封装了指纹识别，并实现类似Glide生命周期绑定，顺便撸了一个指纹控件。

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

[ ![Download](https://api.bintray.com/packages/zhangweihui0503/maven/RxFingerPrinter/images/download.svg?version=1.1.0) ](https://bintray.com/zhangweihui0503/maven/RxFingerPrinter/1.1.0/link)
```gradle
dependencies {
    compile  'com.zwh:RxFingerPrinter:1.1.0'
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
DisposableObserver<Boolean> observer = new DisposableObserver<Boolean>() {

                    @Override
                    protected void onStart() {
                        
                    }

                    @Override
                    public void onError(Throwable e) {
                        //处理错误信息
                        if(e instanceof FPerException){
                            Toast.makeText(MainActivity.this,((FPerException) e).getDisplayMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(aBoolean){
                            //指纹验证成功
                        }else{
                            //指纹验证失败
                        }
                    }
                };
                rxfingerPrinter.begin().subscribe(observer);//RxfingerPrinter会在onPause()时暂停指纹监听，onResume()时恢复指纹监听)
                rxfingerPrinter.addDispose(observer);//由RxfingerPrinter管理(会在onDestroy()生命周期时自动解除绑定)
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
 
 
 ## License
``` 
 Copyright 2018, Zweihui 
  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at 
 
       http://www.apache.org/licenses/LICENSE-2.0 

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
