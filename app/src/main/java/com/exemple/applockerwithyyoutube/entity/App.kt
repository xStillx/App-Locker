package com.exemple.applockerwithyyoutube.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_table")
data class App(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "appName")
    var appName: String,
    @ColumnInfo(name = "isBlocked")
    var isBlocked: Boolean,
    @ColumnInfo(name = "packageName")
    val packageName: String
)
