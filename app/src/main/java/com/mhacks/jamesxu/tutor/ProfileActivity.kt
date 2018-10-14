package com.mhacks.jamesxu.tutor

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mhacks.jamesxu.tutor.Objects.User
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class ProfileActivity : AppCompatActivity() {

    var currentUser: User? = StudTutorActivity.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        username_profile.setText(currentUser!!.username)
        major_profile.setText(currentUser!!.major)

        avgRating_profile.setText("Average Rating: ${currentUser!!.avgRating}/5.0")
        numRatings_profile.setText("Number of Ratings: ${currentUser!!.numRatings}")

        //Update photo
        photo_button_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            //Look at all images on phone
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        //Update user
        update_button_profile.setOnClickListener {
            updateUser()
        }
    }


    var photoUri: Uri = Uri.parse(currentUser!!.profileImageUrl)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")

            //Get the photo
            photoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            //Show the photo
            photo_button_profile.text = null
            val bitmapDrawable = BitmapDrawable(bitmap)
            photo_button_profile.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun updateUser() {
        val username = username_profile.text.toString()
        val major = major_profile.text.toString()

        if (username.isEmpty() || major.isEmpty()) {
            Toast.makeText(this, "Provide a username and major", Toast.LENGTH_SHORT).show()
            return
        }

        uploadPicToStorage()
        updateUserOnDatabase()

    }

    //Upload selected photo to Firebase using a random unqiue ID
    //Not scalable, old photo is not removed
    //Upload the selected profile pic to firebase storage
    private fun uploadPicToStorage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(photoUri)
                .addOnSuccessListener {
                    Log.d("ProfileActivity", "Successfully uploaded image: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("ProfileActivity", "File location: $it")
                    }
                }
                .addOnFailureListener {
                    Log.d("ProfileActivity", "Failed to upload image")
                }
    }

    //Save updated user
    private fun updateUserOnDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_profile.text.toString(), major_profile.text.toString(), photoUri.toString(), currentUser!!.avgRating, currentUser!!.numRatings)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("ProfileActivity", "Updated user in Firebase database")
                    //Go to StudTutor activity
                    val intent = Intent(this, StudTutorActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("ProfileActivity", "Failed to update user to Firebase database")
                }
    }

}