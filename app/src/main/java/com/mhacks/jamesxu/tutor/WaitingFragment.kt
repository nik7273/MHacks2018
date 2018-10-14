package com.mhacks.jamesxu.tutor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.mhacks.jamesxu.tutor.Objects.Offer
import com.mhacks.jamesxu.tutor.Objects.User
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

        adapter.setOnItemClickListener { item, view ->
            val uid = (item as UserItem).uid //friend
            val user = User(StudTutorActivity.currentUser?.uid!!,uid,"","",0.0,0)
            val ref = FirebaseDatabase.getInstance().getReference("/accepted/${uid}") //friend
            ref.setValue(user)

            val friendRef = FirebaseDatabase.getInstance().getReference("/users/$uid")
            friendRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val friend = p0.getValue(User::class.java)
                    val intent = Intent(context, ChatLogActivity::class.java)
                    intent.putExtra("Friend", friend)
                    startActivity(intent)
                }
                override fun onCancelled(p0: DatabaseError) {
                }
            })
        }

        val ref = FirebaseDatabase.getInstance().getReference("/offers/${StudTutorActivity.currentUser?.uid}")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val offer = p0.getValue(Offer::class.java)
                offer?.let {
                    adapter.add(UserItem(it.uid, it.name, it.major, it.price, it.profileImg))
                }
            }


        })
    }

}

class UserItem(val uid: String, val name: String, val major: String, val price: String, val profileImg: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.offer_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.about.text = "${name}, ${major}, ${price}"
        Picasso.get().load(profileImg).into(viewHolder.itemView.profile)
    }

}