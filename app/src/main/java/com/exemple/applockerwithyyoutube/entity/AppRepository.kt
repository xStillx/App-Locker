package com.exemple.applockerwithyyoutube.entity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val appDao: AppDao) {

    suspend fun getLockedApps(): List<App> = withContext(Dispatchers.IO) {
        appDao.readData()
    }

    suspend fun addApp(app: App) = withContext(Dispatchers.IO) {
        appDao.addApp(app)
    }

    suspend fun deleteApp(app: App) {
        appDao.deleteByPackageName(app)
    }
}
