package name.renderer.bevy

import android.content.Context
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class BevySurfaceView : SurfaceView, SurfaceHolder.Callback2 {
    private var bevy_app : Long = Long.MAX_VALUE
    private var ndk_inited = false
    private var sensorManager: SensorManager? = null
    private var mSensor: Sensor? = null
    private var sensorValues: FloatArray = FloatArray(3)

    constructor(context: Context) : super(context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    init {
        // 将当前类设置为 SurfaceHolder 的回调接口代理
        holder.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
//                Log.d("Touch", "ACTION_DOWN at: " + event.x + ", " + event.y)
                if (bevy_app != Long.MAX_VALUE) {
                    RustBridge.device_enter_touch(bevy_app, event.x , event.y )
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
//                Log.d("Touch", "ACTION_MOVE at: " + event.x + ", " + event.y)
                if (bevy_app != Long.MAX_VALUE) {
                    RustBridge.device_touch_move(bevy_app, event.x , event.y )
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
//                Log.d("Touch", "ACTION_UP at: " + event.x + ", " + event.y)
                if (bevy_app != Long.MAX_VALUE) {
                    RustBridge.device_exit_touch(bevy_app)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // 绘制表面被创建后，创建/重新创建 Bevy App
    override fun surfaceCreated(holder: SurfaceHolder) {
        holder.let { h ->
            if (!ndk_inited) {
                ndk_inited = true
                RustBridge.init_ndk_context(this.context)
            }

            if (bevy_app == Long.MAX_VALUE) {
                // Get the screen's density scale
                val scaleFactor: Float = resources.displayMetrics.density
                // Configure Canvas
                globalAppState?.let { appState ->
                    val bg = appState.backgroundColor
                    val t = appState.environmentLightColor
                    bevy_app = RustBridge.create_bevy_app(this.context.assets, h.surface, scaleFactor,
                        bg.red,
                        bg.green,
                        bg.blue,
                        t.red,
                        t.green,
                        t.blue,
                        appState.environmentLightStrength
                        )
                }

            }

            // SurfaceView 默认不会自动开始绘制，setWillNotDraw(false) 用于通知 App 已经准备好开始绘制了。
            setWillNotDraw(false)

            var sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) {
                        sensorValues = event.values
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                }
            }
            mSensor?.also { sensor ->
                sensorManager?.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    // 绘制表面被销毁后，也销毁 Bevy 中的 Android window
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (bevy_app != Long.MAX_VALUE) {
            RustBridge.release_bevy_app(bevy_app)
            bevy_app = Long.MAX_VALUE
        }
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
    }

    // API Level 26+
//    override fun surfaceRedrawNeededAsync(holder: SurfaceHolder, drawingFinished: Runnable) {
//        super.surfaceRedrawNeededAsync(holder, drawingFinished)
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 考虑到边界情况，这个条件判断不能省略
        if (bevy_app == Long.MAX_VALUE) {
           return
        }
        RustBridge.device_motion(bevy_app, sensorValues[0], sensorValues[1], sensorValues[2])
        RustBridge.enter_frame(bevy_app)
        // invalidate() 函数通知通知 App，在下一个 UI 刷新周期重新调用 draw() 函数
        invalidate()
    }

}
