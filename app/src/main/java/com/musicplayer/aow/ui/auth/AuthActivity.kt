package com.musicplayer.aow.ui.auth

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.musicplayer.aow.R
import com.musicplayer.aow.ui.auth.google.model.User
import com.musicplayer.aow.utils.user.UserDetails
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.regex.Pattern


class AuthActivity : AppCompatActivity(){

    private val TAG = this.javaClass.name
    private val RC_SIGN_IN = 9001
    private var mGoogleSignInClient: GoogleSignInClient? = null

    private var inputEmail: EditText? = null
    var inputPassword: EditText? = null
    private var auth: FirebaseAuth? = null
    private var btnSignup: Button? = null
    var btnLogin: Button? = null
    var btnReset: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance()

        if (auth!!.currentUser != null) {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
            finish()
        }

        setContentView(R.layout.activity_auth)


        inputEmail = findViewById<View>(R.id.email) as EditText
        inputPassword = findViewById<View>(R.id.password) as EditText
        btnSignup = findViewById<View>(R.id.btn_signup) as Button
        btnLogin = findViewById<View>(R.id.btn_login) as Button
        btnReset = findViewById<View>(R.id.btn_reset_password) as Button


        btnSignup!!.setOnClickListener({ startActivity(Intent(this, SignUpActivity::class.java)) })

        //btnReset!!.setOnClickListener(View.OnClickListener { startActivity(Intent(this, ResetPasswordActivity::class.java)) })

        btnLogin!!.setOnClickListener(View.OnClickListener {
            val email = inputEmail!!.text.toString()
            val password = inputPassword!!.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }


            //authenticate user
            auth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful) {
                            // there was an error
                            if (password.length < 6) {
                                inputPassword!!.error = getString(R.string.minimum_password)
                            } else {
                                Toast.makeText(applicationContext, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // Write a message to the database
                            //myRef.setValue(FirebaseInstanceId.getInstance().token)
                            var userdata = auth!!.currentUser
                            var user = UserDetails()
                            var userData = User(userdata!!.displayName, user.getMyPhoneNO(this).uline1Number, userdata.email, null, user.getMyPhoneNO(this))

                            val intent = Intent(applicationContext, ProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    })
        })

        login_with_google.setOnClickListener {
            signIn()
        }

    }

    fun checkLogin(arg0: View) {

        //val email = emailEditText!!.text.toString()
        //if (!isValidEmail(email)) {
            //Set error message for email field
            //emailEditText!!.error = "Invalid Email"
        //}

        //val pass = passEditText!!.text.toString()
        //if (!isValidPassword(pass)) {
            //Set error message for password field
            //passEditText!!.error = "Password cannot be empty"
        //}

        //if (isValidEmail(email) && isValidPassword(pass)) {
            // Validation Completed

            finish()
        //}

    }

    // validating email id
    private fun isValidEmail(email: String): Boolean {
        val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    // validating password
    private fun isValidPassword(pass: String?): Boolean {
        return pass != null && pass.length >= 4
    }



    //GOOGLE LOGIN

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val apiException = ApiException::class.java
                val account = task.getResult(apiException)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null)
                // [END_EXCLUDE]
            }

        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        // [START_EXCLUDE silent]
//        showProgressDialog()
        // [END_EXCLUDE]

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth!!.signInWithCredential(credential)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user: FirebaseUser = auth!!.currentUser!!
                        Toast.makeText(applicationContext, user.email, Toast.LENGTH_LONG).show()
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(applicationContext, "Google sign in failed", Toast.LENGTH_LONG).show()
                    }

                    // [START_EXCLUDE]
                    //hideProgressDialog()
                    // [END_EXCLUDE]
                })
    }

    // [START signin]
    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]

    private fun signOut() {
        // Firebase sign out
        auth!!.signOut()

        // Google sign out
        mGoogleSignInClient!!.signOut().addOnCompleteListener(this
        ) { updateUI(null) }
    }

    private fun revokeAccess() {
        // Firebase sign out
        auth!!.signOut()

        // Google revoke access
        mGoogleSignInClient!!.revokeAccess().addOnCompleteListener(this
        ) { updateUI(null) }
    }

    private fun updateUI(user: FirebaseUser?) {
        //hideProgressDialog()
        if (user != null) {
            val intent = Intent(applicationContext, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth?.currentUser
        updateUI(currentUser)
    }

//    fun onClick(v: View) {
//        val i = v.id
//        if (i == R.id.sign_in_button) {
//            signIn()
//        } else if (i == R.id.sign_out_button) {
//            signOut()
//        } else if (i == R.id.disconnect_button) {
//            revokeAccess()
//        }
//    }

}