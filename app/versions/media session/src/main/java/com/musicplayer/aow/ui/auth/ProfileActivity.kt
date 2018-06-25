package com.musicplayer.aow.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        var userdata = auth!!.currentUser
        user_name.text = userdata!!.email
        var coin = 0.0


        var hour = 30000000L
        coin = tv1.text.toString().toDouble()
        object : CountDownTimer(hour, 10000) {

            override fun onTick(millisUntilFinished: Long) {
                coin += 0.1
                var coinString = "${coin.toInt()}"
                tv1.text = "${coin.toInt()}"
                //myRef.setValue(FirebaseInstanceId.getInstance().token)
                var userdata = auth!!.currentUser
                //myRef.child("users").child(userdata!!.uid).setValue(coinString)
            }

            override fun onFinish() {
                var coinString = "${coin.toInt()}"
                // Write a message to the database
                //myRef.setValue(FirebaseInstanceId.getInstance().token)
                var userdata = auth!!.currentUser
                //myRef.child("users").child(userdata!!.uid).setValue(coinString)
            }
        }.start()

        log_out.setOnClickListener {
            //logout
            auth!!.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
