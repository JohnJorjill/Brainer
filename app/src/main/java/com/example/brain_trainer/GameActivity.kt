package com.example.brain_trainer

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import kotlin.math.round

class GameActivity : AppCompatActivity() {

    var score: Int = 0
    var scoreTextView: TextView? = null
    var timeTextView: TextView? = null
    var answerTextView1: TextView? = null
    var answerTextView2: TextView? = null
    var answerTextView3: TextView? = null
    var answerTextView4: TextView? = null
    var isCounterRunning: Boolean? = null
    var mCountDownTimer:CountDownTimer? = null
    var locationOfCorrectAnswer: Int? = null
    var answers = ArrayList<Int>()
    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        supportActionBar!!.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()
        val user = auth!!.currentUser

        answerTextView1 = findViewById(R.id.answerTextView1)
        answerTextView2 = findViewById(R.id.answerTextView2)
        answerTextView3 = findViewById(R.id.answerTextView3)
        answerTextView4 = findViewById(R.id.answerTextView4)
        scoreTextView = findViewById(R.id.scoreTextView)
        timeTextView = findViewById(R.id.timeTextView)

        isCounterRunning = true

        mCountDownTimer = object : CountDownTimer(30100, 1000) {
            override fun onTick(l: Long) {
                timeTextView?.setText("time: "+(l / 1000).toString()+"s")
            }

            override fun onFinish() {

                var new_sum_score:Int? = null
                var new_total_played:Int? = null
                isCounterRunning = false
                user?.getUid()?.let {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(
                        it
                    ).child("score")
                }?.setValue((score))

                val myTopPostsQuery = FirebaseDatabase.getInstance().getReference().child("Users").child(user!!.getUid())
                // get children from child
                myTopPostsQuery.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                         if (dataSnapshot.child("high_score").exists()){
                             val high_score = dataSnapshot.child("high_score").getValue(Int::class.java)!!
                             val score = dataSnapshot.child("score").getValue(Int::class.java)!!
                             if (score>high_score){
                                 user?.getUid()?.let {
                                     FirebaseDatabase.getInstance().getReference().child("Users").child(
                                         it
                                     ).child("high_score")
                                 }?.setValue((score))
                             }
                         } else {
                             user?.getUid()?.let {
                                 FirebaseDatabase.getInstance().getReference().child("Users").child(
                                     it
                                 ).child("high_score")
                             }?.setValue((score))
                         }

                        if (dataSnapshot.child("sum_score").exists()){
                            val sum_score = dataSnapshot.child("sum_score").getValue(Int::class.java)!!
                            new_sum_score = sum_score + score
                            user?.getUid()?.let {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(
                                    it
                                ).child("sum_score")
                            }?.setValue((new_sum_score))
                        } else {
                            user?.getUid()?.let {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(
                                    it
                                ).child("sum_score")
                            }?.setValue((score))
                        }

                        if (dataSnapshot.child("total_played").exists()){
                            val total_played = dataSnapshot.child("total_played").getValue(Int::class.java)!!
                            new_total_played = total_played + 1
                            user?.getUid()?.let {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(
                                    it
                                ).child("total_played")
                            }?.setValue((new_total_played))
                        } else {
                            user?.getUid()?.let {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(
                                    it
                                ).child("total_played")
                            }?.setValue((1))
                        }

                        if (dataSnapshot.child("avg_score").exists()){
                            var new_avg_score:Double? = null
                            val x = new_sum_score?.toDouble()
                            val y = new_total_played?.toDouble()
                            new_avg_score = x!! / y!!
                            user?.getUid()?.let {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(
                                    it
                                ).child("avg_score")
                            }?.setValue((new_avg_score))
                        } else {
                            var new_avg_score:Double? = null
                            new_avg_score = score.toDouble()
                            user?.getUid()?.let {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(
                                    it
                                ).child("avg_score")
                            }?.setValue((new_avg_score))
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@GameActivity,"problem",Toast.LENGTH_SHORT).show()
                    }
                })

                val intent = Intent(getApplicationContext(),ScoreActivity :: class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }

        playAgain(findViewById(R.id.timeTextView))
    }

    fun playAgain(view:View) {
        score = 0
        scoreTextView?.text = "score: " + Integer.toString(score)
        timeTextView?.setText("time: 30s")
        newQuestion()



        if(isCounterRunning==false){
            isCounterRunning = true
            mCountDownTimer?.start()
        }else{
            mCountDownTimer?.cancel()
            mCountDownTimer?.start()
        }
    }

    fun chooseAnswer(view:View){
        if(isCounterRunning==true){
            if (Integer.toString(locationOfCorrectAnswer!!) == view.tag.toString()){
            score++
        }else{
            if(score<=0){
                score = 0
            } else {
                score--
            }
            Toast.makeText(this," Wrong Answer -_- ",Toast.LENGTH_SHORT).show()
        }
        scoreTextView?.text = "score: " + Integer.toString(score)
        newQuestion()
        } else {
            Toast.makeText(this," Game over! ",Toast.LENGTH_SHORT).show()
        }
    }

    fun newQuestion(){

        val rand = Random()

        var a = rand.nextInt(100)
        var b = rand.nextInt(100)
        val sign = rand.nextInt(4)
        val varSign = rand.nextInt(3)
        var signString:String = ""
        var ans:Int = 0

        if(sign==0){
            signString = "+"
            ans = a+b
        }else if (sign==1){
            signString = "-"
            while(a<b){
                a = rand.nextInt(100)
                b = rand.nextInt(100)
            }
            ans = a-b
        }else if (sign==2){
            signString = "*"
            a = rand.nextInt(20)
            b = rand.nextInt(20)
            ans = a*b
        }else if (sign==3){
            signString = "/"
            while (b==0 || a%b!=0){
                    a = rand.nextInt(20)
                    b = rand.nextInt(20)
            }
            ans = a/b
        }

        val aString = Integer.toString(a)
        val bString = Integer.toString(b)

        val problemText:String = aString+signString+bString

        problemTextView?.text = problemText
        locationOfCorrectAnswer = rand.nextInt(4)
        answers.clear()

        var plusTen:Int? = null

        for (i in 0..4){
            if (i == locationOfCorrectAnswer){
                answers.add(ans)
            } else {
                var wrongAnswer: Int? =null

                if(varSign==0){
                    wrongAnswer = ans-rand.nextInt(20)
                } else if (varSign==1) {
                    wrongAnswer = ans+rand.nextInt(20)
                } else if (varSign==2 && plusTen==null){
                    plusTen = ans + 10
                    wrongAnswer = plusTen
                } else {
                    wrongAnswer = ans-rand.nextInt(20)
                }

                while(wrongAnswer == ans){
                    if(varSign==0){
                        wrongAnswer = ans-rand.nextInt(20)
                    } else if (varSign==1) {
                        wrongAnswer = ans+rand.nextInt(20)
                    } else if (varSign==2 && plusTen==null){
                        plusTen = ans + 10
                        wrongAnswer = plusTen
                    } else {
                        wrongAnswer = ans-rand.nextInt(20)
                    }
                }

                if (wrongAnswer != null) {
                    answers.add(wrongAnswer)
                }
            }
        }

        answerTextView1?.text = Integer.toString(answers.get(0))
        answerTextView2?.text = Integer.toString(answers.get(1))
        answerTextView3?.text = Integer.toString(answers.get(2))
        answerTextView4?.text = Integer.toString(answers.get(3))

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer!!.cancel()
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

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

}