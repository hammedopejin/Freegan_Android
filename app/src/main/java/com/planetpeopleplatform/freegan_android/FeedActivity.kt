package com.planetpeopleplatform.freegan_android

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_feed.*
import kotlinx.android.synthetic.main.posts_ticket.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FeedActivity : AppCompatActivity() {

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

//    var ListTickets = ArrayList<Ticket>()
    var ListPosts = ArrayList<Post>()
    var adpater:PostAdpater? = null
    var myemail:String? = null
    var UserUID:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        var b:Bundle=intent.extras
        myemail=b.getString("email")
        UserUID=b.getString("uid")




        adpater= PostAdpater(this,ListPosts)
        lvPosts.adapter=adpater

        LoadPost()
        loadUserPrfPic()

    }




    inner class  PostAdpater: BaseAdapter {
        var listTicketsAdpater=ArrayList<Post>()
        var context: Context?=null
        var myView : View? = null
        constructor(context:Context, listTicketsAdpater:ArrayList<Post>):super(){
            this.listTicketsAdpater = listTicketsAdpater
            this.context=context
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {


            var mypost = listTicketsAdpater[p0]

            if(mypost.userName.equals("loading")){
                 myView=layoutInflater.inflate(R.layout.loading_ticket,null)
                return (myView as View?)!!
            }else{

                 myView = layoutInflater.inflate(R.layout.posts_ticket,null)


                myRef.child("posts")
                        .addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                                try {

                                    var td= dataSnapshot!!.value as HashMap<String,Any>

                                    for(key in td.keys){
//                                        Toast.makeText(applicationContext,mypost.likes.toString(),Toast.LENGTH_LONG).show()
                                            myView!!.userName.text= mypost.userName
                                            Picasso.with(context).load(mypost.profileImgUrl).into(myView!!.user_img)
                                            Picasso.with(context).load(mypost.imageUrl).into(myView!!.post_image)
                                            myView!!.caption.text = mypost.description
                                            myView!!.likes.text =  "" + mypost.likes as Int

                                    }


                                }catch (ex:Exception){}


                            }

                            override fun onCancelled(p0: DatabaseError?) {

                            }
                        })



                return (myView as View?)!!
            }



        }

        override fun getItem(p0: Int): Any {
            return listTicketsAdpater[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {

            return listTicketsAdpater.size

        }



    }





    val PICK_IMAGE_CODE=123
    fun loadImage(){

        var intent= Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==PICK_IMAGE_CODE  && data!=null && resultCode == RESULT_OK){

            val selectedImage=data.data
            val filePathColum= arrayOf(MediaStore.Images.Media.DATA)
            val cursor= contentResolver.query(selectedImage,filePathColum,null,null,null)
            cursor.moveToFirst()
            val coulomIndex=cursor.getColumnIndex(filePathColum[0])
            val picturePath=cursor.getString(coulomIndex)
            cursor.close()
            Picasso.with(this).load(selectedImage).into(img_post)
            uploadImage(BitmapFactory.decodeFile(picturePath))
        }

    }



    var DownloadURL:String?=""

    fun uploadImage(bitmap: Bitmap){
//        ListTickets.add(0, Ticket("0","him","url","loading"))
        adpater!!.notifyDataSetChanged()

        val storage= FirebaseStorage.getInstance()
        val storgaRef=storage.getReferenceFromUrl("gs://freegan-42b40.appspot.com")
        val df= SimpleDateFormat("ddMMyyHHmmss")
        val dataobj= Date()
        val imagePath= SplitString(myemail!!) + "."+ df.format(dataobj)+ ".jpg"
        val ImageRef=storgaRef.child("post-pics/"+imagePath )
        val baos= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data= baos.toByteArray()
        val uploadTask=ImageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->

            DownloadURL= taskSnapshot.downloadUrl!!.toString()
//            ListTickets.removeAt(0)
            adpater!!.notifyDataSetChanged()

        }
    }


    fun SplitString(email:String):String{
        val split= email.split("@")
        return split[0]
    }


    fun loadUserPrfPic(){
        //profilepic
        myRef.child("users").child(UserUID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                try {

                    var td= dataSnapshot!!.value as HashMap<String,Any>

                    for(key in td.keys){
                        if (key.equals("userImgUrl")) {
                            var userPic = td[key] as String
                            Picasso.with(applicationContext).load(userPic).into(prfpic)

                        }
                    }


                }catch (ex:Exception){}
            }
            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }







    fun imgBtn(view: View) {
        loadImage()
    }


    fun backBtn(view: View) {


        myRef.child("posts").push().setValue(
                Post("milk", DownloadURL!!, 0,  DownloadURL!!,"milk"))

//        view.caption.setText("")


    }

    fun LoadPost(){

        myRef.child("posts")
                .addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        try {

                            ListPosts.clear()


                            var td= dataSnapshot!!.value as HashMap<String,Any>

                            for(key in td.keys){

                                var post= td[key] as HashMap<String,Any>


                                    ListPosts.add(Post(key, post))


                            }



                            adpater!!.notifyDataSetChanged()
                        }catch (ex:Exception){}


                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
    }


    fun onAddItem(v: View) {
        val i = Intent(this, SignInActivity::class.java)
        startActivity(i)

    }

}
