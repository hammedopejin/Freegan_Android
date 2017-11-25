package com.planetpeopleplatform.freegan_android.Activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.planetpeopleplatform.freegan_android.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hammedopejin on 9/12/17.
 */
class ProfileActivity : AppCompatActivity(){
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    val PICK_IMAGE_CODE=123
    val MYPST = 234

    var userPicUrl:String? = ""
    var myemail:String? = ""
    var UserUID:String? = ""
    var userName:String? = ""
    var picturePath:String? = ""
    var DownloadURL:String?=""
    var imageSelected:Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var b: Bundle = intent.extras
        myemail = b.getString("email")
        UserUID = b.getString("uid")
        userName = b.getString("userName")



        loadUserPrfPic()

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
            Picasso.with(this).load(selectedImage).into(profileImg)
            imageSelected = true
        }

    }

    fun uploadImage(bitmap: Bitmap){
//        ListTickets.add(0, Ticket("0","him","url","loading"))


        val storage = FirebaseStorage.getInstance()
        val storgaRef=storage.getReferenceFromUrl("gs://freegan-42b40.appspot.com")
        val df= SimpleDateFormat("ddMMyyHHmmss")
        val dataobj= Date()
        val imagePath= SplitString(myemail!!) + "."+ df.format(dataobj)+ ".jpg"
        val ImageRef=storgaRef.child("user-images/"+imagePath )


        val baos= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data= baos.toByteArray()


        val toReplace = storage.getReferenceFromUrl(this!!.userPicUrl!!)
        toReplace.delete()

        val uploadTask=ImageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(applicationContext,"New pic successfully uploaded", Toast.LENGTH_LONG).show()
            DownloadURL= taskSnapshot.downloadUrl!!.toString()
//            ListTickets.removeAt(0)
            this.picturePath = ""
            this.imageSelected = false
            myRef.child("users").child(UserUID).child("userImgUrl").setValue(DownloadURL)
            loadUserPrfPic()

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

                    var td= dataSnapshot!!.value as HashMap<String, Any>

                    for(key in td.keys){
                        if (key.equals("userImgUrl")) {
                            userPicUrl = td[key] as String
                            Picasso.with(applicationContext).load(userPicUrl).into(prfIcon)
                            Picasso.with(applicationContext).load(userPicUrl).into(profileImg)

                        }
                        if(key.equals("userName")){
                            tfUserName.text = td[key] as String
                            eTUserName.hint = td[key] as String
                        }
                    }


                }catch (ex:Exception){}
            }
            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }

    fun prfImgBtn(view: View) {
        loadImage()
    }

    fun goToFeed(view: View) {
        var data = Intent()
        data.putExtra("userName", userName)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    fun updatePrfBtnTapped(view: View) {
        if (eTUserName.text.toString() != "") {
            this.userName = eTUserName.text.toString()
            myRef.child("users").child(UserUID).child("userName").setValue(this.userName)
            eTUserName.text.clear()
            eTUserName.hint = this.userName
        }

        if(imageSelected == true){

        val options = BitmapFactory.Options()
        options.inSampleSize = 2

            if(this!!.picturePath!!.equals("") == false) {
                uploadImage(BitmapFactory.decodeFile(picturePath, options))
            }
        }
    }

    fun goToMyPosts(view: View) {
        var intent = Intent(this, MyPostsActivity::class.java)
        intent.putExtra("email", myemail)
        intent.putExtra("uid", UserUID)
        intent.putExtra("userName", userName)

        startActivityForResult(intent, MYPST)
    }

}