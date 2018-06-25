package com.musicplayer.aow.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Environment
import android.text.format.Formatter
import android.util.Log
import com.musicplayer.aow.application.Injection
import com.musicplayer.aow.ui.settings.server.WebServer
import fi.iki.elonen.SimpleWebServer
import java.io.File
import java.net.BindException
import java.net.InetAddress
import java.net.NetworkInterface

class FileServer(){

    //file server
    var server: SimpleWebServer? = null

    private var ip = InetAddress.getLoopbackAddress().hostAddress
    private var localip = "192.168.43.1"

    fun initServer() {
        server = SimpleWebServer(
                localip,
                7000,
                File(Environment.getExternalStorageDirectory().absolutePath),
                false)
    }

    fun reInitServer() {
        server = SimpleWebServer(
                "$localip",
                7000,
                File(Environment.getExternalStorageDirectory().absolutePath),
                false)
    }

    fun serverIp(): String? {
        return localip
    }

    fun startServer(){
        if (server != null) {
            if(!server!!.isAlive) {
                try {
                    server!!.start()
                    Log.e("server", "started " + InetAddress.getLoopbackAddress().hostAddress)
                }catch (e: BindException){
                    Log.e("server", "err: not running, address in use. $e")
                }
                if (server!!.wasStarted()) {
                    Log.e("server", "started " + InetAddress.getLoopbackAddress().hostAddress)
                } else {
                    Log.e("server", "not started")
                }
            }else{
                Log.e("server", "running")
            }
        }
    }

    fun stopServer(){
        if (server != null) {
            server!!.closeAllConnections()
            server!!.stop()
            Log.e("server", "stopped")
        }
    }

    fun closeAllConnections(){
        if (server != null) {
            server!!.closeAllConnections()
        }
    }

    fun GetDeviceipMobileData(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val networkinterface = en.nextElement()
                val enumIpAddr = networkinterface.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        Log.e("Current IP", inetAddress.hostAddress.toString())
                        return inetAddress.hostAddress.toString()
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("Current IP", ex.toString())
        }

        return null
    }

    fun GetDeviceipWiFiData(): String {
        val wm = Injection.provideContext()!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Log.e("Current IP", Formatter.formatIpAddress(wm.connectionInfo.ipAddress))
        return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }

    companion object {

        private var sInstance: FileServer? = null

        val instance: FileServer?
            get() {
                if (sInstance == null) {
                    synchronized(FileServer.Companion) {
                        if (sInstance == null) {
                            sInstance = FileServer()
                        }
                    }
                }
                return sInstance
            }
    }
}