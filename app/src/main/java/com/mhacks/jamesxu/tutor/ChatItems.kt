package com.mhacks.jamesxu.tutor

import com.mhacks.jamesxu.tutor.Objects.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_row_left.view.*
import kotlinx.android.synthetic.main.chat_row_right.view.*

class ChatLeftItem(val user: User, val text: String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        //Show profile pic
        Picasso.get().load(user.profileImageUrl)
                .resize(50, 50)
                .centerCrop()
                .into(viewHolder.itemView.imageView_chat_row_left)

        //Show text
        viewHolder.itemView.textView_chat_row_left.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_left
    }
}

class ChatRightItem(val user: User, val text: String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        //Show profile pic
        Picasso.get().load(user.profileImageUrl)
                .resize(50, 50)
                .centerCrop()
                .into(viewHolder.itemView.imageView_chat_row_right)

        //Show text
        viewHolder.itemView.textView_chat_row_right.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_row_right
    }
}