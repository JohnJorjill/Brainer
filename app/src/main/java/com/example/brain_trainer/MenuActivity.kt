package com.example.brain_trainer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.net.HttpURLConnection
import java.net.URL


class MenuActivity : AppCompatActivity() {

    var imageView: ImageView? = null
    var textView: TextView? = null
    var auth: FirebaseAuth? = null

    // if play is clicked, go to GameActivity
    fun onPlayClicked(view: View){
        val intent = Intent(this,GameActivity :: class.java)
        startActivity(intent)
    }

    fun onStatsClicked(view: View){
        val intent = Intent(this,StatsActivity :: class.java)
        startActivity(intent)
    }

    // if score clicked, start score activity
    fun onScoreClicked(view: View){
        val intent = Intent(this,ScoreActivity :: class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        supportActionBar!!.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()
        val user = auth!!.currentUser
        val photoUrl = user?.photoUrl
        val username = user?.displayName

        // sets user's image and username in menu
        textView?.setText("Welcome, " + username)
        FirebaseDatabase.getInstance().getReference().child("Users").child(user!!.uid).child("imageUrl").setValue(photoUrl.toString())
        val task = ImageDownloader()
        val myImage: Bitmap
        try {
            myImage = task.execute(photoUrl.toString()).get()
            imageView?.setImageBitmap(myImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    inner class ImageDownloader : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            try {
                val url = URL(urls[0])
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val `in` = connection.inputStream
                return BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.quit_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.quit) {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            val intent = Intent(this,MainActivity :: class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return true
        }
        return false
    }


}