package com.andforce.network

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.use
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class NetworkViewModel : ViewModel() {
    private val client = OkHttpClient.Builder()
        // 添加拦截器，打印所有的请求和响应
        .addInterceptor {
            val request = it.request()
            val response = it.proceed(request)
            Log.d("NetworkViewModel", "request: ${request.url}")
            Log.d("NetworkViewModel", "response: $response")
            response
        }
        .build()
    private val retrofit = Retrofit.Builder()
        //.addConverterFactory(ScalareConverterFactory.create())
        .baseUrl("http://192.168.2.183:3001") // replace with your base url
        // 使用OKHttp下载
        .client(client)
        .build()

    private val downloadService = retrofit.create(ApiService::class.java)

    private val _stateFlow = MutableStateFlow<File?>(null)
    val stateFlow: Flow<File> = _stateFlow.filter { it != null }.mapNotNull { it }

    fun downloadApk(context: Context, name: String, url: String) {
        viewModelScope.launch {
            val response = downloadService.downloadFile(url)
            dowload(context, response) {
                success {
                    _stateFlow.value = it
                }
                error {
                    Log.e("NetworkViewModel", "download error: $it")
                }
            }.startDowload()

//            saveFileToSdCard(context, name, call.body()!!).let {
//                _stateFlow.value = it
//            }
        }

    }


    private fun saveFileToSdCard(context: Context, name: String, body: ResponseBody): File? {
        val apkFile = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let {
            File(it, name)
        } ?: return null

        var input: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            input = body.byteStream()
            fos = FileOutputStream(apkFile)
            fos.use { output ->
                input.use { input ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            return apkFile
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fos?.close()
        }
        return apkFile
    }
}