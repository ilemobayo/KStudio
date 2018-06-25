package com.musicplayer.aow.ui.auth

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.musicplayer.aow.R
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete


class ProfileActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        val userdata = auth!!.currentUser
        user_name.text = userdata!!.displayName
        user_email.text = userdata.email
        user_key.text = ""

        doAsync {
            val img = Glide.with(applicationContext)
                    .load(userdata.photoUrl).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(550, 550)
                    .get()
            onComplete {
                photo.setImageBitmap(img)
            }
        }

        log_out.setOnClickListener {
            //logout
            auth!!.signOut()
            finish()
        }
    }
}
