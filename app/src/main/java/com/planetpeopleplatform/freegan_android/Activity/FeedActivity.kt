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
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.planetpeopleplatform.freegan_android.Activity.ProfileActivity
import com.planetpeopleplatform.freegan_android.Activity.SignInActivity
import com.planetpeopleplatform.freegan_android.Utility.Post
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_feed.*
import kotlinx.android.synthetic.main.posts_ticket.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by hammedopejin on 8/24/17.
 */

class FeedActivity : AppCompatActivity() {

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    lateinit var likesRef: DatabaseReference
    var post: Post? = null

    //    var ListTickets = ArrayList<Ticket>()
    var ListPosts = ArrayList<Post>()
    var adpater: PostAdpater? = null
    var myemail:String? = null
    var UserUID:String? = null
    var userName:String? = ""
    var postDownloadURL:String? = ""
    var userDownloadURL:String? = ""
    var imageSelected:Boolean? = false
    var picturePath:String? = ""


    val PRFNEW = 234
    val PICK_IMAGE_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        var b:Bundle = intent.extras
        myemail = b.getString("email")
        UserUID = b.getString("uid")
        userName = b.getString("userName")




        adpater= PostAdpater(this,ListPosts)
        lvPosts.adapter=adpater

        LoadPost()
        loadUserPrfPic()

    }

    override fun onResume() {
        super.onResume()
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


            post = listTicketsAdpater[p0]

            likesRef = myRef.child("users").child(UserUID).child("likes").child(post!!.postKey)

            if(post!!.userName.equals("loading")){
                myView = layoutInflater.inflate(R.layout.loading_ticket,null)
                return myView!!
            }else{

                myView = layoutInflater.inflate(R.layout.posts_ticket,null)


                myRef.child("posts")
                        .addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                                try {

                                    var td= dataSnapshot!!.value as HashMap<String,Any>

                                    for(key in td.keys){
                                        myView!!.tvUserName.text= post!!.userName
                                        Picasso.with(context).load(post!!.profileImgUrl).into(myView!!.user_img)
                                        Picasso.with(context).load(post!!.imageUrl).into(myView!!.post_image)
                                        myView!!.caption.text = post!!.description
                                        myView!!.likes.text =  "" + post!!.likes
                                        myView!!.tv_tweet_date.text = post!!.postDate

                                        likesRef.addListenerForSingleValueEvent(object :ValueEventListener{
                                            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                                                //Toast.makeText(applicationContext, dataSnapshot!!.value.toString(), Toast.LENGTH_LONG).show()
                                                if (dataSnapshot!!.value == true || check == true) {
                                                    myView!!.loveImg.setImageResource(R.drawable.filled_heart)
                                                } else {
                                                    myView!!.loveImg.setImageResource(R.drawable.empty_heart)
                                                }
                                            }
                                            override fun onCancelled(p0: DatabaseError?) {}
                                        })

                                    }


                                }catch (ex:Exception){}


                            }

                            override fun onCancelled(p0: DatabaseError?) {

                            }
                        })

                return myView!!
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
            picturePath=cursor.getString(coulomIndex)
            cursor.close()
            Picasso.with(this).load(selectedImage).into(img_post)
            imageSelected = true

        }
        if(requestCode==PRFNEW  && data!=null && resultCode == RESULT_OK){
            userName = data.getStringExtra("userName")
        }

    }





    fun uploadImage(bitmap: Bitmap){
//        ListTickets.add(0, Ticket("0","him","url","loading"))
//        adpater!!.notifyDataSetChanged()

        val storage= FirebaseStorage.getInstance()
        val storgaRef=storage.getReferenceFromUrl("gs://freegan-42b40.appspot.com")
        val df= SimpleDateFormat("ddMMyyHHmmss")
        val dataobj= Date()
        val imagePath= SplitString(myemail!!) + "."+ df.format(dataobj)+ ".jpg"
        val ImageRef=storgaRef.child("post-pics/"+imagePath )
        val baos= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,10,baos)
        val data= baos.toByteArray()
        val uploadTask=ImageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(applicationContext,"successfully uploaded image", Toast.LENGTH_LONG).show()
            postDownloadURL = taskSnapshot.downloadUrl!!.toString()
            this.picturePath = ""
            postToFirebase()
//            ListTickets.removeAt(0)
//            adpater!!.notifyDataSetChanged()

        }
    }



    fun SplitString(email:String):String{
        val split= email.split("@")
        return split[0]
    }


    fun loadUserPrfPic(){
        myRef.child("users").child(UserUID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                try {

                    var td= dataSnapshot!!.value as HashMap<String,Any>

                    for(key in td.keys){
                        if (key.equals("userImgUrl")) {
                            userDownloadURL = td[key] as String
                            Picasso.with(applicationContext).load(userDownloadURL).into(userSmallImg)

                        }
                        if (key.equals("userName")) {
                            userName = td[key] as String


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


    fun postBtnTapped(view: View){
        var desc = etItemDesc.text.toString()
        if(desc.equals("")){
            Toast.makeText(applicationContext,"Item must have description", Toast.LENGTH_LONG).show()
            return
        }
        if(imageSelected == false){
            Toast.makeText(applicationContext,"An image must be selected", Toast.LENGTH_LONG).show()
            return
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = 2

        if(this!!.picturePath!!.equals("") == false) {
            uploadImage(BitmapFactory.decodeFile(picturePath, options))
        }
    }

    fun postToFirebase() {


        val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        val dataobj= Date()

        //val post =  Post(etItemDesc.text.toString(), this!!.postDownloadURL!!, 0 as Any, this!!.userDownloadURL!!, this.userName!! as String, sfd.format(dataobj))
        val post : HashMap<String, Any> = HashMap<String, Any>()
            post.put("description", etItemDesc.text.toString() as Any)
            post.put("imageUrl", this!!.postDownloadURL!! as Any)
            post.put("likes", 0 as Any)
            post.put("profileImgUrl", this!!.userDownloadURL!! as Any)
            post.put("userName", this.userName!! as Any)
            post.put("postDate", sfd.format(dataobj) as Any)

        myRef.child("posts").push().setValue(post)

        img_post.setImageResource(R.drawable.add_image)
        etItemDesc.text.clear()
        imageSelected = false

        LoadPost()
    }




    fun LoadPost(){

        myRef.child("posts")
                .addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        try {

                            ListPosts.clear()


                            var td= dataSnapshot!!.value as HashMap<String,Any>

                            for(key in td.keys) {
                                var likes: Any = 0
                                var post = td[key] as HashMap<String, Any>
                                ListPosts.add(Post(key, post))
                                for (keey in post) {
                                    if (keey.key == "likes") {
                                        likes = keey.value
                                        ListPosts.last().likes = likes
                                    }

                                }


                            }

                            adpater!!.notifyDataSetChanged()
                        }catch (ex:Exception){}


                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
    }


    fun signOuttapped(v: View) {
        val i = Intent(this, SignInActivity::class.java)
        startActivity(i)

    }
    fun goToProfile(v: View) {


        var intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("email", myemail)
        intent.putExtra("uid", UserUID)
        intent.putExtra("userName", userName)

        startActivityForResult(intent, PRFNEW)


    }
    var check = false

    fun likeTapped(v: View) {
        if (check == false) {
            v.loveImg.setImageResource(R.drawable.filled_heart)
            post!!.adjustLikes(true)
            likesRef.setValue(true)
            check = true

        } else {
            v.loveImg.setImageResource(R.drawable.empty_heart)
            post!!.adjustLikes(false)
            likesRef.removeValue()
            check = false

        }
    }



}