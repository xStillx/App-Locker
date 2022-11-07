package com.exemple.applockerwithyyoutube

import android.app.AppOpsManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.chibatching.kotpref.Kotpref
import com.exemple.applockerwithyyoutube.databinding.ActivityMainBinding
import com.exemple.applockerwithyyoutube.entity.App
import com.exemple.applockerwithyyoutube.service.AppLockerService
import com.exemple.applockerwithyyoutube.utils.canDrawOverlays
import com.exemple.applockerwithyyoutube.utils.hasGetUsageStatsPermissions
import com.exemple.applockerwithyyoutube.utils.requestGetUsageStatsPermission
import com.exemple.applockerwithyyoutube.utils.requestOverlaySettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private var appModel: ArrayList<App> = arrayListOf()
    private var adapterApps: AppsAdapter? = null
    private var lockedApps: List<App>? = arrayListOf()
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Kotpref.init(this)
        password = Pref().password
        dialog()
        initViews()
        settings()
        observe()
    }

    private fun observe() {
        viewModel.apps.observe(this){
            lockedApps = it
        }
    }

    private fun settings() {
        lifecycleScope.launch {
            if (!canDrawOverlays) {
                requestOverlaySettings()
            }

            if (!hasGetUsageStatsPermissions()) {
                requestGetUsageStatsPermission()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(
                    Intent(this@MainActivity, AppLockerService::class.java)
                )
            } else {
                startService(
                    Intent(this@MainActivity, AppLockerService::class.java)
                )
            }
        }
    }

    private fun dialog() {
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Разрешите приложению сбор статистики для корректной работы.")
                setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User clicked OK button
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        startActivity(intent)
                    })
                setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                        dialog.dismiss()
                    })
            }
            // Set other dialog properties

            // Create the AlertDialog
            builder.create()
        }
        if (!isAccessGranted()){
            alertDialog?.show()
        }
    }

    private fun initViews() {
        if (password.isEmpty()){
            viewBinding.btnPassword.text = "Set password"
        }else{
            viewBinding.btnPassword.text = "Update password"
        }
        viewBinding.btnPassword.setOnClickListener {
            if (password.isEmpty()){
                setPassword(this)
            }else{
                updatePassword(this)
            }
        }
        val appList: List<ApplicationInfo> =
            this.packageManager.getInstalledApplications(0)
                .filter {
                    val appName = packageName
                    it.packageName != appName && (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    lockedApps?.any { app -> app.packageName != it.packageName }!!
                }
        for (app in appList){
            appModel.add(
                App(
                    app.loadLabel(packageManager).toString(),
                    false,
                    app.packageName
                )
            )
        }
        lockedApps?.let {
            for (lockedApp in it){
                appModel.add(
                    App(
                        lockedApp.packageName,
                        lockedApp.isBlocked,
                        lockedApp.appName
                    )
                )
            }
        }
        adapterApps = AppsAdapter{ app, isDelete ->
            Log.d("AppsAdapter", "Foreground app: $app , $isDelete")
            if (isDelete){
                viewModel.onAddToBlockingList(
                    app
                )
                Log.d("AppsAdapter", "false")
            }else{
                viewModel.deleteFromBlockingList(app)
                Log.d("AppsAdapter", "true")
            }
        }

        adapterApps!!.submitList(appModel)
        with(viewBinding.rvApps){
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = adapterApps
        }
    }

    private fun isAccessGranted(): Boolean {
        return try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            var mode = 0
            mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid, applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun setPassword(context: Context){
        val dialog = AlertDialog.Builder(context)
        val ll = LinearLayout(context)
        val title = TextView(context)
        val input = EditText(context)
        title.text = "Ведите пароль"
        input.inputType = InputType.TYPE_CLASS_TEXT or  InputType.TYPE_TEXT_VARIATION_PASSWORD
        ll.orientation = LinearLayout.VERTICAL
        ll.addView(title)
        ll.addView(input)
        dialog.setView(ll)
        dialog.apply {
            setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    // User clicked OK button
                    Pref().password = input.text.toString()
                    password = input.text.toString()
                    viewBinding.btnPassword.text = "Update password"
                })
            setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                    dialog.dismiss()
                })
        }
        // Set other dialog properties

        // Create the AlertDialog
        dialog.create()
        dialog.show()
    }


    private fun updatePassword(context: Context){
        val dialog = AlertDialog.Builder(context)
        val ll = LinearLayout(context)
        val title = TextView(context)
        val titleNew = TextView(context)
        val inputPrevious = EditText(context)
        val inputNew = EditText(context)
        title.text = "Ведите предыдущий пароль"
        titleNew.text = "Введите новый пароль"
        inputPrevious.inputType = InputType.TYPE_CLASS_TEXT or  InputType.TYPE_TEXT_VARIATION_PASSWORD
        inputNew.inputType = InputType.TYPE_CLASS_TEXT or  InputType.TYPE_TEXT_VARIATION_PASSWORD
        ll.orientation = LinearLayout.VERTICAL
        ll.addView(title)
        ll.addView(inputPrevious)
        ll.addView(titleNew)
        ll.addView(inputNew)
        dialog.setView(ll)
        dialog.apply {
            setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    // User clicked OK button
                    if (password == inputPrevious.text.toString()){
                        Pref().password = inputNew.text.toString()
                        password = inputNew.text.toString()
                    }else{
                        Toast.makeText(context, "Введен неправильный пароль", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                    dialog.dismiss()
                })
        }
        // Set other dialog properties

        // Create the AlertDialog
        dialog.create()
        dialog.show()
    }
}

