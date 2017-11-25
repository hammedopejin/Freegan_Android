package com.planetpeopleplatform.freegan_android.Utility

import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


/**
 * Created by hammedopejin on 11/24/17.
 */

class MyDeleteDialogFragment : DialogFragment() {
    internal var mListener: OnCompleteListener? = null
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference
    lateinit var postRef: DatabaseReference

    interface OnCompleteListener {
        fun onComplete(postKey: String)
    }

    fun onAttach(activity: AppCompatActivity) {
        super.onAttach(activity)
        try {
            this.mListener = activity as OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnCompleteListener")
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = getArguments().getString("title")
        val postKey = getArguments().getString("postKey")


        val alertDialogBuilder = AlertDialog.Builder(getActivity())
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage("Are you sure you want to delete the post?")
        alertDialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            // on success

            postRef = myRef.child("posts").child(postKey)
            postRef.removeValue()
            mListener?.onComplete("deleted !");
        })
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

        return alertDialogBuilder.create()
    }

    companion object {

        fun newInstance(title: String, postKey: String): MyDeleteDialogFragment {
            val frag = MyDeleteDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("postKey", postKey)
            frag.setArguments(args)
            return frag
        }
    }


}// Empty constructor required for DialogFragment