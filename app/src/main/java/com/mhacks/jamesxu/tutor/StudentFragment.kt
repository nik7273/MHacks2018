package com.mhacks.jamesxu.tutor

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.mhacks.jamesxu.tutor.Objects.Request
import kotlinx.android.synthetic.main.fragment_student.*
import com.google.android.gms.tasks.Task
import java.util.jar.Manifest


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class StudentFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student, container, false)
    }


    fun addRequest() {
        val subjectText = subject_request.text.toString()
        val courseText = course_request.text.toString()
        val additional = additional_requests.text.toString()
        if (subjectText.isEmpty() || courseText.isEmpty()) {
            Toast.makeText(context, "Please enter a course", Toast.LENGTH_SHORT).show()
            return;
        }
        val uid = StudTutorActivity.currentUser?.uid
        Log.d("James", "" + StudTutorActivity.lat)
        Log.d("James", "" + StudTutorActivity.long)
        val request = Request(uid!!, subjectText, courseText, StudTutorActivity.currentUser!!.profileImageUrl, StudTutorActivity.currentUser!!.username, additional,  StudTutorActivity.currentUser!!.major, StudTutorActivity.lat, StudTutorActivity.long)
        val ref = FirebaseDatabase.getInstance().getReference("/requests/$uid")
        ref.setValue(request)
            .addOnSuccessListener {
                Log.d("James", "Save request")
                (activity as? StudTutorActivity)?.navigateToFragment(WaitingFragment())
                StudTutorActivity.waiting = true
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        request_button_request.setOnClickListener {
            addRequest()
        }
    }


}
