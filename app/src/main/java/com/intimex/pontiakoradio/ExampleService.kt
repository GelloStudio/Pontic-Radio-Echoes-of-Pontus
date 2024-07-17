package com.intimex.pontiakoradio

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import com.intimex.pontiakoradio.App.Companion.CHANNEL_ID
import java.util.ResourceBundle.clearCache

//Here let's create Foreground Service, which do it's long running work on background threads
class ExampleService : Service(){

    companion object {
        var KEEP_ALIVE: Int = 0
        val ACTION_STOP = "stop"
    }

    private val TAG: String = ExampleService::class.java.simpleName
    private lateinit var handlerThread : HandlerThread
    private lateinit var handler: Handler

    //Here my objective will be as soon, as people bind to this service, we will increment a KeepAlive counter, which was initially 0,
    // This will be checked after every 10 seconds, that if that counter reaches 0, then we will stop the foreground service
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        handlerThread = HandlerThread("ServiceBackgroundHandler")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    @SuppressLint("LaunchActivityFromNotification")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAppAndQuit()
            return START_NOT_STICKY
        }
        val strAppName: String
        val strLiveBroadcast: String
        strAppName = resources.getString(R.string.app_name);
        strLiveBroadcast = resources.getString(R.string.live_broadcast);

        //Setting up the foreground service notification
        val stopnotificationIntent = Intent(this, ExampleService::class.java)
        stopnotificationIntent.action = ACTION_STOP
        val pendingIntent: PendingIntent = PendingIntent.getService(this, 0, stopnotificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val openAppIntent = Intent(this, MainActivity::class.java)
        openAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        openAppIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK)

        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(this, 1, openAppIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification : Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(strLiveBroadcast)
            .setContentText(strAppName)
            .setSmallIcon(R.drawable.ic_play_white)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop Player", pendingIntent)
            .build()

        startForeground(1, notification)

        doSomeBackgroundWork()

        return START_NOT_STICKY
    }

    private fun doSomeBackgroundWork(){
        handler.postDelayed( Runnable {
            Log.d(TAG, "running")
            doSomeBackgroundWork()
        } , 10000)
    }

    //This is run, from the notification, in case user is not interested in running the app
    private fun stopAppAndQuit(){
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
        stopForeground(true)
        stopSelf()
        sendBroadcast(Intent("finishActivity"))
        this.makeTheAppCompleteClose();
    }
    fun makeTheAppCompleteClose() {
        callLoop()
        clearCache()
    }
    //We are overwriting this, as we want the foreground service to be killed as soon as the app is closed from the recent screen
    override fun onTaskRemoved(rootIntent: Intent?) {
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        handlerThread.quitSafely()
        super.onDestroy()
    }

    fun callLoop() {
        Process.killProcess(Process.myPid())
    }
}