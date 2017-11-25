package com.planetpeopleplatform.freegan_android.Activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.planetpeopleplatform.freegan_android.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_contact_giver.*
import java.util.*

/**
 * Created by hammedopejin on 9/14/17.
 */

class ContactGvActivity : AppCompatActivity() {

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    var myemail:String? = null
    var UserUID:String? = null
    var postKey:String? = null
    var userName:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_giver)

        var b: Bundle =intent.extras
        myemail=b.getString("email")
        UserUID=b.getString("uid")
        postKey=b.getString("pkey")

        loadUserPrfPic()


        myRef.child("posts").child(postKey).addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        try {
                            //Toast.makeText(applicationContext, postKey, Toast.LENGTH_LONG).show()
                            var td= dataSnapshot!!.value as HashMap<String,Any>

                            for(key in td.keys) {
                                        tvDesc.text = td["description"] as String
                                        Picasso.with(applicationContext).load(td["imageUrl"] as String).into(ivpost_image)
                            }
                        }catch (ex:Exception){}


                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })


    }


    fun loadUserPrfPic(){
        myRef.child("users").child(UserUID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                try {

                    var td= dataSnapshot!!.value as HashMap<String, Any>

                    for(key in td.keys){
                        if (key.equals("userImgUrl")) {
                            var userPic = td[key] as String
                            Picasso.with(applicationContext).load(userPic).into(ivprfIcon)


                        }
                    }


                }catch (ex:Exception){}
            }
            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }



    fun backBtn(view: View) {
       finish()
    }





}




