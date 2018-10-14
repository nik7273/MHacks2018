package com.mhacks.jamesxu.tutor

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mhacks.jamesxu.tutor.Objects.Offer
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_waiting.*
import kotlinx.android.synthetic.main.offer_row.view.*


class WaitingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_waiting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancel_tutor.setOnClickListener {
            StudTutorActivity.waiting = false
            (activity as? StudTutorActivity)?.navigateToFragment(StudentFragment())
        }
        val adapter = GroupAdapter<ViewHolder>()
        offers_list.adapter = adapter

        val ref = FirebaseDatabase.getInstance().getReference("/offers/${StudTutorActivity.currentUser?.uid}")
        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                Log.d("James", p0.getValue(String::class.java))
                val offer = p0.getValue(Offer::class.java)
                offer?.let {
                    adapter.add(UserItem(it.name, it.major, it.price, it.profileImg))
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

}

class UserItem(val name: String, val major: String, val price: String, val profileImg: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.offer_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.about.text = "${name}, ${major}, ${price}"
        Picasso.get().load(profileImg).into(viewHolder.itemView.profile)
    }

}