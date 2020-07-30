package com.brainerjorjill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class MainActivity : AppCompatActivity() {

    var mGoogleSignInClient: GoogleSignInClient? = null
    var RC_SIGN_IN: Int = 123;
    var auth: FirebaseAuth? = null
    var mCallbackManager: CallbackManager? = null
    var customFButton: Button? = null
    companion object {
        var uid:String? = null
    }


    override fun onStart() {
        super.onStart()

        // create a firebase user
        val currentUser = auth?.currentUser

        // if user if authorized, go to next activity
        if (currentUser!=null){
            val intent = Intent(this,MenuActivity :: class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getSupportActionBar()?.hide()

        // make auth
        auth = FirebaseAuth.getInstance()
        FacebookSdk.sdkInitialize(applicationContext)
        createRequest()
        mCallbackManager = CallbackManager.Factory.create()
        customFButton = findViewById(R.id.customFButton)

        customFButton?.setOnClickListener{
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"))
            LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    if (loginResult != null) {
                        handleFacebookAccessToken(loginResult.accessToken)
                    }
                }
                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    Toast.makeText(applicationContext,error.toString(),Toast.LENGTH_SHORT).show()
                }
            })
        }

        // google button
        val btn_click_me: Button = findViewById(R.id.googleButton)
        btn_click_me.setOnClickListener {
            signIn()
        }


    }

    fun createRequest(){
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
            }
        }

        mCallbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    // when signed in with google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth!!.currentUser
                    FirebaseDatabase.getInstance().getReference().child("Users").child(task.result!!.user!!.uid).child("uname").setValue(user?.displayName)

                    val myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child("Users").child(user!!.getUid())

                    myTopPostsQuery.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            if (!(dataSnapshot.child("score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("score")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("high_score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("high_score")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("sum_score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("sum_score")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("total_played").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("total_played")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("avg_score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("avg_score")
                                }?.setValue((0.0))
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })

                    val intent = Intent(this,MenuActivity :: class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // when signed in with facebook, if success go to menuactivity
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth?.currentUser
                    FirebaseDatabase.getInstance().getReference().child("Users").child(task.result!!.user!!.uid).child("uname").setValue(user?.displayName)

                    val myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child("Users").child(user!!.getUid())

                    myTopPostsQuery.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            if (!(dataSnapshot.child("score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("score")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("high_score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("high_score")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("sum_score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("sum_score")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("total_played").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("total_played")
                                }?.setValue((0))
                            }

                            if (!(dataSnapshot.child("avg_score").exists())){
                                user?.getUid()?.let {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                                        it
                                    ).child("avg_score")
                                }?.setValue((0.0))
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })

                    val intent = Intent(this,MenuActivity :: class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                } else if (!task.isSuccessful()) {
                    // If sign in fails, display a message to the user.
                    Log.w( "signInWithEmail:failed", task.getException())
                    Toast.makeText(this@MainActivity, "User Authentication Failed: " + task.getException()?.message, Toast.LENGTH_SHORT).show()
                }
                // ...
            }
    }

}