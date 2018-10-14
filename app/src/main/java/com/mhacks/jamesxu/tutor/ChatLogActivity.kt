package com.mhacks.jamesxu.tutor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.*
import com.mhacks.jamesxu.tutor.Objects.ChatMessage
import com.mhacks.jamesxu.tutor.Objects.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    var friend: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val currentUser = StudTutorActivity.currentUser
        val friend = intent.getParcelableExtra<User>("Friend")

        //Set title to friend's username
        supportActionBar?.title = friend.username

        recyclerview_chat_log.adapter = adapter

        listenForMessages(currentUser!!, friend)

        send_chat_log.setOnClickListener {
            Log.d("ChatLogActivity", "Send message")
            performSendMessage(currentUser, friend)
        }


    }


    private fun listenForMessages(currentUser: User, friend: User) {

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${currentUser.uid}/${friend.uid}")
        ref.addChildEventListener(object: ChildEventListener {

            //Look at messages under the correct sender and receiver directories
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    //right side
                    if (chatMessage.fromId == currentUser.uid) {
                        Log.d("ChatLogActivity", "${StudTutorActivity.currentUser!!.username}: ${chatMessage.text}")
                        adapter.add(ChatRightItem(currentUser, chatMessage.text))
                    }
                    //left side
                    if (chatMessage.fromId == friend.uid) {
                        Log.d("ChatLogActivity", "${friend.username}: chatMessage.text")
                        adapter.add(ChatLeftItem(friend, chatMessage.text))
                    }
                }

                //Scroll to bottom
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun performSendMessage(currentUser: User, friend: User) {

        val text = editText_chat_log.text.toString()

        //Place message in correct directories, push() creates a random id for the message data
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/${currentUser.uid}/${friend.uid}").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/${friend.uid}/${currentUser.uid}").push()

        val chatMessage = ChatMessage(reference.key!!, text, currentUser.uid, friend.uid, System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d("ChatLogActivity", "Saved our chat message to sender data: ${reference.key}")
                }
                .addOnFailureListener {
                    Log.d("ChatLogActivity", "Failed to save chat message (reference)")
                }

        toReference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d("ChatLogActivity", "Saved our chat message to receiver data: ${toReference.key}")

                    //Delete the text from the editText
                    editText_chat_log.text.clear()
                    //Scroll to bottom
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                }
                .addOnFailureListener {
                    Log.d("ChatLogActivity", "Failed to save chat message (toReference)")
                }

    }
}
