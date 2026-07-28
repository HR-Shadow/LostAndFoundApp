package eu.tutorials.lostfoundapp

import android.app.Application
// ❌ Puraana Firebase BuildConfig HATA DEIN:
// import com.google.firebase.BuildConfig

// ✅ Apna App BuildConfig IMPORT KAREIN:
import eu.tutorials.lostfoundapp.BuildConfig

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.initialize
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class LostFoundApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        if (BuildConfig.DEBUG) {
            // Ab Debug build me bilkul sahi Debug Provider chalega
            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {

            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}