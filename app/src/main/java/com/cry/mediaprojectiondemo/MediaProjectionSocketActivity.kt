package com.cry.mediaprojectiondemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.andforce.socket.MouseEvent
import com.cry.mediaprojectiondemo.socket.SocketViewModel
import com.cry.screenop.coroutine.RecordViewModel
import com.example.plugindemo.databinding.MediaprojectionActivityMainBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MediaProjectionSocketActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaProjectionViewModel(this@MediaProjectionSocketActivity) as T
            }
        })[MediaProjectionViewModel::class.java]
    }

    private val viewMainBinding by lazy {
        MediaprojectionActivityMainBinding.inflate(layoutInflater)
    }

    private val recordViewModel: RecordViewModel by inject()
    private val socketViewModel: SocketViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewMainBinding.root)
//        viewMainBinding.root.setOnTouchListener { v, event ->
//            Log.d("MediaProjectionSocketActivity", "onCreate: $event")
//            true
//        }

        Log.d("RecordViewModel", "RecordViewModel1: $recordViewModel")

        recordViewModel.recordState.observe(this) {
            when (it) {
                is RecordViewModel.RecordState.Recording -> {
                    viewMainBinding.tvInfo.text = "Recording"
                }
                is RecordViewModel.RecordState.Stopped -> {
                    viewMainBinding.tvInfo.text = "Stopped"
                }
            }
        }

        viewModel.result.observe(this) {
            when (it) {
                is MediaProjectionViewModel.Result.Success -> {
                    CastService.startService(this, it.data, it.resultCode)
                }
                MediaProjectionViewModel.Result.PermissionDenied -> {
                    Toast.makeText(this, "User did not grant permission", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewMainBinding.btnStart.setOnClickListener {
            if (recordViewModel.recordState.value is RecordViewModel.RecordState.Recording) {
                Toast.makeText(this, "Recording, no need start", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    viewModel.createScreenCaptureIntent()
                }
            }
        }

        viewMainBinding.btnTest.setOnClickListener {
            Toast.makeText(this, "收到点击事件了", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            socketViewModel.eventFlow.collect {
                it?.let {
                    when (it) {
//                    is MouseEvent.None -> {
//                        viewMainBinding.tvInfo.text = "None"
//                    }
                        is MouseEvent.Down -> {
                            viewMainBinding.tvInfo.text = "MouseDown"
                        }
                        is MouseEvent.Move -> {
                            viewMainBinding.tvInfo.text = "MouseMove"
                        }
                        is MouseEvent.Up -> {
                            viewMainBinding.tvInfo.text = "MouseUp"
                        }

//                    is MouseEvent.Click -> {
//                        viewMainBinding.tvInfo.text = "MouseClick"
//                    }
                    }
                }
            }
        }
    }
}
