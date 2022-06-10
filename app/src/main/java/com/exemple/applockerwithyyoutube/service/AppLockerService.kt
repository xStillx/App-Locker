package com.exemple.applockerwithyyoutube.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.exemple.applockerwithyyoutube.R
import com.exemple.applockerwithyyoutube.entity.AppDataBase
import com.exemple.applockerwithyyoutube.entity.AppRepository
import com.exemple.applockerwithyyoutube.utils.canDrawOverlays
import com.exemple.applockerwithyyoutube.utils.hasGetUsageStatsPermissions
import com.exemple.applockerwithyyoutube.utils.observeForegroundApps
import com.exemple.applockerwithyyoutube.utils.windowManager
import kotlinx.coroutines.*

private const val TAG = "AppLockerService"

private const val SERVICE_NOTIFICATION_ID = 1
private const val NOTIFICATION_CHANNEL_ID = "app_locker_service"

@SuppressLint("InflateParams")
class AppLockerService : Service() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val overlayView by lazy {
        LayoutInflater.from(this).inflate(R.layout.lock_overlay_view, null)
    }

    private val repository: AppRepository by lazy {
        AppRepository(AppDataBase.getDataBase(this).appDao())
    }

    override fun onBind(intent: Intent): Nothing? = null

    override fun onCreate() {
        super.onCreate()

        addNotification()
        if (hasGetUsageStatsPermissions()) {
            coroutineScope.launch {
                observeForegroundApps().collect {
                    val lockedApps = repository.getLockedApps()

                    Log.d(TAG, "Foreground app 1: $it $lockedApps")

                    hideOverlay()
                    if (lockedApps.any { app -> app.packageName == it }) {
                        showOverlay()
                    }

                    /*readAllData.value?.let { apps ->
                        Log.d(TAG, "Foreground app 2: $apps")
                        for (app in apps){
                            Log.d(TAG, "Foreground app 3: $it $app")
                            if (app.packageName == it){
                                showOverlay()
                            }else{
                                hideOverlay()
                            }
                        }
                    }*/
                }
            }
        } else {
            Log.d(TAG, "No usage stats service perms!")
            stopSelf()
        }
    }

    private fun hideOverlay() {
        if (overlayView.isAttachedToWindow)
            windowManager.removeViewImmediate(overlayView)
    }

    private fun showOverlay() {
        if (canDrawOverlays) {
            windowManager.addView(
                overlayView,
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    },
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            )
        }
    }

    private fun addNotification() {
        NotificationManagerCompat.from(this)
            .createNotificationChannel(
                NotificationChannelCompat.Builder(
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_MAX
                )
                    .setName(getString(R.string.app_locker_channel_name))
                    .build()
            )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_locker_service_notification_title))
            .build()

        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()

        coroutineScope.cancel()
    }
}
