package com.planetpeopleplatform.freegan_android

/**
 * Created by hammedopejin on 9/5/17.
 */
class Ticket{
    var postKey:String?=null
    var caption:String?=null
    var imageUrl:String?=null
    var postPersonUID:String?=null
    constructor(postKey:String, caption:String, imageUrl:String, postPersonUID:String){
        this.postKey=postKey
        this.caption=caption
        this.imageUrl=imageUrl
        this.postPersonUID=postPersonUID
        }
}