package com.mhacks.jamesxu.tutor.Objects

class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timeStamp: Long) {
    //empty constructor (won't be used)
    constructor(): this("", "", "", "", -1)
}
