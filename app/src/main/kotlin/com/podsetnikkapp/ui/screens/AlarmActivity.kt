package com.podsetnikkapp.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.podsetnikkapp.PodsetnikApplication
import com.podsetnikkapp.ui.theme.PodsetnikTheme
import com.podsetnikkapp.utils.AlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === KLJUCNO: window flags za sve Android verzije ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Otpusti keyguard (zaključan ekran)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        }

        // Sakri navigation bar i status bar - pravi fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { ctrl ->
                ctrl.hide(WindowInsets.Type.systemBars())
                ctrl.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val title = intent.getStringExtra("reminder_title") ?: "Podsetnik"
        val desc = intent.getStringExtra("reminder_desc") ?: ""
        val snoozeMin = intent.getIntExtra("snooze_minutes", 10)

        setContent {
            PodsetnikTheme(darkTheme = true) {
                AlarmScreen(
                    title = title,
                    description = desc,
                    snoozeMinutes = snoozeMin,
                    onDismiss = {
                        stopAlarm()
                        finish()
                    },
                    onSnooze = {
                        stopAlarm()
                        val app = application as PodsetnikApplication
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            app.repository.getById(reminderId)?.let { reminder ->
                                AlarmScheduler.scheduleSnooze(this@AlarmActivity, reminder, snoozeMin)
                            }
                        }
                        finish()
                    }
                )
            }
        }
    }

    private fun stopAlarm() {
        val stopIntent = android.content.Intent(this, com.podsetnikkapp.service.AlarmService::class.java)
        stopIntent.action = "DISMISS"
        startService(stopIntent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun AlarmScreen(
    title: String,
    description: String,
    snoozeMinutes: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "a")

    val bellScale by inf.animateFloat(1f, 1.25f,
        infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bs")
    val bellRot by inf.animateFloat(-14f, 14f,
        infiniteRepeatable(tween(260, easing = LinearEasing), RepeatMode.Reverse), label = "br")
    val glow by inf.animateFloat(0.15f, 0.75f,
        infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "g")
    val ripple by inf.animateFloat(0.6f, 2.8f,
        infiniteRepeatable(tween(1800, easing = LinearOutSlowInEasing), RepeatMode.Restart), label = "r")
    val rippleA by inf.animateFloat(0.7f, 0f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "ra")
    val ripple2 by inf.animateFloat(0.6f, 2.8f,
        infiniteRepeatable(tween(1800, easing = LinearOutSlowInEasing, delayMillis = 600), RepeatMode.Restart), label = "r2")
    val ripple2A by inf.animateFloat(0.7f, 0f,
        infiniteRepeatable(tween(1800, easing = LinearEasing, delayMillis = 600), RepeatMode.Restart), label = "ra2")

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); currentTime = System.currentTimeMillis() } }

    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFmt = SimpleDateFormat("EEEE, dd. MMMM yyyy.", Locale("sr", "RS"))

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        // Gradient bg
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3D0B6B).copy(glow * 0.8f),
                    Color(0xFF120028).copy(0.9f),
                    Color(0xFF000000)
                ), radius = 1800f
            )
        ))

        // Ripple krugovi
        Box(modifier = Modifier.size(260.dp).align(Alignment.Center).offset(y = (-50).dp)) {
            Box(modifier = Modifier.fillMaxSize().scale(ripple).alpha(rippleA)
                .border(2.dp, Color(0xFF9D4EDD), CircleShape))
            Box(modifier = Modifier.fillMaxSize().scale(ripple2).alpha(ripple2A)
                .border(1.5.dp, Color(0xFF6B2FA0), CircleShape))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // SAT - veliki
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 32.dp)) {
                Text(
                    text = timeFmt.format(Date(currentTime)),
                    color = Color.White,
                    fontSize = 88.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 8.sp
                )
                Text(
                    text = dateFmt.format(Date(currentTime)).replaceFirstChar { it.uppercase() },
                    color = Color.White.copy(0.65f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light
                )
            }

            // ZVONCE
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                Box(modifier = Modifier.size(140.dp).clip(CircleShape).background(
                    Brush.radialGradient(listOf(Color(0xFF9D4EDD).copy(glow * 0.55f), Color.Transparent))
                ))
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(105.dp).scale(bellScale).rotate(bellRot)
                )
            }

            // KARTICA SA PODSETNICIMA
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Info kartica
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.06f)),
                    border = BorderStroke(1.dp, Color(0xFF9D4EDD).copy(0.55f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                                .background(Color(0xFF9D4EDD)))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "PODSETNIK",
                                color = Color(0xFF9D4EDD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                                .background(Color(0xFF9D4EDD)))
                        }
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                        if (description.isNotBlank()) {
                            Divider(color = Color.White.copy(0.08f), thickness = 1.dp)
                            Text(
                                text = description,
                                color = Color.White.copy(0.72f),
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // SNOOZE dugme
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF9D4EDD).copy(0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBB86FC))
                ) {
                    Icon(Icons.Default.Snooze, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Odlozi za $snoozeMinutes minuta",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                // ODBACI dugme
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(62.dp),
                    shape = RoundedCornerShape(31.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.AlarmOff, null, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Odbaci alarm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
