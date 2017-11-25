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
import kotlinx.android.synthetic.main.activity_edit_post.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hammedopejin on 11/12/17.
 */


class EditPostActivity : AppCompatActivity(){

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    val PICK_IMAGE_CODE = 123

    var UserUID: String? = ""
    var pkey: String? = ""
    var imageSelected:Boolean? = false
    var picturePath:String? = ""
    var postPicUrl:String? = ""
    var myEmail:String? = ""
    var userName: String? = ""
    var postDownloadURL:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        var b: Bundle = intent.extras
        UserUID = b.getString("uid")
        pkey = b.getString("pkey")
        myEmail = b.getString("email")
        userName = b.getString("userName")

        myRef.child("posts")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        try {
                            var td = dataSnapshot!!.value as HashMap<String, Any>
                            for (key in td.keys) {
                                var post = td[key] as HashMap<String, Any>
                                if (post["postUserObjectId"] == UserUID) {
                                    if (pkey == key) {
                                        postPicUrl = post["imageUrl"] as String
                                        Picasso.with(applicationContext).load(postPicUrl).into(post_image)
                                        caption.text = post["description"] as String
                                    }
                            }
                            }
                        } catch (ex: Exception) {
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })


    }





    fun imgBtn2(view: View) {
        loadImage()
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
            Picasso.with(this).load(selectedImage).into(img_post2)
            imageSelected = true

        }

    }


    fun postBtnTapped2(view: View){
        var desc = etItemDesc2.text.toString()
        if(desc.equals("") == false){
            myRef.child("posts").child(pkey).child("description").setValue(desc)
            etItemDesc2.text.clear()
            etItemDesc2.hint = "Enter item description"
        }

        if(imageSelected != false){
            val options = BitmapFactory.Options()
            options.inSampleSize = 2

            if(this!!.picturePath!!.equals("") == false) {
                uploadImage(BitmapFactory.decodeFile(picturePath, options))
            }
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


        val toReplace = storage.getReferenceFromUrl(this!!.postPicUrl!!)
        toReplace.delete()

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


    fun postToFirebase() {


        myRef.child("posts").child(pkey).child("imageUrl").setValue(this!!.postDownloadURL!!)
        img_post2.setImageResource(R.drawable.add_image)
        imageSelected = false

    }

    fun SplitString(email:String):String{
        val split= email.split("@")
        return split[0]
    }


    fun goBackToMyPosts(v: View) {
        setResult(Activity.RESULT_OK)
        finish()
    }


}