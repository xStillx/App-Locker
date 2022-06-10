package com.exemple.applockerwithyyoutube.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

private const val HOUR_IN_MILLISECONDS = 1000L * 3600L
private const val FOREGROUND_APP_CHECK_DELAY = 100L

suspend fun Context.observeForegroundApps() = flow {
    val statsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    while (currentCoroutineContext().isActive) {
        val events: UsageEvents? = statsManager.queryLastHourEvents()

        var lastUsedAppPackage: String? = null

        val event = UsageEvents.Event()
        while (currentCoroutineContext().isActive
            && events != null
            && events.hasNextEvent()
        ) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                && event.packageName != packageName
                && event.className != null
            ) {
                lastUsedAppPackage = event.packageName
            }
        }

        if (lastUsedAppPackage != null) {
            emit(lastUsedAppPackage)
        }

        delay(FOREGROUND_APP_CHECK_DELAY)
    }
}.distinctUntilChanged()

private fun UsageStatsManager.queryLastHourEvents(): UsageEvents? {
    val currentTime = System.currentTimeMillis()
    return queryEvents(currentTime - HOUR_IN_MILLISECONDS, currentTime)
}
