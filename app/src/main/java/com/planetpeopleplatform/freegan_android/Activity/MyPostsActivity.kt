package com.planetpeopleplatform.freegan_android.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.planetpeopleplatform.freegan_android.R
import com.planetpeopleplatform.freegan_android.Utility.ItemTouchHelperAdapter
import com.planetpeopleplatform.freegan_android.Utility.MyDeleteDialogFragment
import com.planetpeopleplatform.freegan_android.Utility.MyItemTouchHelperCallback
import com.planetpeopleplatform.freegan_android.Utility.Post
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_my_posts.*
import kotlinx.android.synthetic.main.my_posts_ticket.view.*
import java.util.*




/**
 * Created by hammedopejin on 11/10/17.
 */


class MyPostsActivity : AppCompatActivity() {

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference


    lateinit var likesRef: DatabaseReference
    var post: Post? = null

    var ListPosts = ArrayList<Post>()
    var adapater: PostAdpater? = null
    var myEmail: String? = ""
    var UserUID: String? = null
    var pkey: String? = null
    var userName: String? = ""
    var itemPosition: Int = 1
    var temp = false



    val PRFNEW = 234
    val GO_TO_EDIT_POST_CODE = 777

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

        var b: Bundle = intent.extras
        myEmail = b.getString("email")
        UserUID = b.getString("uid")
        userName = b.getString("userName")

        adapater = PostAdpater(this, ListPosts)
        val callback = MyItemTouchHelperCallback(adapater!!)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(lvMyPosts)


        lvMyPosts.layoutManager = LinearLayoutManager(this)
        lvMyPosts.hasFixedSize()
        lvMyPosts.adapter = adapater


        LoadPost()


    }

        inner class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bindData(post: Post) {
             myRef.child("posts")
                    .addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot?) {

                            try {
                                var td = dataSnapshot!!.value as HashMap<String, Any>
                                likesRef = myRef.child("users").child(UserUID).child("likes")
                                for (key in td.keys) {
                                    Picasso.with(applicationContext).load(post!!.imageUrl).into(itemView!!.post_image2)
                                    itemView!!.caption2.text = post!!.description
                                    itemView!!.likes2.text = "" + post!!.likes
                                    itemView!!.tv_tweet_date2.text = post!!.postDate
                                }


                            } catch (ex: Exception) {
                            }


                        }

                        override fun onCancelled(p0: DatabaseError?) {

                        }
                    })
                itemView!!.setOnClickListener { goToEditPost(itemView) }
        }

    }


    inner class PostAdpater(var c: Context, var ListPosts: ArrayList<Post>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {
        var myView: View? = null

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            (holder as Item).bindData(ListPosts[position])
            holder.itemView.setTag(position)

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
            myView = LayoutInflater.from(c).inflate(R.layout.my_posts_ticket, parent, false)

            return Item(myView!!)
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getItemCount(): Int {

            return ListPosts.size
        }

        override fun onItemDismiss(position: Int) {
            deleteWarning(position)
            LoadPost()
            adapater!!.notifyDataSetChanged()
        }


    }

    fun LoadPost() {

        myRef.child("posts")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        try {

                            ListPosts.clear()


                            var td = dataSnapshot!!.value as HashMap<String, Any>

                            for (key in td.keys) {
                                var likes: Any = 0
                                var post = td[key] as HashMap<String, Any>

                                if (post["postUserObjectId"] == UserUID) {

                                    ListPosts.add(Post(key, post))
                                    for (keey in post) {
                                        if (keey.key == "likes") {
                                            likes = keey.value
                                            ListPosts.last().likes = likes
                                        }
                                    }

                                }


                            }

                            adapater!!.notifyDataSetChanged()
                        } catch (ex: Exception) {
                        }


                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
    }

    fun deleteWarning(position: Int) {

        var postKey = ListPosts[position].postKey

        val deleteDialog = MyDeleteDialogFragment.newInstance("ATTENTION !!!", postKey as String)
        var args =  Bundle()
        args.putString("title", "ATTENTION !!!")
        args.putString("postKey", postKey)
        deleteDialog.setArguments(args)
        deleteDialog.show(fragmentManager, "fragment_alert")

    }


    fun goToProfile(v: View) {


        var intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("email", myEmail)
        intent.putExtra("uid", UserUID)
        intent.putExtra("userName", userName)

        startActivityForResult(intent, PRFNEW)


    }

    fun goBackToProfile(v: View) {
        setResult(Activity.RESULT_OK)
        finish()
    }


    fun goToEditPost(v: View) {
        var intent = Intent(this, EditPostActivity::class.java)

        itemPosition = v.getTag() as Int
        post = ListPosts[itemPosition]
        pkey = post!!.postKey
        intent.putExtra("email", myEmail)
        intent.putExtra("uid", UserUID)
        intent.putExtra("userName", userName)
        intent.putExtra("pkey", pkey)

        startActivityForResult(intent, GO_TO_EDIT_POST_CODE)
    }


}