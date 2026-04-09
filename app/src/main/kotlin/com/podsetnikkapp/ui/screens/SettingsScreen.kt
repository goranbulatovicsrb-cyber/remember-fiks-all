package com.podsetnikkapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.podsetnikkapp.ui.theme.AppThemeMode
import com.podsetnikkapp.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel, onBack: () -> Unit) {
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(bgColor, surfaceColor, bgColor))
    )) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Podesavanja", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = BorderStroke(1.dp, primaryColor.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                            Text("🎨", fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Tema aplikacije", color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Izaberi izgled aplikacije", color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 12.sp)
                            }
                        }

                        // Theme grid
                        val themes = AppThemeMode.entries.chunked(2)
                        themes.forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { mode ->
                                    ThemeCard(
                                        mode = mode,
                                        isSelected = themeMode == mode,
                                        onClick = { themeViewModel.setTheme(mode) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Current theme preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = BorderStroke(1.dp, primaryColor.copy(0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(themeMode.emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Aktivna tema: ${themeMode.label}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Promjena je trenutna", color = primaryColor, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // About section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = BorderStroke(1.dp, Color.White.copy(0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("O aplikaciji", color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        InfoRow("📱", "Verzija", "1.0.0")
                        InfoRow("🔔", "Alarmi", "Precizni alarmi sa zvukom i vibracijom")
                        InfoRow("📍", "Lokacija", "Geofence alarm po GPS lokaciji")
                        InfoRow("🎤", "Glas", "Glasovni unos podsetnika")
                        InfoRow("🤖", "AI", "Pametan predlog datuma i vremena")
                        InfoRow("🕍", "Praznici", "Pravoslavni, slave i drzavni praznici")
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCard(mode: AppThemeMode, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val themeColor = when (mode) {
        AppThemeMode.DARK -> Color(0xFF9D4EDD)
        AppThemeMode.LIGHT -> Color(0xFF6750A4)
        AppThemeMode.AMOLED -> Color(0xFF555555)
        AppThemeMode.DYNAMIC -> Color(0xFF00BCD4)
        AppThemeMode.BLUE -> Color(0xFF4CC9F0)
        AppThemeMode.GREEN -> Color(0xFF4CAF50)
        AppThemeMode.PINK -> Color(0xFFFF69B4)
        AppThemeMode.PURPLE -> Color(0xFF9D4EDD)
        AppThemeMode.ORANGE -> Color(0xFFFF9800)
        AppThemeMode.RED -> Color(0xFFE53935)
    }
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp))
            .background(themeColor.copy(if (isSelected) 0.3f else 0.1f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) themeColor else themeColor.copy(0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(mode.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Column {
                Text(mode.label, color = if (isSelected) themeColor else Color.White.copy(0.8f),
                    fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
            if (isSelected) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Check, null, tint = themeColor, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun InfoRow(emoji: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 13.sp, modifier = Modifier.width(80.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
    }
}
