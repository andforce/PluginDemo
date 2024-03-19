package com.cry.mediaprojectiondemo.autotouch

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.cry.mediaprojectiondemo.socket.SocketViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AutoTouchService : AccessibilityService() {

    private val socketViewModel: SocketViewModel by inject()

    private var job: Job? = null

    private val metrics = DisplayMetrics()

    private var finalWidthPixels = 1
    private var finalHeightPixels = 1
    private var path:Path? = Path()
    private val points = mutableListOf<Pair<Float, Float>>()
    private var lastTimeStamp = 0L

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        val windowManager =
            applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return

        windowManager.defaultDisplay?.getRealMetrics(metrics)

        finalWidthPixels = (metrics.widthPixels)
        finalHeightPixels = (metrics.heightPixels)


        job = GlobalScope.launch {
            socketViewModel.eventFlow.buffer(capacity = 1024).collect {

                it?.let {
                    //Log.d("AutoTouchService", "collect MouseEvent: $it")
                    val scaleW = finalWidthPixels / it.remoteWidth.toFloat()
                    val scaleH = finalHeightPixels / it.remoteHeight.toFloat()
                    val fromRealX = if (it.x * scaleW < 0) 0f else it.x * scaleW
                    val fromRealY = if (it.y * scaleH < 0) 0f else it.y * scaleH

                    when (it) {
                        is com.andforce.socket.MouseEvent.Down -> {

                            lastTimeStamp = System.currentTimeMillis()
                            path = Path()
                            points.clear()
                            path?.moveTo(fromRealX, fromRealY)
                            points.add(Pair(fromRealX, fromRealY))
                            Log.d("AutoTouchService", "DOWN points: $points, path: $path")
                        }
                        is com.andforce.socket.MouseEvent.Move -> {
                            path?.lineTo(fromRealX, fromRealY)
                            points.add(Pair(fromRealX, fromRealY))
                            Log.d("AutoTouchService", "MOVE points: $points, path: $path")
                        }
                        is com.andforce.socket.MouseEvent.Up -> {
                            if (path?.isEmpty == true) {
                                return@collect
                            }
                            path?.lineTo(fromRealX, fromRealY)
                            points.add(Pair(fromRealX, fromRealY))

                            val currentTime = System.currentTimeMillis()
                            var duration = if (currentTime - lastTimeStamp < 100) 100 else currentTime - lastTimeStamp
                            if (duration > 300) {
                                duration = 300
                            }

                            // 打印points
                            Log.d("AutoTouchService", "UP points: $points ,duration: $duration, path: $path")

                            path?.let {p->
                                dispatchMouseGesture(p,0, duration).also {
                                    lastTimeStamp = 0
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //Log.d("AutoTouchService", "onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        Log.d("AutoTouchService", "onInterrupt")
    }

    private fun dispatchMouseGesture(path: Path, startTime: Long, duration: Long) {
        val gestureDescription = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, startTime, duration))
            .build()
        dispatchGesture(gestureDescription, null, null)
    }

}