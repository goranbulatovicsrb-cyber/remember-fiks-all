package com.podsetnikkapp

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.podsetnikkapp.ui.navigation.AppNavigation
import com.podsetnikkapp.ui.theme.PodsetnikTheme
import com.podsetnikkapp.viewmodel.ReminderViewModel
import com.podsetnikkapp.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ReminderViewModel by viewModels {
        ReminderViewModel.Factory(application)
    }
    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModel.Factory(application)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestAllPermissions()

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            PodsetnikTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(viewModel = viewModel, themeViewModel = themeViewModel)
                }
            }
        }
    }

    private fun requestAllPermissions() {
        val perms = mutableListOf<String>()

        // Notifikacije (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Lokacija za Geofence
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Audio fajlovi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }

        if (perms.isNotEmpty()) {
            requestPermissionLauncher.launch(perms.toTypedArray())
        }

        // USE_FULL_SCREEN_INTENT - Android 14+ treba posebnu dozvolu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            if (!nm.canUseFullScreenIntent()) {
                // Otvori Settings da korisnik rucno dozvoli
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:$packageName")
                }
                try { startActivity(intent) } catch (e: Exception) {}
            }
        }

        // Baterija optimizacija - iskljuci za pouzdane alarme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(android.os.PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {}
            }
        }
    }
}
