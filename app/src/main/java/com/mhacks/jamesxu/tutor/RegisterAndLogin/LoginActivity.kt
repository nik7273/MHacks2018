package com.mhacks.jamesxu.tutor.RegisterAndLogin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mhacks.jamesxu.tutor.R
import com.mhacks.jamesxu.tutor.StudTutorActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title = "Login"
        new_account_textview.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        login_button_login.setOnClickListener {
            val intent = Intent(this, StudTutorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
