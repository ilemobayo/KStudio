package com.musicplayer.aow.ui.settings

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.System.canWrite
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import com.musicplayer.aow.R
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.utils.FileServer
import com.musicplayer.aow.utils.sharefile.wifihotspot.WifiHotSpotManager
import kotlinx.android.synthetic.main.activity_server.*
import android.net.wifi.WifiConfiguration
import android.util.Log


class ServerActivity : AppCompatActivity() {

    var server: FileServer = FileServer.instance!!

    var wifiHotSpotManager: WifiHotSpotManager? = null;
    var pos = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back) // your drawable
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }


        //wifi connection
        wifiHotSpotManager = WifiHotSpotManager(applicationContext)


        server.initServer()
        //Settings change permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Hot configuration class
            val apConfig = WifiConfiguration()
            // Configuration hot name ( You can add a random number behind the name )
            apConfig.SSID = "Musixplay_WFP1000"
            // Configuration hot password
            apConfig.preSharedKey = "mxp1000"
            settingsPermission(apConfig)
        }
        server_ip.text = "http://" + server!!.serverIp() + ":7000"

        close_server.setOnClickListener {
            var wifiUnSet = wifiHotSpotManager?.setWifiApEnabled(null, false)
            if (wifiUnSet == true){
                server.closeAllConnections()
                server.stopServer()
                finish()
            }
        }
    }
    

    @RequiresApi(Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    fun settingsPermission(config: WifiConfiguration? = null){
        if (!canWrite(applicationContext)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + Injection.provideContext()!!.getPackageName()))
            startActivityForResult(intent, 200)
        }else{
            var wifiSet = wifiHotSpotManager?.setWifiApEnabled(config, true)
            if (wifiSet == true){
                server.startServer()
                server_ip.text = "http://" + wifiHotSpotManager?.getIpAddr() + ":7000"
            }
        }
    }

}
