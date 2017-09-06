package com.planetpeopleplatform.freegan_android

/**
 * Created by hammedopejin on 9/5/17.
 */

class Post{


    var postKey:String?=null
    var description:String?=null
    var imageUrl:String?=null
    var userName:String?=null
    var likes:Int? = 0
    var profileImgUrl:String?=null

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


        val lkes = postData["likes"] as? Int
            this.likes = lkes

    }

    constructor(description:String, imageUrl:String, likes:Int, profileImgUrl:String, userName:String){
        this.postKey = postKey
        this.description=description
        this.imageUrl=imageUrl
        this.likes=likes
        this.profileImgUrl=profileImgUrl
        this.userName=userName

    }

}