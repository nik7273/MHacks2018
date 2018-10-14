package com.mhacks.jamesxu.tutor.Objects

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


//Class to represent each user
@Parcelize
class User(val uid: String, val username: String, val major: String, val profileImageUrl: String, val avgRating: Double, val numRatings: Int): Parcelable {
    constructor(): this("", "", "", "", 5.0, 0)
}
