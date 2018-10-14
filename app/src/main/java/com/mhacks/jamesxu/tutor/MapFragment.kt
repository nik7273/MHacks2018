package com.mhacks.jamesxu.tutor

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mhacks.jamesxu.tutor.Objects.Request


class MapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    private var requests = mutableMapOf<LatLng, Request>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        mapView?.getMapAsync(this)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view?.findViewById(R.id.map) as MapView
        mapView?.onCreate(savedInstanceState)
        mapView?.onResume()
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0

        // Add a marker in Sydney and move the camera
        val michigan = LatLng(42.2780, -83.7382)
        //mMap?.addMarker(MarkerOptions().position(michigan).title("University of Michigan"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(michigan))
        mMap?.setMinZoomPreference(16f)

        val ref = FirebaseDatabase.getInstance().getReference("/requests")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val request = p0.getValue(Request::class.java) as Request
                val lat = request.lat
                val long = request.long
                Log.d("James", ""+lat)
                Log.d("James", ""+long)
                val mark = LatLng(lat, long)
                mMap?.addMarker(MarkerOptions().position(mark).title("${request.subject} ${request.course}"))
                requests[LatLng(lat,long)] = request
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        mMap?.setOnInfoWindowClickListener {
            val request = requests[it.position]
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle("${request?.name} (${request?.major}) wants to be tutored in ${request?.subject} ${request?.course}")
            alertDialog.setMessage("${request?.additional}")
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_popup, getView() as ViewGroup, false)
            val input = view.findViewById<AutoCompleteTextView>(R.id.input)
            alertDialog.setView(view)
            alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Send Offer", DialogInterface.OnClickListener { dialogInterface, i ->
                Log.d("James", "Hello")
            })
            alertDialog.setButton(Dialog.BUTTON_NEUTRAL, "Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                alertDialog.cancel()
            })


            alertDialog.show()



        }


    }

}
