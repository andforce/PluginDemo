package com.cry.mediaprojectiondemo

import android.app.Application
import com.cry.mediaprojectiondemo.socket.SocketViewModel
import com.cry.screenop.coroutine.RecordViewModel
import kotlinx.coroutines.MainScope
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 定义 Koin 模块
        val myModule = module {
            // 将 MyViewModel 定义为全局单例
            single { RecordViewModel(MainScope()) }
            single { SocketViewModel() }
        }

        startKoin {
            androidContext(this@App)
            modules(myModule)
        }
    }
}