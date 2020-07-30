package com.brainerjorjill

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatsActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var highTextView: TextView? = null
    var avgTextView: TextView? = null
    var totalTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        highTextView = findViewById(R.id.highTextView)
        avgTextView = findViewById(R.id.avgTextView)
        totalTextView = findViewById(R.id.totalTextView)

        auth = FirebaseAuth.getInstance()
        val user = auth!!.currentUser

        val myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child("Users").child(user!!.getUid())
        // get children from child "Users" add to list and connect to recyclerview
        myTopPostsQuery.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.child("high_score").exists()){
                    val high_score = Integer.toString(dataSnapshot.child("high_score").getValue(Int::class.java)!!)
                    highTextView?.setText(high_score)
                } else {
                    highTextView?.setText("0")
                }

                if (dataSnapshot.child("avg_score").exists()){
                    val avg_score = (dataSnapshot.child("avg_score").getValue(Double::class.java)!!).toString().substring(0,3)
                    avgTextView?.setText(avg_score)
                } else {
                    avgTextView?.setText("0.0")
                }

                if (dataSnapshot.child("total_played").exists()){
                    val total_played = Integer.toString(dataSnapshot.child("total_played").getValue(Int::class.java)!!)
                    totalTextView?.setText(total_played)
                } else {
                    totalTextView?.setText("0")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StatsActivity,"problem", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.quit_menu2, menu)
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
        } else if (item.itemId == R.id.back){
            val intent = Intent(this,MenuActivity :: class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return true
        }


        return false
    }
}