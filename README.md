## 下载AOSP源码（Android12举例）

```shell
# 创建目录$HOME/aosp
mkdir -p $HOME/aosp
cd $HOME/aosp
repo init -u https://android.googlesource.com/platform/manifest -b android-12.0.0_r2
repo sync
```


## 编译AOSP
```shell
cd $HOME/aosp
source build/envsetup.sh
# target可以根据自己的需求选择，这里选择aosp_arm-eng
launch aosp_arm-eng
make framework-minus-apex
make framework-res
make services
```

## 运行full_sdk.py脚本

```shell
# 得到$HOME/aosp/sdk/full_sdk/full_sdk.jar
python3 full_sdk.py
```

## 生成密钥库
```shell
cd $HOME/aosp/build/make/target/product/security
# 步骤 1：合并为 PKCS12 密钥库
openssl pkcs8 -inform DER -nocrypt -in platform.pk8 -out platform.key.pem
openssl pkcs12 -export -in platform.x509.pem -inkey platform.key.pem -out platform.p12 -name android -password pass:android
# 步骤 2：将 PKCS12 密钥库转换为 JKS 密钥库
keytool -importkeystore -deststorepass android -destkeypass android -destkeystore platform.jks -srckeystore platform.p12 -srcstoretype PKCS12 -srcstorepass android -alias android
```
得到$HOME/aosp/build/make/target/product/security/platform.jks，拷贝到gradle项目根目录

## 配置签名

```groovy
android {
    signingConfigs {
        config {
            storeFile project.file("../platform.jks")
            storePassword "android"
            keyAlias "android"
            keyPassword "android"
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            signingConfig signingConfigs.config
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {
            minifyEnabled false
            signingConfig signingConfigs.config
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 设置sharedUserId
```shell
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:sharedUserId="android.uid.system">
</manifest>
```