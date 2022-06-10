package com.exemple.applockerwithyyoutube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exemple.applockerwithyyoutube.databinding.ItemAppsBinding
import com.exemple.applockerwithyyoutube.entity.App

class AppsAdapter(
    private val onLockClick: (App, Boolean) -> Unit
) : ListAdapter<App, AppsAdapter.AppManagerViewHolder>(ApplicationInfoDiffCallBack()) {

    class AppManagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val viewBinding: ItemAppsBinding by viewBinding(ItemAppsBinding::bind)

        fun bind(app: App, onLockClick: (App, Boolean) -> Unit) {
                viewBinding.tvAppName.text = app.appName
            when(app.isBlocked){
                false -> {
                    viewBinding.btnLock.setImageResource(R.drawable.unlock)
                }
                true -> {
                    viewBinding.btnLock.setImageResource(R.drawable.lock)
                }
            }
                viewBinding.btnLock.setOnClickListener {

                    if (app.isBlocked){
                        app.isBlocked = false
                        onLockClick.invoke(app, false)
                        viewBinding.btnLock.setImageResource(R.drawable.unlock)
                    }else{
                        app.isBlocked = true
                        viewBinding.btnLock.setImageResource(R.drawable.lock)
                        onLockClick.invoke(app, true)
                    }
                }
        }
    }

    private class ApplicationInfoDiffCallBack : DiffUtil.ItemCallback<App>() {
        override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(
            oldItem: App,
            newItem: App
        ): Boolean {
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppManagerViewHolder {
        val cellForApps =
            LayoutInflater.from(parent.context).inflate(R.layout.item_apps, parent, false)
        return AppManagerViewHolder(cellForApps)
    }

    override fun onBindViewHolder(holder: AppManagerViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app, onLockClick)
    }

}
