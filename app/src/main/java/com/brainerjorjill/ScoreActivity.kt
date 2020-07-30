package com.brainerjorjill

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ScoreActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null
    var list: ArrayList<Profile>? = null
    var list2: ArrayList<Profile>? = ArrayList()
    var new_list: ArrayList<Profile>? = null
    var adapter:MyAdapter? = null


    fun onMenuClicked(view: View){
        val intent = Intent(this,MenuActivity :: class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        recyclerView = findViewById(R.id.myRecycler)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        list = ArrayList<Profile>()
        new_list = ArrayList<Profile>()

        // get sorted values from "Users" child
        val myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("score")
        // get children from child "Users" add to list and connect to recyclerview
        myTopPostsQuery.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list?.clear()
                for (dataSnapshot1 in dataSnapshot.children) {
                    // get data from firebase and add it to list
                    val p = dataSnapshot1.getValue(Profile::class.java)!!
                    list!!.add(p)
                }
                list?.reverse()

                var sizeLimit = 0
                if (list?.size!!>10){
                    sizeLimit = 10
                } else {
                    sizeLimit = list!!.size
                }

                for (x in 0 until sizeLimit){
                    list2?.add(list!![x])
                }

                list?.clear()

                adapter = MyAdapter(this@ScoreActivity, list2)
                // connect recyclerview to adapter
                recyclerView?.adapter = adapter

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ScoreActivity, "Something went wrong", Toast.LENGTH_LONG).show()
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