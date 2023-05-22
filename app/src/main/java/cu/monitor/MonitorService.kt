package cu.monitor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import com.topjohnwu.superuser.Shell

class MonitorService : Service() {
    companion object {
        var isServiceCreated = false

        fun IsServiceRunning(): Boolean {
            return isServiceCreated
        }
    }

    private var floatView: ImageView? = null
    private var daemonRunning = false

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId)
    }

    override fun onCreate() {
        super.onCreate()
        isServiceCreated = true
        CreateWindow()
    }

    override fun onDestroy() {
        val appDataPath = applicationContext.filesDir.absolutePath
        Shell.cmd("sh ${appDataPath}/monitor/stop_monitor.sh").exec()
        DestroyWindow()
        isServiceCreated = false
        super.onDestroy()
    }

    private fun CreateWindow() {
        val density = resources.displayMetrics.density
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.width = dp2px(density, 40)
        layoutParams.height = dp2px(density, 40)
        layoutParams.gravity = Gravity.TOP or Gravity.RIGHT
        layoutParams.alpha = 0.6f

        floatView = ImageView(this)
        floatView!!.setBackgroundColor(Color.parseColor("#88000000"))
        floatView!!.setPadding(
            dp2px(density, 5),
            dp2px(density, 5),
            dp2px(density, 5),
            dp2px(density, 5)
        )
        floatView!!.setImageResource(R.mipmap.start_monitor)
        floatView!!.setOnClickListener {
            val appDataPath = applicationContext.filesDir.absolutePath
            if (!daemonRunning) {
                Shell.cmd("sh ${appDataPath}/monitor/start_monitor.sh").exec()
                (it as ImageView).setImageResource(R.mipmap.stop_monitor)
                daemonRunning = true
            } else {
                Shell.cmd("sh ${appDataPath}/monitor/stop_monitor.sh").exec()
                (it as ImageView).setImageResource(R.mipmap.start_monitor)
                daemonRunning = false
            }
        }

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatView, layoutParams)
    }

    private fun DestroyWindow() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (floatView != null) {
            windowManager.removeViewImmediate(floatView)
        }
    }

    private fun dp2px(scale: Float, dpValue: Int): Int {
        return (dpValue * scale + 0.5f).toInt()
    }
}