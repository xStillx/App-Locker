package com.exemple.applockerwithyyoutube

import android.content.Context
import android.content.SharedPreferences
import com.chibatching.kotpref.KotprefModel

class Pref: KotprefModel() {
    var password by stringPref()
    var listSize by intPref()
    private val SHARED_APP_PREFERENCE_NAME = "SharedPref"
    var pref: SharedPreferences = context.getSharedPreferences(SHARED_APP_PREFERENCE_NAME, Context.MODE_PRIVATE);

    private fun putString(key: String?, value: String?) {
        pref.edit().putString(key, value).apply()
    }

    fun getString(key: String?): String? {
        return pref.getString(key, "")
    }

    fun putListString(list: List<String>){
        for ((index, app) in list.withIndex()){
            putString("app_$index", app)
        }
        listSize = list.size
    }


}
