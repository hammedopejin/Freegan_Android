package com.planetpeopleplatform.freegan_android.Activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.planetpeopleplatform.freegan_android.R
import com.planetpeopleplatform.freegan_android.Utility.Post
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_feed.*
import java.util.*

/**
 * Created by hammedopejin on 9/14/17.
 */

class ContactGvActivity : AppCompatActivity() {

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    //    var ListTickets = ArrayList<Ticket>()
    var ListPosts = ArrayList<Post>()

    var myemail:String? = null
    var UserUID:String? = null
    var userName:String? = null


    val PRFNEW = 234


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_giver)

        var b: Bundle =intent.extras
        myemail=b.getString("email")
        UserUID=b.getString("uid")







    }



















    fun loadUserPrfPic(){
        myRef.child("users").child(UserUID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                try {

                    var td= dataSnapshot!!.value as HashMap<String, Any>

                    for(key in td.keys){
                        if (key.equals("userImgUrl")) {
                            var userPic = td[key] as String
                            Picasso.with(applicationContext).load(userPic).into(userSmallImg)

                        }
                    }


                }catch (ex:Exception){}
            }
            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }



    fun backBtn(view: View) {

        var intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("email", myemail)
        intent.putExtra("uid", UserUID)

        startActivityForResult(intent, PRFNEW)



    }





}




