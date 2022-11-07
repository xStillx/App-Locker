package com.exemple.applockerwithyyoutube

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.applockerwithyyoutube.entity.App
import com.exemple.applockerwithyyoutube.entity.AppDataBase
import com.exemple.applockerwithyyoutube.entity.AppRepository
import com.exemple.applockerwithyyoutube.utils.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context
) : ViewModel() {

    private val _apps = MutableLiveData<List<App>>()
    val apps get() = _apps.asLiveData()

    private val repository: AppRepository = AppRepository(
        AppDataBase.getDataBase(applicationContext).appDao()
    )

    init {
        getLockedApps()
    }

    private fun getLockedApps(){
        viewModelScope.launch {
            _apps.value = repository.getLockedApps()
        }
    }

    fun onAddToBlockingList(app: App) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addApp(app)
            Log.d("MainViewModel", "onAddToBlockingList: add")
        }
    }

    fun deleteFromBlockingList(app: App) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteApp(app)
            Log.d("MainViewModel", "onAddToBlockingList: delete")
        }
    }

    /*fun onAddToBlockingList(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            _app.value?.let {
                repository.addApp(App(
                    it.loadLabel(context.packageManager).toString(),
                    it.loadIcon(context.packageManager),
                    isBlocked = true,
                    it.packageName
                ))
            }
        }
    }*/
}
