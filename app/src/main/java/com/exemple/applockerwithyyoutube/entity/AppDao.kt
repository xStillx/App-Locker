package com.exemple.applockerwithyyoutube.entity

import androidx.room.*

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addApp(appRep: App)

    @Query("SELECT * FROM app_table ORDER BY appName DESC")
    fun readData(): List<App>

    @Delete
    suspend fun deleteByPackageName(appRep: App)
}
