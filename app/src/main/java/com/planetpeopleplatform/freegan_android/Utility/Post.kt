package com.planetpeopleplatform.freegan_android.Utility

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by hammedopejin on 9/5/17.
 */

class Post{

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    var postKey:String?=null
    var description:String?=null
    var imageUrl:String?=null
    var userName:String?=null
    var postUserObjectId:String?=null
    var likes:Any? = 0
    var profileImgUrl:String? = null
    var postDate:String? = null

    lateinit var postRef: DatabaseReference

    constructor(postKey: String, postData: HashMap<String, Any>) {
        this.postKey = postKey

        val des = postData["description"] as? String
            this.description = des


        val imgUrl = postData["imageUrl"] as? String
            this.imageUrl = imgUrl


        val proImgUrl = postData["profileImgUrl"] as? String
            this.profileImgUrl = proImgUrl


        val usrNam = postData["userName"] as? String
            this.userName = usrNam

        val pstUserObId = postData["postUserObjectId"] as? String
        this.postUserObjectId = pstUserObId

        val lkes = postData["likes"] as? Int
            this.likes = lkes

        val pDate = postData["postDate"] as? String
        this.postDate = pDate

        postRef = myRef.child("posts").child(postKey)

    }

    constructor(postKey: String, description:String, imageUrl:String, likes:Int, profileImgUrl:String, userName:String, postDate:String){
        this.postKey = postKey
        this.description=description
        this.imageUrl=imageUrl
        this.likes=likes
        this.profileImgUrl=profileImgUrl
        this.userName=userName
        this.postDate=postDate

    }

    fun adjustLikes(addLike: Boolean) {
        if (addLike == true) {
            likes = likes!! as Long + 1
        } else {
            likes = likes!! as Long - 1
        }
        postRef.child("likes").setValue(likes)

    }

}