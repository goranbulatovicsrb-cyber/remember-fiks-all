package com.podsetnikkapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.podsetnikkapp.data.*
import com.podsetnikkapp.utils.AiSuggestionHelper
import com.podsetnikkapp.utils.HolidayHelper
import com.podsetnikkapp.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    viewModel: ReminderViewModel,
    reminderId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEdit = reminderId > 0L

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    )}
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var ringType by remember { mutableStateOf(RingType.SOUND_AND_VIBRATE) }
    var ringtoneUri by remember { mutableStateOf("default") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedColor by remember { mutableStateOf(ReminderColor.PURPLE) }
    var showOnLockScreen by remember { mutableStateOf(true) }
    var isPinned by remember { mutableStateOf(false) }
    var snoozeDuration by remember { mutableStateOf(10) }
    var isActive by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf(Category.NONE) }
    var isFavorite by remember { mutableStateOf(false) }
    // Advanced alarm
    var isProgressiveVolume by remember { mutableStateOf(false) }
    var repeatUntilDismissed by remember { mutableStateOf(false) }
    var repeatIntervalSeconds by remember { mutableStateOf(30) }
    var selectedWeekDays by remember { mutableStateOf(setOf<Int>()) }
    var preAlertMinutes by remember { mutableStateOf(setOf<Int>()) }
    var titleError by remember { mutableStateOf(false) }

    // AI state
    var aiText by remember { mutableStateOf("") }
    var aiApiKey by remember { mutableStateOf("") }
    var aiLoading by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf("") }
    var showAiPanel by remember { mutableStateOf(false) }

    // Voice state
    var showVoiceHint by remember { mutableStateOf(false) }

    // Geofence state
    var geofenceEnabled by remember { mutableStateOf(false) }
    var geofenceLat by remember { mutableStateOf("") }
    var geofenceLng by remember { mutableStateOf("") }
    var geofenceRadius by remember { mutableStateOf("100") }
    var geofenceAddress by remember { mutableStateOf("") }
    var showGeofencePanel by remember { mutableStateOf(false) }

    // Holidays state
    var showHolidaysPanel by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("RS") }

    LaunchedEffect(reminderId) {
        if (isEdit) {
            scope.launch {
                viewModel.getReminderById(reminderId)?.let { r ->
                    title = r.title; description = r.description
                    selectedDateTime = r.dateTimeMillis; repeatType = r.repeatType
                    ringType = r.ringType; ringtoneUri = r.ringtoneUri; priority = r.priority
                    selectedColor = ReminderColor.entries.find { it.hex == r.colorHex } ?: ReminderColor.PURPLE
                    showOnLockScreen = r.showOnLockScreen; isPinned = r.isPinned
                    snoozeDuration = r.snoozeDurationMinutes; isActive = r.isActive
                    geofenceEnabled = r.geofenceEnabled
                    geofenceLat = if (r.geofenceLat != 0.0) r.geofenceLat.toString() else ""
                    geofenceLng = if (r.geofenceLng != 0.0) r.geofenceLng.toString() else ""
                    geofenceRadius = r.geofenceRadius.toInt().toString()
                    geofenceAddress = r.geofenceAddress
                    category = r.category
                    isFavorite = r.isFavorite
                    isProgressiveVolume = r.isProgressiveVolume
                    repeatUntilDismissed = r.repeatUntilDismissed
                    repeatIntervalSeconds = r.repeatIntervalSeconds
                    selectedWeekDays = r.weekDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                    preAlertMinutes = r.preAlertMinutes.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                }
            }
        }
    }

    val sdf = SimpleDateFormat("EEE, dd.MM.yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTime }

    val datePicker = DatePickerDialog(context,
        { _, year, month, day -> cal.set(year, month, day); selectedDateTime = cal.timeInMillis },
        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
    )
    val timePicker = TimePickerDialog(context,
        { _, hour, minute -> cal.set(Calendar.HOUR_OF_DAY, hour); cal.set(Calendar.MINUTE, minute); cal.set(Calendar.SECOND, 0); selectedDateTime = cal.timeInMillis },
        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
    )

    val ringtoneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.let { ringtoneUri = it.toString() }
    }

    // Media picker za MP3/WAV/OGG fajlove
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Trajni pristup fajlu
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { /* ignore if not persistable */ }
            ringtoneUri = it.toString()
        }
    }

    // Naziv izabranog media fajla
    val selectedFileName = remember(ringtoneUri) {
        when {
            ringtoneUri == "default" || ringtoneUri.isBlank() -> null
            ringtoneUri.startsWith("content://") -> {
                try {
                    context.contentResolver.query(
                        Uri.parse(ringtoneUri),
                        arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                        null, null, null
                    )?.use { c ->
                        if (c.moveToFirst()) c.getString(0) else "Vlastiti zvuk"
                    } ?: "Vlastiti zvuk"
                } catch (e: Exception) { "Vlastiti zvuk" }
            }
            else -> "Vlastiti zvuk"
        }
    }

    // Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { spoken ->
            if (title.isBlank()) title = spoken else description += " $spoken"
            showVoiceHint = true
        }
    }

    fun saveAndExit() {
        if (title.isBlank()) { titleError = true; return }
        val lat = geofenceLat.toDoubleOrNull() ?: 0.0
        val lng = geofenceLng.toDoubleOrNull() ?: 0.0
        val radius = geofenceRadius.toFloatOrNull() ?: 100f
        viewModel.saveReminder(Reminder(
            id = if (isEdit) reminderId else 0L, title = title.trim(),
            description = description.trim(), dateTimeMillis = selectedDateTime,
            repeatType = repeatType, ringType = ringType, ringtoneUri = ringtoneUri,
            priority = priority, colorHex = selectedColor.hex, isActive = isActive,
            showOnLockScreen = showOnLockScreen, isPinned = isPinned,
            snoozeDurationMinutes = snoozeDuration,
            geofenceEnabled = geofenceEnabled && lat != 0.0 && lng != 0.0,
            geofenceLat = lat, geofenceLng = lng,
            geofenceRadius = radius, geofenceAddress = geofenceAddress.trim()
        ))
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF0F0F1A), Color(0xFF1A1A2E), Color(0xFF0F0F1A)))
    )) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(if (isEdit) "Uredi podsetnik" else "Novi podsetnik", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                    actions = { TextButton(onClick = { saveAndExit() }) { Text("Sacuvaj", color = Color(0xFF9D4EDD), fontWeight = FontWeight.Bold, fontSize = 16.sp) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // === DATUM I VREME ===
                SectionCard {
                    SectionTitle("📅 Datum i vreme")

                    // Veliki vizuelni prikaz izabranog vremena
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0D0D1F))
                            .border(1.dp, Color(0xFF9D4EDD).copy(0.3f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            // Veliki sat prikaz
                            Text(
                                text = timeFormat.format(Date(selectedDateTime)),
                                color = Color(0xFF9D4EDD),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Thin,
                                letterSpacing = 4.sp
                            )
                            Text(
                                text = sdf.format(Date(selectedDateTime)),
                                color = Color.White.copy(0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Light
                            )
                            // Countdown
                            val now = System.currentTimeMillis()
                            val diff = selectedDateTime - now
                            if (diff > 0) {
                                val days = diff / (1000 * 60 * 60 * 24)
                                val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                                val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                                val countdown = when {
                                    days > 0 -> "za ${days}d ${hours}h ${minutes}min"
                                    hours > 0 -> "za ${hours}h ${minutes}min"
                                    else -> "za ${minutes}min"
                                }
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF9D4EDD).copy(0.15f))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(countdown, color = Color(0xFF9D4EDD), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            } else {
                                Spacer(Modifier.height(6.dp))
                                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFF6B6B).copy(0.15f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    Text("Datum je u prošlosti!", color = Color(0xFFFF6B6B), fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Dugmad za datum i vreme
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // DATUM dugme
                        Button(
                            onClick = { datePicker.show() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD).copy(0.2f)),
                            border = BorderStroke(1.dp, Color(0xFF9D4EDD).copy(0.6f))
                        ) {
                            Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF9D4EDD), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Datum", color = Color.White.copy(0.6f), fontSize = 10.sp)
                                Text(
                                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(selectedDateTime)),
                                    color = Color(0xFF9D4EDD), fontSize = 13.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // VREME dugme
                        Button(
                            onClick = { timePicker.show() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CC9F0).copy(0.2f)),
                            border = BorderStroke(1.dp, Color(0xFF4CC9F0).copy(0.6f))
                        ) {
                            Icon(Icons.Default.AccessTime, null, tint = Color(0xFF4CC9F0), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Vreme", color = Color.White.copy(0.6f), fontSize = 10.sp)
                                Text(
                                    timeFormat.format(Date(selectedDateTime)),
                                    color = Color(0xFF4CC9F0), fontSize = 18.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Brze prečice za vreme
                    Text("Brze prečice", color = Color.White.copy(0.5f), fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Za 15min" to 15L,
                            "Za 30min" to 30L,
                            "Za 1h" to 60L,
                            "Za 2h" to 120L,
                            "Sutra" to (24 * 60L),
                            "Za nedelju" to (7 * 24 * 60L)
                        ).forEach { (label, minutes) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF9D4EDD).copy(0.1f))
                                    .border(1.dp, Color(0xFF9D4EDD).copy(0.3f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val newTime = System.currentTimeMillis() + minutes * 60 * 1000L
                                        selectedDateTime = newTime
                                        cal.timeInMillis = newTime
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(label, color = Color(0xFF9D4EDD), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Ponavljanje", color = Color.White.copy(0.7f), fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RepeatType.entries.forEach { type ->
                            FilterChip(
                                selected = repeatType == type, onClick = { repeatType = type },
                                label = { Text(type.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF9D4EDD).copy(0.3f), selectedLabelColor = Color(0xFF9D4EDD))
                            )
                        }
                    }
                }




                // === ZVUK ===
                SectionCard {
                    SectionTitle("🔔 Zvuk i vibracija")

                    // Tip zvuka
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RingType.entries.forEach { type ->
                            FilterChip(
                                selected = ringType == type,
                                onClick = { ringType = type },
                                label = { Text(type.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4CC9F0).copy(0.2f),
                                    selectedLabelColor = Color(0xFF4CC9F0)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Trenutno izabrana melodija
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4CC9F0).copy(0.07f))
                            .border(1.dp, Color(0xFF4CC9F0).copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, null,
                                tint = Color(0xFF4CC9F0), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Izabrana melodija:",
                                    color = Color.White.copy(0.5f), fontSize = 11.sp
                                )
                                Text(
                                    when {
                                        ringtoneUri == "default" || ringtoneUri.isBlank() -> "Podrazumevano zvono alarma"
                                        else -> selectedFileName ?: "Vlastiti zvuk"
                                    },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            if (ringtoneUri != "default" && ringtoneUri.isNotBlank()) {
                                IconButton(
                                    onClick = { ringtoneUri = "default" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, null,
                                        tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Dugme 1 - Android zvona
                    Button(
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Izaberi melodiju alarma")
                            }
                            ringtoneLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CC9F0).copy(0.15f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF4CC9F0).copy(0.5f))
                    ) {
                        Icon(Icons.Default.Notifications, null,
                            tint = Color(0xFF4CC9F0), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Zvona telefona",
                                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Alarmi i tonovi zvona Androida",
                                color = Color.White.copy(0.5f), fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Dugme 2 - Vlastiti fajl
                    Button(
                        onClick = { mediaPickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9D4EDD).copy(0.15f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF9D4EDD).copy(0.5f))
                    ) {
                        Icon(Icons.Default.FolderOpen, null,
                            tint = Color(0xFF9D4EDD), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Moj fajl (MP3, WAV, OGG...)",
                                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Iz Muzike, Downloads, Memorije...",
                                color = Color.White.copy(0.5f), fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Snooze
                    Text("Odlaganje (snooze)", color = Color.White.copy(0.7f), fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20, 30).forEach { min ->
                            FilterChip(
                                selected = snoozeDuration == min,
                                onClick = { snoozeDuration = min },
                                label = { Text("${min}min", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF9800).copy(0.2f),
                                    selectedLabelColor = Color(0xFFFF9800)
                                )
                            )
                        }
                    }
                }

                


                // === AI PRIJEDLOZI ===
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showAiPanel = !showAiPanel },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("AI prijedlozi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Opisi sta zelis i AI predlozi datum/vreme", color = Color.White.copy(0.5f), fontSize = 12.sp)
                        }
                        Icon(if (showAiPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color(0xFF9D4EDD))
                    }
                    AnimatedVisibility(visible = showAiPanel) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = aiText,
                                onValueChange = { v: String -> aiText = v },
                                label = { Text("Npr: 'sutra u 15h zubara' ili 'za nedelju dana plata'") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                colors = fieldColors()
                            )
                            OutlinedTextField(
                                value = aiApiKey,
                                onValueChange = { v: String -> aiApiKey = v },
                                label = { Text("Claude API kljuc (anthropic.com)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = fieldColors()
                            )
                            Button(
                                onClick = {
                                    if (aiText.isBlank() || aiApiKey.isBlank()) return@Button
                                    aiLoading = true
                                    scope.launch {
                                        val result = AiSuggestionHelper.getSuggestion(aiText, aiApiKey)
                                        result.onSuccess { suggestion ->
                                            title = suggestion.suggestedTitle
                                            description = suggestion.suggestedDescription
                                            selectedDateTime = suggestion.suggestedDateTimeMillis
                                            cal.timeInMillis = selectedDateTime
                                            aiResult = "Prijedlog: ${suggestion.explanation}"
                                        }.onFailure {
                                            aiResult = "Greska: ${it.message}"
                                        }
                                        aiLoading = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                                enabled = !aiLoading
                            ) {
                                if (aiLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                else Icon(Icons.Default.AutoAwesome, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (aiLoading) "AI analizira..." else "Predlozi podsetnik")
                            }
                            if (aiResult.isNotBlank()) {
                                Text(aiResult, color = Color(0xFF4CC9F0), fontSize = 12.sp,
                                    modifier = Modifier.background(Color(0xFF4CC9F0).copy(0.1f), RoundedCornerShape(8.dp)).padding(8.dp))
                            }
                        }
                    }
                }

                // === GLASOVNI UNOS ===
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF9D4EDD).copy(0.08f))
                            .clickable {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "sr-RS")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Recite naziv podsetnika...")
                                }
                                try { voiceLauncher.launch(intent) } catch (e: Exception) { }
                            }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎤", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Glasovni unos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Klikni i govori naziv podsetnika", color = Color.White.copy(0.5f), fontSize = 12.sp)
                        }
                        Icon(Icons.Default.Mic, null, tint = Color(0xFF9D4EDD), modifier = Modifier.size(24.dp))
                    }
                    if (showVoiceHint) {
                        Text("Glas je dodat u naslov ili opis!", color = Color(0xFF4CAF50), fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }

                // === PRAZNICI ===
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showHolidaysPanel = !showHolidaysPanel },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎉", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Drzavni praznici", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Automatski dodaj podsetnik za praznik", color = Color.White.copy(0.5f), fontSize = 12.sp)
                        }
                        Icon(if (showHolidaysPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color(0xFF9D4EDD))
                    }
                    AnimatedVisibility(visible = showHolidaysPanel) {
                        Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Country selector
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("RS" to "Srbija", "BA" to "BiH", "HR" to "Hrvatska", "ORTHODOX" to "Pravoslavni", "SLAVA" to "Slave").forEach { (code, label) ->
                                    FilterChip(
                                        selected = selectedCountry == code,
                                        onClick = { selectedCountry = code },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF9D4EDD).copy(0.3f),
                                            selectedLabelColor = Color(0xFF9D4EDD)
                                        )
                                    )
                                }
                            }
                            val holidays = HolidayHelper.getAllHolidaysForYear(selectedCountry)
                            if (holidays.isEmpty()) {
                                Text("Nema predstojecih praznika", color = Color.White.copy(0.5f), fontSize = 13.sp)
                            } else {
                                holidays.forEach { (holiday, millis) ->
                                    val hSdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF9D4EDD).copy(0.08f))
                                            .clickable {
                                                title = holiday.name
                                                description = "${holiday.type}: ${holiday.name}"
                                                selectedDateTime = millis
                                                cal.timeInMillis = millis
                                                showHolidaysPanel = false
                                            }.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(holiday.name, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                        Text(hSdf.format(Date(millis)), color = Color(0xFF9D4EDD), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }


                // === KATEGORIJA + FAVORIT ===
                SectionCard {
                    SectionTitle("Kategorija i organizacija")
                    Text("Kategorija", color = Color.White.copy(0.7f), fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Category.entries.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text("${cat.emoji} ${cat.label}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF9D4EDD).copy(0.3f),
                                    selectedLabelColor = Color(0xFF9D4EDD))
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    ToggleRow("⭐", "Oznaci kao omiljeno", "Prikazuje se u tabeli Omiljeni", isFavorite, { isFavorite = it }, Color(0xFFFFD700))
                }

                // === OSNOVNO ===
                SectionCard {
                    SectionTitle("Osnovno")
                    OutlinedTextField(
                        value = title, onValueChange = { v: String -> title = v; titleError = false },
                        label = { Text("Naslov podsetnika *") }, isError = titleError,
                        supportingText = if (titleError) {{ Text("Naslov je obavezan!") }} else null,
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = fieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description, onValueChange = { v: String -> description = v },
                        label = { Text("Opis / napomena") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), maxLines = 5, colors = fieldColors()
                    )
                }

                // === GEOFENCE ===
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showGeofencePanel = !showGeofencePanel },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Alarm po lokaciji (Geofence)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Alarm se oglasi kad dodjes na lokaciju", color = Color.White.copy(0.5f), fontSize = 12.sp)
                        }
                        if (geofenceEnabled) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                            Spacer(Modifier.width(8.dp))
                        }
                        Icon(if (showGeofencePanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color(0xFF9D4EDD))
                    }
                    AnimatedVisibility(visible = showGeofencePanel) {
                        Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ToggleRow("📍", "Aktiviraj alarm po lokaciji", "Kada dodjes u krug od X metara", geofenceEnabled, { geofenceEnabled = it }, Color(0xFF4CAF50))
                            AnimatedVisibility(visible = geofenceEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = geofenceAddress, onValueChange = { v: String -> geofenceAddress = v },
                                        label = { Text("Naziv lokacije (npr. Kuca, Posao)") },
                                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = fieldColors()
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = geofenceLat, onValueChange = { v: String -> geofenceLat = v },
                                            label = { Text("Latitude") }, modifier = Modifier.weight(1f),
                                            singleLine = true, colors = fieldColors()
                                        )
                                        OutlinedTextField(
                                            value = geofenceLng, onValueChange = { v: String -> geofenceLng = v },
                                            label = { Text("Longitude") }, modifier = Modifier.weight(1f),
                                            singleLine = true, colors = fieldColors()
                                        )
                                    }
                                    OutlinedTextField(
                                        value = geofenceRadius, onValueChange = { v: String -> geofenceRadius = v },
                                        label = { Text("Poluprecnik u metrima (npr. 100)") },
                                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = fieldColors()
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(0.08f)),
                                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(0.3f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Kako pronaci koordinate:", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("1. Otvori Google Maps", color = Color.White.copy(0.7f), fontSize = 11.sp)
                                            Text("2. Dugo pritisni na zeljenoj lokaciji", color = Color.White.copy(0.7f), fontSize = 11.sp)
                                            Text("3. Kopiraj koordinate koje se pojave", color = Color.White.copy(0.7f), fontSize = 11.sp)
                                            Text("   Primjer: 44.8178, 20.4570 (Beograd)", color = Color(0xFF4CC9F0), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                // === NAPREDNI ALARM ===
                SectionCard {
                    SectionTitle("Napredne opcije alarma")

                    // Progressive volume
                    ToggleRow("📈", "Progresivna glasnoca", "Zvuk pocinje tiho i pojacava se 30 sekundi", isProgressiveVolume, { isProgressiveVolume = it }, Color(0xFF4CC9F0))
                    Spacer(Modifier.height(8.dp))
                    // Repeat until dismissed
                    ToggleRow("🔁", "Ponavljaj dok ne odbaciš", "Alarm se ponavlja na svaki interval", repeatUntilDismissed, { repeatUntilDismissed = it }, Color(0xFFFF9800))

                    if (repeatUntilDismissed) {
                        Spacer(Modifier.height(10.dp))
                        Text("Interval ponavljanja", color = Color.White.copy(0.7f), fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(15 to "15s", 30 to "30s", 60 to "1min", 120 to "2min", 300 to "5min").forEach { (sec, label) ->
                                FilterChip(
                                    selected = repeatIntervalSeconds == sec,
                                    onClick = { repeatIntervalSeconds = sec },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF9800).copy(0.2f),
                                        selectedLabelColor = Color(0xFFFF9800))
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    // Pre-alerts
                    Text("Upozorenje unaprijed", color = Color.White.copy(0.7f), fontSize = 13.sp)
                    Text("Dobices obavjestenje toliko minuta PRIJE podsetnika", color = Color.White.copy(0.4f), fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5 to "5min", 10 to "10min", 15 to "15min", 30 to "30min", 60 to "1h", 120 to "2h", 1440 to "1 dan").forEach { (min, label) ->
                            FilterChip(
                                selected = min in preAlertMinutes,
                                onClick = {
                                    preAlertMinutes = if (min in preAlertMinutes)
                                        preAlertMinutes - min else preAlertMinutes + min
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF9D4EDD).copy(0.3f),
                                    selectedLabelColor = Color(0xFF9D4EDD))
                            )
                        }
                    }
                    if (preAlertMinutes.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Aktivna upozorenja: ${preAlertMinutes.sorted().joinToString(", ") { if (it >= 60) "${it/60}h" else "${it}min" }} prije",
                            color = Color(0xFF9D4EDD), fontSize = 11.sp
                        )
                    }
                }

                // === DANI U SEDMICI ===
                SectionCard {
                    SectionTitle("⏰ Dani u sedmici (budilnik)")

                    // VREME alarma - veliki prikaz
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0D0D1F))
                            .border(1.5.dp, Color(0xFF4CC9F0).copy(0.5f), RoundedCornerShape(20.dp))
                            .clickable { timePicker.show() }
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "VREME ALARMA",
                                color = Color(0xFF4CC9F0).copy(0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null,
                                    tint = Color(0xFF4CC9F0),
                                    modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = timeFormat.format(Date(selectedDateTime)),
                                    color = Color.White,
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 4.sp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF4CC9F0).copy(0.15f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Klikni da promijenis vreme",
                                    color = Color(0xFF4CC9F0),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Izaberi dane kada se alarm oglasava", color = Color.White.copy(0.5f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    val dayLabels = listOf(0 to "Ned", 1 to "Pon", 2 to "Uto", 3 to "Sri", 4 to "Cet", 5 to "Pet", 6 to "Sub")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dayLabels.forEach { (day, label) ->
                            val isSelected = day in selectedWeekDays
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF9D4EDD).copy(0.3f) else Color.White.copy(0.05f))
                                    .border(1.dp, if (isSelected) Color(0xFF9D4EDD) else Color.White.copy(0.15f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedWeekDays = if (isSelected) selectedWeekDays - day else selectedWeekDays + day
                                        if (selectedWeekDays.isNotEmpty()) repeatType = RepeatType.CUSTOM_DAYS
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label,
                                    color = if (isSelected) Color(0xFF9D4EDD) else Color.White.copy(0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    if (selectedWeekDays.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        val dayNames = mapOf(0 to "Ned", 1 to "Pon", 2 to "Uto", 3 to "Sri", 4 to "Cet", 5 to "Pet", 6 to "Sub")
                        Text(
                            "Aktivno: ${selectedWeekDays.sorted().mapNotNull { dayNames[it] }.joinToString(", ")}",
                            color = Color(0xFF9D4EDD), fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Quick presets
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { selectedWeekDays = setOf(1,2,3,4,5); repeatType = RepeatType.CUSTOM_DAYS },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CC9F0)),
                            border = BorderStroke(1.dp, Color(0xFF4CC9F0).copy(0.4f)),
                            modifier = Modifier.weight(1f)
                        ) { Text("💼 Pon-Pet", fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = { selectedWeekDays = setOf(0,6); repeatType = RepeatType.CUSTOM_DAYS },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9D4EDD)),
                            border = BorderStroke(1.dp, Color(0xFF9D4EDD).copy(0.4f)),
                            modifier = Modifier.weight(1f)
                        ) { Text("🏖️ Vikend", fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = { selectedWeekDays = setOf(0,1,2,3,4,5,6); repeatType = RepeatType.CUSTOM_DAYS },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50)),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(0.4f)),
                            modifier = Modifier.weight(1f)
                        ) { Text("📅 Svaki", fontSize = 12.sp) }
                    }
                }

                // === PRIORITET ===
                SectionCard {
                    SectionTitle("Prioritet")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Priority.entries.forEach { p ->
                            val pColor = Color(p.color)
                            FilterChip(
                                selected = priority == p, onClick = { priority = p },
                                modifier = Modifier.weight(1f),
                                label = { Text(p.label, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = pColor.copy(0.25f), selectedLabelColor = pColor, labelColor = Color.White.copy(0.6f))
                            )
                        }
                    }
                }

                // === BOJA ===
                SectionCard {
                    SectionTitle("Boja podsetnika")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReminderColor.entries.forEach { rc ->
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(rc.hex))
                                    .border(if (selectedColor == rc) 3.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { selectedColor = rc },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == rc) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }

                // === ZAKLJUCAN EKRAN ===
                SectionCard {
                    SectionTitle("Zakljucan ekran")
                    ToggleRow("📱", "Prikazi na zakljucanom ekranu", "Vidljivo bez otkljucavanja", showOnLockScreen, { showOnLockScreen = it }, Color(0xFF4CC9F0))
                    Spacer(Modifier.height(8.dp))
                    ToggleRow("📌", "Prikvaci na zakljucani ekran", "Stalno prikazano dok ne otpustis", isPinned, { isPinned = it }, Color(0xFF9D4EDD))
                    Spacer(Modifier.height(8.dp))
                    ToggleRow("🔔", "Aktiviran", "Alarm ce se oglasiti na vreme", isActive, { isActive = it }, Color(0xFF4CAF50))
                }

                Button(
                    onClick = { saveAndExit() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEdit) "Azuriraj podsetnik" else "Sacuvaj podsetnik", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) { Column(modifier = Modifier.padding(16.dp), content = content) }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
fun ToggleRow(icon: String, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(0.08f)).clickable { onCheckedChange(!checked) }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = Color.White.copy(0.5f), fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor,
                uncheckedThumbColor = Color.White.copy(0.5f), uncheckedTrackColor = Color.White.copy(0.15f)))
    }
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF9D4EDD), unfocusedBorderColor = Color.White.copy(0.2f),
    focusedLabelColor = Color(0xFF9D4EDD), unfocusedLabelColor = Color.White.copy(0.5f),
    cursorColor = Color(0xFF9D4EDD), focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(0.9f),
    errorBorderColor = Color(0xFFFF6B6B), errorLabelColor = Color(0xFFFF6B6B)
)
