package com.musicplayer.aow.utils.user

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager



/**
 * Created by Arca on 11/28/2017.
 */
class UserDetails{
    @SuppressLint("MissingPermission")
    fun getMyPhoneNO(context: Context): UserPhoneDetails {
        val mTelephonyMgr: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return UserPhoneDetails(mTelephonyMgr.simSerialNumber, mTelephonyMgr.subscriberId, mTelephonyMgr.deviceSoftwareVersion, mTelephonyMgr.line1Number)
    }

    class UserPhoneDetails(deviceId: String, subscriberId: String, deviceSoftwareVersion: String, line1Number: String){
            var udeviceId = deviceId
            var usubscriberId = subscriberId
            var udeviceSoftwareVersion = deviceSoftwareVersion
            var uline1Number = line1Number
    }
}