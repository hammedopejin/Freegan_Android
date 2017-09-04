package com.planetpeopleplatform.freegan_android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class FeedActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

    }



    fun backBtn(view: View) {
        var intent = Intent(this, SignInActivity::class.java)

        startActivity(intent)
    }
}
