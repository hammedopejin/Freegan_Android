package com.planetpeopleplatform.freegan_android

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.planetpeopleplatform.freegan_android.Activity.ContactGvActivity
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


    val PRFNEW = 234
    val PICK_IMAGE_CODE = 123
    val CONTACT_GIVER_CODE = 777

    lateinit var likesRef: DatabaseReference
    var post: Post? = null

    var ListPosts = ArrayList<Post>()
    var adapater: PostAdpater? = null
    var myEmail:String? = ""
    var UserUID:String? = null
    var pkey:String? = null
    var userName:String? = ""
    var postDownloadURL:String? = ""
    var userDownloadURL:String? = ""
    var imageSelected:Boolean? = false
    var picturePath:String? = ""
    var itemPosition:Int = 1
    var check = false
    var temp = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        var b:Bundle = intent.extras
        myEmail = b.getString("email")
        UserUID = b.getString("uid")
        userName = b.getString("userName")



        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.length > 0) {
                    adapater = PostAdpater(applicationContext, (filter(newText.toString())))

                }
                if (newText!!.isEmpty()){
                    adapater = PostAdpater( applicationContext, ListPosts)
                    LoadPost()

                }
                lvPosts.adapter = adapater
                adapater!!.notifyDataSetChanged()
                return true
            }
        })

        adapater = PostAdpater(this, ListPosts)


        lvPosts.layoutManager = LinearLayoutManager(this)
        lvPosts.hasFixedSize()
        lvPosts.adapter = adapater

        LoadPost()
        loadUserPrfPic()


    }

    inner class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(post: Post, posts: ArrayList<Post>) {

                                likesRef = myRef.child("users").child(UserUID).child("likes")
                                for (key in posts) {
                                    itemView!!.tvUserName.text= post!!.userName
                                    Picasso.with(applicationContext).load(post!!.imageUrl).into(itemView!!.post_image)
                                    Picasso.with(applicationContext).load(post!!.profileImgUrl).into(itemView!!.user_img)
                                    itemView!!.caption.text = post!!.description
                                    itemView!!.likes.text = "" + post!!.likes
                                    itemView!!.tv_tweet_date.text = post!!.postDate

                                    //updateView(itemView!!)

                                }


            itemView!!.setOnClickListener { contactGiver(itemView) }
            itemView!!.loveImg.setOnClickListener{ likeTapped(itemView)}

        }

    }

    //            if(post!!.userName.equals("loading")){
//                myView = layoutInflater.inflate(R.layout.loading_ticket,null)
//                return myView!!
//            }else{


    inner class PostAdpater(var c: Context, var listPosts: ArrayList<Post>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var myView: View? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

            (holder as Item).bindData(listPosts[position], listPosts)
            holder.itemView.setTag(position)

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
            myView = LayoutInflater.from(c).inflate(R.layout.posts_ticket, parent, false)

            return Item(myView!!)
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getItemCount(): Int {
        return listPosts.size
        }
    }

    private fun filter(text: String): ArrayList<Post>{
        val filterdPost = ArrayList<Post>()

        for (s in ListPosts) {
            if (s.description!!.toLowerCase().contains(text.toLowerCase())) {
                filterdPost.add(s)
            }
        }
        ListPosts.clear()
        ListPosts.addAll(filterdPost)

        return ListPosts
    }

    private fun updateView(vi: View) {
        val v  = vi.getTag() as Int ?: return

        likesRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                var x: HashMap<String, Any>?
                x = dataSnapshot?.value as HashMap<String, Any>?
                if (v != null) {
                    if (x?.get(post?.postKey) != null) {
                        temp = true
                    } else {
                        temp = false
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })

        //Toast.makeText(applicationContext, temp.toString(), Toast.LENGTH_LONG).show()

        if(temp == true){
            vi.loveImg.setImageResource(R.drawable.filled_heart)
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
        val imagePath= SplitString(myEmail!!) + "."+ df.format(dataobj)+ ".jpg"
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
            this.imageSelected = false
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


        val sfd = SimpleDateFormat("yyyy-MM-dd");
        val dataobj= Date()

        //val post =  Post(etItemDesc.text.toString(), this!!.postDownloadURL!!, 0 as Any, this!!.userDownloadURL!!, this.userName!! as String, sfd.format(dataobj))

        val post : HashMap<String, Any> = HashMap<String, Any>()
            post.put("description", etItemDesc.text.toString() as Any)
            post.put("imageUrl", this!!.postDownloadURL!! as Any)
            post.put("likes", 0 as Any)
            post.put("profileImgUrl", this!!.userDownloadURL!! as Any)
            post.put("userName", this.userName!! as Any)
            post.put("postDate", sfd.format(dataobj) as Any)
            post.put("postUserObjectId", UserUID as Any)

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

                            adapater!!.notifyDataSetChanged()
                        }catch (ex:Exception){}


                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
    }


    fun signOuttapped(v: View) {
        val i = Intent(this, SignInActivity::class.java)
        FirebaseAuth.getInstance().signOut()
        startActivity(i)

    }
    fun goToProfile(v: View) {


        var intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("email", myEmail)
        intent.putExtra("uid", UserUID)
        intent.putExtra("userName", userName)

        startActivityForResult(intent, PRFNEW)


    }


    fun likeTapped(v: View) {
        if (check == false) {
            v.loveImg.setImageResource(R.drawable.filled_heart)
            itemPosition = v.getTag() as Int
            post = ListPosts[itemPosition]
            likesRef = myRef.child("users").child(UserUID).child("likes").child(post!!.postKey)
            post!!.adjustLikes(true)
            likesRef.setValue(true)
            check = true

        } else {
            v.loveImg.setImageResource(R.drawable.empty_heart)
            itemPosition = v.getTag() as Int
            post = ListPosts[itemPosition]
            likesRef = myRef.child("users").child(UserUID).child("likes").child(post!!.postKey)
            post!!.adjustLikes(false)
            likesRef.removeValue()
            check = false

        }
    }

    fun contactGiver(v:View){
        var intent = Intent(this, ContactGvActivity::class.java)

        itemPosition = v.getTag() as Int
        post = ListPosts[itemPosition]
        pkey = post!!.postKey
        intent.putExtra("email", myEmail)
        intent.putExtra("uid", UserUID)
        intent.putExtra("userName", userName)
        intent.putExtra("pkey", pkey)

        startActivityForResult(intent, CONTACT_GIVER_CODE )
    }



}