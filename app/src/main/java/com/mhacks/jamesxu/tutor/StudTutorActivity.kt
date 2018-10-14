package com.mhacks.jamesxu.tutor

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.renderscript.Sampler
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mhacks.jamesxu.tutor.Objects.Offer
import com.mhacks.jamesxu.tutor.Objects.User
import com.mhacks.jamesxu.tutor.RegisterAndLogin.RegisterActivity
import kotlinx.android.synthetic.main.activity_stud_tutor.*

class StudTutorActivity : AppCompatActivity() {

    private val studentFrag = StudentFragment()
    private val mapFrag = MapFragment()
    private val waitingFragment = WaitingFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    companion object {
        var lat = 0.0
        var long = 0.0
        var waiting: Boolean = false
        var currentUser: User? = null
    }
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportActionBar?.title = "Student Requests"
                navigateToFragment(mapFrag)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                supportActionBar?.title = "Request a Tutor"
                if (waiting)
                    navigateToFragment(waitingFragment)
                else
                    navigateToFragment(studentFrag)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stud_tutor)
        supportActionBar?.title = "Student Requests"
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, mapFrag).commit()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.d("James", "Not granted")
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
                lat = location.latitude
                long = location.longitude
            }
        }

        val ref = FirebaseDatabase.getInstance().getReference("accepted/${currentUser?.uid}")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val uid = p0.getValue(String::class.java)
                val intent = Intent(this@StudTutorActivity, ChatLogActivity::class.java)
                intent.putExtra("FriendUid", uid)
                startActivity(intent)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })


        verifyUserIsLoggedIn()
        fetchCurrentUser()
    }

    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    //If there is no uid, user is not logged in so go to register page
    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    //Get the current logged in user
    private fun fetchCurrentUser() {
        //Get user id from authentication
        val uid = FirebaseAuth.getInstance().uid
        //Find the id in the database to get all info, and set currentUser
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("StudyTutorActivity", "Current User: ${currentUser?.username}, ${currentUser?.uid}")
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }


}
