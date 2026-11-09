/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * APPLICATION CLASS: CampusApp
 * Initializes the AppContainer and holds the application-level dependency graph.
 */
package com.example

import android.app.Application
import com.example.campusdirectory.di.AppContainer
import com.example.campusdirectory.di.DefaultAppContainer

class CampusApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
