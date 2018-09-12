package com.musicplayer.aow.utils.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast


/**
 * Created by Arca on 11/30/2017.
 */
class IncomingSms : BroadcastReceiver() {
    // Get the object of SmsManager
    internal val sms = SmsManager.getDefault()
    override fun onReceive(context: Context, intent: Intent) {
        // Retrieves a map of extended data from the intent.
        val bundle = intent.extras
        try {
            if (bundle != null) {
                val pdusObj = bundle.get("pdus") as Array<Any>
                for (i in pdusObj.indices) {
//                    val currentMessage = SmsMessage.createFromPdu(pdusObj[i] as ByteArray, "")
//                    val phoneNumber = currentMessage.displayOriginatingAddress
//                    val message = currentMessage.displayMessageBody
//                    Log.e("SmsReceiver", "senderNum: $phoneNumber; message: $message")
//                    // Show Alert
//                    val duration = Toast.LENGTH_LONG
//                    val toast = Toast.makeText(context,
//                            "senderNum: $phoneNumber, message: $message", duration)
//                    toast.show()
                } // end for loop
            } // bundle is null
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e)
        }
    }
}