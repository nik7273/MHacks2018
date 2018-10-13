package com.mhacks.jamesxu.tutor.RegisterAndLogin

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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mhacks.jamesxu.tutor.Objects.User
import com.mhacks.jamesxu.tutor.R
import com.mhacks.jamesxu.tutor.StudTutorActivity
import kotlinx.android.synthetic.main.activity_register.*
import java.io.Serializable
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.title = "Register"
        have_account_text.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        //Set photo
        photo_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            //Look at all images on phone
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        //Register User
        register_button_register.setOnClickListener {
            registerUser();

            //Go to StudTutor activity

        }

        //Go to login activity
        have_account_text.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    //Set user's profile pic
    var photoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")

            //Get the photo
            photoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            //Show the photo
            photo_button_register.text = null
            val bitmapDrawable = BitmapDrawable(bitmap)
            photo_button_register.setBackgroundDrawable(bitmapDrawable)

        }
    }

    //Register the user, called when register button is clicked
    private fun registerUser() {
        val username = username_register.text.toString()
        val email = email_register.text.toString()
        val password = password_register.text.toString()

        Log.d("RegisterActivity", email)
        Log.d("RegisterActivity", password)

        if (photoUri == null || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Provide an profile pic, username, email and password", Toast.LENGTH_SHORT).show()
            return
        }

        //Authenticate user on Firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    //If successful
                    Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user!!.uid}")
                    uploadPicToStorage()
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
    }

    //Upload the selected profile pic to firebase storage
    private fun uploadPicToStorage() {

        if (photoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(photoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("RegisterActivity", "File location: $it")

                        saveUserToDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to upload image")
                }
    }

    //After saving the profile image as a Url, save a new instance of User to database
    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_register.text.toString(), major_register.text.toString(), profileImageUrl)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Saved user to Firebase database")
                    val intent = Intent(this, StudTutorActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to save user to Firebase database")
                }
    }
}
