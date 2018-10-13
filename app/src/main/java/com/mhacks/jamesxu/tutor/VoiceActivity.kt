//package com.mhacks.jamesxu.tutor
//import android.Manifest
//import android.app.NotificationManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.DialogInterface
//import android.content.Intent
//import android.content.IntentFilter
//import android.content.pm.PackageManager
//import android.media.AudioAttributes
//import android.media.AudioFocusRequest
//import android.media.AudioManager
//import android.os.Build
//import android.os.Bundle
//import android.os.SystemClock
//import android.support.annotation.NonNull
//import android.support.design.widget.CoordinatorLayout
//import android.support.design.widget.FloatingActionButton
//import android.support.design.widget.Snackbar
//import android.support.v4.app.ActivityCompat
//import android.support.v4.content.ContextCompat
//import android.support.v4.content.LocalBroadcastManager
//import android.support.v7.app.AlertDialog
//import android.support.v7.app.AppCompatActivity
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.Menu
//import android.view.MenuInflater
//import android.view.MenuItem
//import android.view.View
//import android.view.Window
//import android.view.WindowManager
//import android.widget.Chronometer
//import android.widget.EditText
//import com.google.firebase.iid.FirebaseInstanceId
//import com.koushikdutta.async.future.FutureCallback
//import com.koushikdutta.ion.Ion
//import com.twilio.voice.Call
//import com.twilio.voice.CallException
//import com.twilio.voice.CallInvite
//import com.twilio.voice.RegistrationException
//import com.twilio.voice.RegistrationListener
//import com.twilio.voice.Voice
//import java.util.HashMap
//class VoiceActivity:AppCompatActivity() {
//    lateinit private var accessToken:String
//    lateinit private var audioManager:AudioManager
//    private val savedAudioMode = AudioManager.MODE_INVALID
//    private val isReceiverRegistered = false
//    lateinit private var voiceBroadcastReceiver:VoiceBroadcastReceiver
//    // Empty HashMap, never populated for the Quickstart
//    internal var twiMLParams = HashMap<String, String>()
//    lateinit private var coordinatorLayout:CoordinatorLayout
//    lateinit private var callActionFab:FloatingActionButton
//    lateinit private var hangupActionFab:FloatingActionButton
//    lateinit private var muteActionFab:FloatingActionButton
//    lateinit private var chronometer:Chronometer
//    lateinit private var soundPoolManager:SoundPoolManager
//    lateinit private var notificationManager:NotificationManager
//    lateinit private var alertDialog:AlertDialog
//    lateinit private var activeCallInvite:CallInvite
//    lateinit private var activeCall:Call
//    private val activeCallNotificationId:Int = 0
//    internal var registrationListener = registrationListener()
//    internal var callListener = callListener()
//    override protected fun onCreate(savedInstanceState:Bundle) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_voice)
//        // These flags ensure that the activity can be launched when the screen is locked.
//        val window = getWindow()
//        window.addFlags((WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON))
//        coordinatorLayout = findViewById(R.id.coordinator_layout)
//        callActionFab = findViewById(R.id.call_action_fab)
//        hangupActionFab = findViewById(R.id.hangup_action_fab)
//        muteActionFab = findViewById(R.id.mute_action_fab)
//        chronometer = findViewById(R.id.chronometer)
//        callActionFab.setOnClickListener(callActionFabClickListener())
//        hangupActionFab.setOnClickListener(hangupActionFabClickListener())
//        muteActionFab.setOnClickListener(muteActionFabClickListener())
//        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        soundPoolManager = SoundPoolManager.getInstance(this)
//        /*
//     * Setup the broadcast receiver to be notified of FCM Token updates
//     * or incoming call invite in this Activity.
//     */
//        voiceBroadcastReceiver = VoiceBroadcastReceiver()
//        registerReceiver()
//        /*
//     * Needed for setting/abandoning audio focus during a call
//     */
//        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.setSpeakerphoneOn(true)
//        /*
//     * Enable changing the volume using the up/down keys during a conversation
//     */
//        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL)
//        /*
//     * Setup the UI
//     */
//        resetUI()
//        /*
//     * Displays a call dialog if the intent contains a call invite
//     */
//        handleIncomingCallIntent(getIntent())
//        /*
//     * Ensure the microphone permission is enabled
//     */
//        if (!checkPermissionForMicrophone())
//        {
//            requestPermissionForMicrophone()
//        }
//        else
//        {
//            retrieveAccessToken()
//        }
//    }
//    override protected fun onNewIntent(intent:Intent) {
//        super.onNewIntent(intent)
//        handleIncomingCallIntent(intent)
//    }
//    private fun registrationListener():RegistrationListener {
//        return object:RegistrationListener() {
//            override fun onRegistered(accessToken:String, fcmToken:String) {
//                Log.d(TAG, "Successfully registered FCM " + fcmToken)
//            }
//            override fun onError(error:RegistrationException, accessToken:String, fcmToken:String) {
//                val message = String.format("Registration Error: %d, %s", error.getErrorCode(), error.getMessage())
//                Log.e(TAG, message)
//                Snackbar.make(coordinatorLayout, message, SNACKBAR_DURATION).show()
//            }
//        }
//    }
//    private fun callListener():Call.Listener {
//        return object:Call.Listener() {
//            override fun onConnectFailure(call:Call, error:CallException) {
//                setAudioFocus(false)
//                Log.d(TAG, "Connect failure")
//                val message = String.format("Call Error: %d, %s", error.getErrorCode(), error.getMessage())
//                Log.e(TAG, message)
//                Snackbar.make(coordinatorLayout, message, SNACKBAR_DURATION).show()
//                resetUI()
//            }
//            override fun onConnected(call:Call) {
//                setAudioFocus(true)
//                Log.d(TAG, "Connected")
//                activeCall = call
//            }
//            override fun onDisconnected(call:Call, error:CallException) {
//                setAudioFocus(false)
//                Log.d(TAG, "Disconnected")
//                if (error != null)
//                {
//                    val message = String.format("Call Error: %d, %s", error.getErrorCode(), error.getMessage())
//                    Log.e(TAG, message)
//                    Snackbar.make(coordinatorLayout, message, SNACKBAR_DURATION).show()
//                }
//                resetUI()
//            }
//        }
//    }
//    /*
//   * The UI state when there is an active call
//   */
//    private fun setCallUI() {
//        callActionFab.hide()
//        hangupActionFab.show()
//        muteActionFab.show()
//        chronometer.setVisibility(View.VISIBLE)
//        chronometer.setBase(SystemClock.elapsedRealtime())
//        chronometer.start()
//    }
//    /*
//   * Reset UI elements
//   */
//    private fun resetUI() {
//        callActionFab.show()
//        muteActionFab.setImageDrawable(ContextCompat.getDrawable(this@VoiceActivity, R.drawable.ic_mic_white_24dp))
//        muteActionFab.hide()
//        hangupActionFab.hide()
//        chronometer.setVisibility(View.INVISIBLE)
//        chronometer.stop()
//    }
//    override protected fun onResume() {
//        super.onResume()
//        registerReceiver()
//    }
//    override protected fun onPause() {
//        super.onPause()
//        unregisterReceiver()
//    }
//    override fun onDestroy() {
//        soundPoolManager.release()
//        super.onDestroy()
//    }
//    private fun handleIncomingCallIntent(intent:Intent) {
//        if (intent != null && intent.getAction() != null)
//        {
//            if (intent.getAction().equals(ACTION_INCOMING_CALL))
//            {
//                activeCallInvite = intent.getParcelableExtra(INCOMING_CALL_INVITE)
//                if (activeCallInvite != null && (activeCallInvite.getState() === CallInvite.State.PENDING))
//                {
//                    soundPoolManager.playRinging()
//                    alertDialog = createIncomingCallDialog(this@VoiceActivity,
//                            activeCallInvite,
//                            answerCallClickListener(),
//                            cancelCallClickListener())
//                    alertDialog.show()
//                    activeCallNotificationId = intent.getIntExtra(INCOMING_CALL_NOTIFICATION_ID, 0)
//                }
//                else
//                {
//                    if (alertDialog != null && alertDialog.isShowing())
//                    {
//                        soundPoolManager.stopRinging()
//                        alertDialog.cancel()
//                    }
//                }
//            }
//            else if (intent.getAction().equals(ACTION_FCM_TOKEN))
//            {
//                retrieveAccessToken()
//            }
//        }
//    }
//    private fun registerReceiver() {
//        if (!isReceiverRegistered)
//        {
//            val intentFilter = IntentFilter()
//            intentFilter.addAction(ACTION_INCOMING_CALL)
//            intentFilter.addAction(ACTION_FCM_TOKEN)
//            LocalBroadcastManager.getInstance(this).registerReceiver(
//                    voiceBroadcastReceiver, intentFilter)
//            isReceiverRegistered = true
//        }
//    }
//    private fun unregisterReceiver() {
//        if (isReceiverRegistered)
//        {
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceBroadcastReceiver)
//            isReceiverRegistered = false
//        }
//    }
//    private inner class VoiceBroadcastReceiver:BroadcastReceiver() {
//        fun onReceive(context:Context, intent:Intent) {
//            val action = intent.getAction()
//            if (action == ACTION_INCOMING_CALL)
//            {
//                /*
//         * Handle the incoming call invite
//         */
//                handleIncomingCallIntent(intent)
//            }
//        }
//    }
//    private fun answerCallClickListener():DialogInterface.OnClickListener {
//        return object:DialogInterface.OnClickListener() {
//            fun onClick(dialog:DialogInterface, which:Int) {
//                soundPoolManager.stopRinging()
//                answer()
//                setCallUI()
//                alertDialog.dismiss()
//            }
//        }
//    }
//    private fun callClickListener():DialogInterface.OnClickListener {
//        return object:DialogInterface.OnClickListener() {
//            fun onClick(dialog:DialogInterface, which:Int) {
//                // Place a call
//                val contact = (dialog as AlertDialog).findViewById(R.id.contact) as EditText
//                twiMLParams.put("to", contact.getText().toString())
//                activeCall = Voice.call(this@VoiceActivity, accessToken, twiMLParams, callListener)
//                setCallUI()
//                alertDialog.dismiss()
//            }
//        }
//    }
//    private fun cancelCallClickListener():DialogInterface.OnClickListener {
//        return object:DialogInterface.OnClickListener() {
//            fun onClick(dialogInterface:DialogInterface, i:Int) {
//                soundPoolManager.stopRinging()
//                if (activeCallInvite != null)
//                {
//                    activeCallInvite.reject(this@VoiceActivity)
//                    notificationManager.cancel(activeCallNotificationId)
//                }
//                alertDialog.dismiss()
//            }
//        }
//    }
//    /*
//   * Register your FCM token with Twilio to receive incoming call invites
//   *
//   * If a valid google-services.json has not been provided or the FirebaseInstanceId has not been
//   * initialized the fcmToken will be null.
//   *
//   * In the case where the FirebaseInstanceId has not yet been initialized the
//   * VoiceFirebaseInstanceIDService.onTokenRefresh should result in a LocalBroadcast to this
//   * activity which will attempt registerForCallInvites again.
//   *
//   */
//    private fun registerForCallInvites() {
//        val fcmToken = FirebaseInstanceId.getInstance().getToken()
//        if (fcmToken != null)
//        {
//            Log.i(TAG, "Registering with FCM")
//            Voice.register(this, accessToken, Voice.RegistrationChannel.FCM, fcmToken, registrationListener)
//        }
//    }
//    private fun callActionFabClickListener():View.OnClickListener {
//        return object:View.OnClickListener() {
//            fun onClick(v:View) {
//                alertDialog = createCallDialog(callClickListener(), cancelCallClickListener(), this@VoiceActivity)
//                alertDialog.show()
//            }
//        }
//    }
//    private fun hangupActionFabClickListener():View.OnClickListener {
//        return object:View.OnClickListener() {
//            fun onClick(v:View) {
//                soundPoolManager.playDisconnect()
//                resetUI()
//                disconnect()
//            }
//        }
//    }
//    private fun muteActionFabClickListener():View.OnClickListener {
//        return object:View.OnClickListener() {
//            fun onClick(v:View) {
//                mute()
//            }
//        }
//    }
//    /*
//   * Accept an incoming Call
//   */
//    private fun answer() {
//        activeCallInvite.accept(this, callListener)
//        notificationManager.cancel(activeCallNotificationId)
//    }
//    /*
//   * Disconnect from Call
//   */
//    private fun disconnect() {
//        if (activeCall != null)
//        {
//            activeCall.disconnect()
//            activeCall = null
//        }
//    }
//    private fun mute() {
//        if (activeCall != null)
//        {
//            val mute = !activeCall.isMuted()
//            activeCall.mute(mute)
//            if (mute)
//            {
//                muteActionFab.setImageDrawable(ContextCompat.getDrawable(this@VoiceActivity, R.drawable.ic_mic_white_off_24dp))
//            }
//            else
//            {
//                muteActionFab.setImageDrawable(ContextCompat.getDrawable(this@VoiceActivity, R.drawable.ic_mic_white_24dp))
//            }
//        }
//    }
//    private fun setAudioFocus(setFocus:Boolean) {
//        if (audioManager != null)
//        {
//            if (setFocus)
//            {
//                savedAudioMode = audioManager.getMode()
//                // Request audio focus before making any device switch.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                {
//                    val playbackAttributes = AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                            .build()
//                    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                            .setAudioAttributes(playbackAttributes)
//                            .setAcceptsDelayedFocusGain(true)
//                            .setOnAudioFocusChangeListener(object:AudioManager.OnAudioFocusChangeListener() {
//                                fun onAudioFocusChange(i:Int) {}
//                            })
//                            .build()
//                    audioManager.requestAudioFocus(focusRequest)
//                }
//                else
//                {
//                    audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
//                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                }
//                /*
//         * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
//         * required to be in this mode when playout and/or recording starts for
//         * best possible VoIP performance. Some devices have difficulties with speaker mode
//         * if this is not set.
//         */
//                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
//            }
//            else
//            {
//                audioManager.setMode(savedAudioMode)
//                audioManager.abandonAudioFocus(null)
//            }
//        }
//    }
//    private fun checkPermissionForMicrophone():Boolean {
//        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//        return resultMic == PackageManager.PERMISSION_GRANTED
//    }
//    private fun requestPermissionForMicrophone() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
//        {
//            Snackbar.make(coordinatorLayout,
//                    "Microphone permissions needed. Please allow in your application settings.",
//                    SNACKBAR_DURATION).show()
//        }
//        else
//        {
//            ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf<String>(Manifest.permission.RECORD_AUDIO),
//                    MIC_PERMISSION_REQUEST_CODE)
//        }
//    }
//    fun onRequestPermissionsResult(requestCode:Int, @NonNull permissions:Array<String>, @NonNull grantResults:IntArray) {
//        /*
//     * Check if microphone permissions is granted
//     */
//        if (requestCode == MIC_PERMISSION_REQUEST_CODE && permissions.size > 0)
//        {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
//            {
//                Snackbar.make(coordinatorLayout,
//                        "Microphone permissions needed. Please allow in your application settings.",
//                        SNACKBAR_DURATION).show()
//            }
//            else
//            {
//                retrieveAccessToken()
//            }
//        }
//    }
//    fun onCreateOptionsMenu(menu:Menu):Boolean {
//        val inflater = getMenuInflater()
//        inflater.inflate(R.menu.menu, menu)
//        return true
//    }
//    fun onOptionsItemSelected(item:MenuItem):Boolean {
//        when (item.getItemId()) {
//            R.id.speaker_menu_item -> if (audioManager.isSpeakerphoneOn())
//            {
//                audioManager.setSpeakerphoneOn(false)
//                item.setIcon(R.drawable.ic_phonelink_ring_white_24dp)
//            }
//            else
//            {
//                audioManager.setSpeakerphoneOn(true)
//                item.setIcon(R.drawable.ic_volume_up_white_24dp)
//            }
//        }
//        return true
//    }
//    /*
//   * Get an access token from your Twilio access token server
//   */
//    private fun retrieveAccessToken() {
//        Ion.with(this).load(TWILIO_ACCESS_TOKEN_SERVER_URL + "?identity=" + identity).asString().setCallback(object:FutureCallback<String>() {
//            fun onCompleted(e:Exception, accessToken:String) {
//                if (e == null)
//                {
//                    Log.d(TAG, "Access token: " + accessToken)
//                    this@VoiceActivity.accessToken = accessToken
//                    registerForCallInvites()
//                }
//                else
//                {
//                    Snackbar.make(coordinatorLayout,
//                            "Error retrieving access token. Unable to make calls",
//                            Snackbar.LENGTH_LONG).show()
//                }
//            }
//        })
//    }
//    companion object {
//        private val TAG = "VoiceActivity"
//        private val identity = "alice"
//        /*
//     * You must provide the URL to the publicly accessible Twilio access token server route
//     *
//     * For example: https://myurl.io/accessToken
//     *
//     * If your token server is written in PHP, TWILIO_ACCESS_TOKEN_SERVER_URL needs .php extension at the end.
//     *
//     * For example : https://myurl.io/accessToken.php
//     */
//        private val TWILIO_ACCESS_TOKEN_SERVER_URL = "TWILIO_ACCESS_TOKEN_SERVER_URL"
//        private val MIC_PERMISSION_REQUEST_CODE = 1
//        private val SNACKBAR_DURATION = 4000
//        val INCOMING_CALL_INVITE = "INCOMING_CALL_INVITE"
//        val INCOMING_CALL_NOTIFICATION_ID = "INCOMING_CALL_NOTIFICATION_ID"
//        val ACTION_INCOMING_CALL = "ACTION_INCOMING_CALL"
//        val ACTION_FCM_TOKEN = "ACTION_FCM_TOKEN"
//        fun createIncomingCallDialog(
//                context:Context,
//                callInvite:CallInvite,
//                answerCallClickListener:DialogInterface.OnClickListener,
//                cancelClickListener:DialogInterface.OnClickListener):AlertDialog {
//            val alertDialogBuilder = AlertDialog.Builder(context)
//            alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp)
//            alertDialogBuilder.setTitle("Incoming Call")
//            alertDialogBuilder.setPositiveButton("Accept", answerCallClickListener)
//            alertDialogBuilder.setNegativeButton("Reject", cancelClickListener)
//            alertDialogBuilder.setMessage(callInvite.getFrom() + " is calling.")
//            return alertDialogBuilder.create()
//        }
//        fun createCallDialog(callClickListener:DialogInterface.OnClickListener,
//                             cancelClickListener:DialogInterface.OnClickListener,
//                             context:Context):AlertDialog {
//            val alertDialogBuilder = AlertDialog.Builder(context)
//            alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp)
//            alertDialogBuilder.setTitle("Call")
//            alertDialogBuilder.setPositiveButton("Call", callClickListener)
//            alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener)
//            alertDialogBuilder.setCancelable(false)
//            val li = LayoutInflater.from(context)
//            val dialogView = li.inflate(R.layout.dialog_call, null)
//            val contact = dialogView.findViewById(R.id.contact) as EditText
//            contact.setHint(R.string.callee)
//            alertDialogBuilder.setView(dialogView)
//            return alertDialogBuilder.create()
//        }
//    }
//}