package com.musicplayer.aow.utils.sharefile.wifihotspot


/**
 * Created by Arca on 11/29/2017.
 */
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.net.InetAddress
import java.util.*


class WifiHotSpotManager(private val context: Context) {
    private val mWifiManager: WifiManager

    /**
     * Gets the Wi-Fi enabled state.
     *
     * @return [WIFI_AP_STATE]
     * @see .isWifiApEnabled
     */
    // Fix for Android 4
    val wifiApState: WIFI_AP_STATE
        get() {
            try {
                val method = mWifiManager.javaClass.getMethod("getWifiApState")

                var tmp = method.invoke(mWifiManager) as Int
                if (tmp >= 10) {
                    tmp = tmp - 10
                }

                return WIFI_AP_STATE::class.java!!.getEnumConstants()[tmp]
            } catch (e: Exception) {
                Log.e(this.javaClass.toString(), "", e)
                return WIFI_AP_STATE.WIFI_AP_STATE_FAILED
            }

        }

    /**
     * Return whether Wi-Fi AP is enabled or disabled.
     *
     * @return `true` if Wi-Fi AP is enabled
     * @hide Dont open yet
     * @see .getWifiApState
     */
    val isWifiApEnabled: Boolean
        get() = wifiApState === WIFI_AP_STATE.WIFI_AP_STATE_ENABLED

    /**
     * Gets the Wi-Fi AP Configuration.
     *
     * @return AP details in [WifiConfiguration]
     */
    val wifiApConfiguration: WifiConfiguration?
        get() {
            try {
                val method = mWifiManager.javaClass.getMethod("getWifiApConfiguration")
                return method.invoke(mWifiManager) as WifiConfiguration
            } catch (e: Exception) {
                Log.e(this.javaClass.toString(), "", e)
                return null
            }

        }

    init {
        mWifiManager = this.context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    /**
     * Show write permission settings page to user if necessary or forced
     * @param force show settings page even when rights are already granted
     */
    fun showWritePermissionSettings(force: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (force || !Settings.System.canWrite(this.context)) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + this.context.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.context.startActivity(intent)
            }
        }
    }


    fun getIpAddr(): String? {
        val wifiInfo = mWifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        return Formatter.formatIpAddress(wifiInfo.ipAddress);
    }

    /**
     * Start AccessPoint mode with the specified
     * configuration. If the radio is already running in
     * AP mode, update the new configuration
     * Note that starting in access point mode disables station
     * mode operation
     *
     * @param wifiConfig SSID, security and channel details as part of WifiConfiguration
     * @return `true` if the operation succeeds, `false` otherwise
     */
    fun setWifiApEnabled(wifiConfig: WifiConfiguration?, enabled: Boolean): Boolean {
        try {
            if (enabled) { // disable WiFi in any case
                mWifiManager.isWifiEnabled = false
            }

            val method = mWifiManager.javaClass.getMethod("setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType)
            return method.invoke(mWifiManager, wifiConfig, enabled) as Boolean
        } catch (e: Exception) {
            Log.e(this.javaClass.toString(), "", e)
            return false
        }

    }

    /**
     * Sets the Wi-Fi AP Configuration.
     *
     * @return `true` if the operation succeeded, `false` otherwise
     */
    fun setWifiApConfiguration(wifiConfig: WifiConfiguration): Boolean {
        try {
            val method = mWifiManager.javaClass.getMethod("setWifiApConfiguration", WifiConfiguration::class.java)
            return method.invoke(mWifiManager, wifiConfig) as Boolean
        } catch (e: Exception) {
            Log.e(this.javaClass.toString(), "", e)
            return false
        }

    }

    /**
     * Gets a list of the clients connected to the Hotspot, reachable timeout is 300
     *
     * @param onlyReachables  `false` if the list should contain unreachable (probably disconnected) clients, `true` otherwise
     * @param finishListener, Interface called when the scan method finishes
     */
    fun getClientList(onlyReachables: Boolean, finishListener: FinishScanListener) {
        getClientList(onlyReachables, 300, finishListener)
    }

    /**
     * Gets a list of the clients connected to the Hotspot
     *
     * @param onlyReachables   `false` if the list should contain unreachable (probably disconnected) clients, `true` otherwise
     * @param reachableTimeout Reachable Timout in miliseconds
     * @param finishListener,  Interface called when the scan method finishes
     */
    fun getClientList(onlyReachables: Boolean, reachableTimeout: Int, finishListener: FinishScanListener) {
        val runnable = object : Runnable {
            override fun run() {

                var br: BufferedReader? = null
                val result = ArrayList<ClientScanResult>()

                try {
                    br = BufferedReader(FileReader("/proc/net/arp"))
                    var line: String
                    line = br.readLine()
                    while ((line) != null) {
                        val splitted = line.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        if (splitted != null && splitted.size >= 4) {
                            // Basic sanity check
                            val mac = splitted[3]

                            if (mac.matches("..:..:..:..:..:..".toRegex())) {
                                val isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout)

                                if (!onlyReachables || isReachable) {
                                    result.add(ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(this.javaClass.toString(), e.toString())
                } finally {
                    try {
                        br!!.close()
                    } catch (e: IOException) {
                        Log.e(this.javaClass.toString(), e.message)
                    }

                }

                // Get a handler that can be used to post to the main thread
                val mainHandler = Handler(context.mainLooper)
                val myRunnable = Runnable { finishListener.onFinishScan(result) }
                mainHandler.post(myRunnable)
            }
        }

        val mythread = Thread(runnable)
        mythread.start()
    }
}