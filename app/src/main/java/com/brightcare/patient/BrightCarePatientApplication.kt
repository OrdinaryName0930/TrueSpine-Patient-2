package com.brightcare.patient

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for BrightCare Patient app
 * Initializes Facebook SDK and other configurations
 */
@HiltAndroidApp
class BrightCarePatientApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}

