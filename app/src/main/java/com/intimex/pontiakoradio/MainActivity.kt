package com.intimex.pontiakoradio

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.*
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.ResourceBundle.clearCache


class MainActivity : AppCompatActivity() {

    lateinit var adView: AdView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(R.layout.activity_main)

        // Webview
        val goldGameView = findViewById<WebView>(R.id.goldGameapp)

        goldGameView.webViewClient = WebViewClient()
        goldGameView.settings.javaScriptEnabled = true
        goldGameView.loadUrl(getString(R.string.website_url))
        // Webview

        // Checknetwork
        checkConnectivity()


        // Load an ad into the AdMob banner view.
        adView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        // Load an ad into the AdMob banner view.

        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }

        })
        MobileAds.initialize(this@MainActivity)


        // Set web view chrome client
        goldGameView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress_bar.progress = newProgress

                //display Webview
                if (newProgress > 80) {
                    goldGameView.visibility = View.VISIBLE
                }
                Log.d("Progress", newProgress.toString())
            }
        }


        //startService(serviceIntent)
        val intent = Intent(this, ExampleService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startForegroundService(this, intent)
        //for starting foreground Service


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(broadcastReceiver, IntentFilter("finishActivity"),Context.RECEIVER_EXPORTED)
        }else{

            registerReceiver(broadcastReceiver, IntentFilter("finishActivity"))
        }
    }

    //Setting up broadcast receiver for the service to call this activity to kill this
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceive(context: Context?, intent: Intent?) {
            finishAndRemoveTask()
        }
    }


    //This ensures that if the user presses back button, the app first stops the service, and then kill this activity and then remove the task from recent activity menu

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to close App?")
            .setPositiveButton(
                "MUSIC OFF"
            ) { dialog, which ->
                finish()
                val intent = Intent(this, ExampleService::class.java)
                intent.action = ExampleService.ACTION_STOP
                this.finish()
                this.makeTheAppCompleteClose();
                super.onBackPressed();
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    fun makeTheAppCompleteClose() {
        callLoop()
        clearCache()
    }

    //Here we will unregister the broadcast receiver
    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    fun callLoop() {
        Process.killProcess(Process.myPid())
    }

    private fun checkConnectivity() {
        val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetworkInfo

        if (null == activeNetwork) {
            goldGameapp.loadUrl("about:blank")
            val dialogBuilder = AlertDialog.Builder(this)
            Intent(this, MainActivity::class.java)
            // set message of alert dialog
            dialogBuilder.setMessage("Make sure that WI-FI or mobile data is turned on, then try again")
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Retry", DialogInterface.OnClickListener { dialog, id ->
                    recreate()
                })
                // negative button text and action
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                    finish()
                    clearCache()
                    Process.killProcess(Process.myPid())
                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("No Internet Connection")
            // show alert dialog
            alert.show()
        }
    }
}
