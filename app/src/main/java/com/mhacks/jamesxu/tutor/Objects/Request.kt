package com.mhacks.jamesxu.tutor.Objects

class Request(val uid: String, val subject: String, val course: String, val profileImageUrl: String, val name: String, val additional: String, val major: String, val lat: Double, val long: Double) {
    constructor(): this("", "", "", "", "", "", "", 0.0, 0.0)
}