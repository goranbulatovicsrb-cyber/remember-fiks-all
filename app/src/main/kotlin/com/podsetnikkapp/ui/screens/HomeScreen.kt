package com.podsetnikkapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.podsetnikkapp.data.*
import com.podsetnikkapp.viewmodel.ReminderViewModel
import com.podsetnikkapp.viewmodel.SortOrder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReminderViewModel,
    onAddNew: () -> Unit,
    onEdit: (Long) -> Unit,
    onSettings: () -> Unit
) {
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val activeReminders by viewModel.activeReminders.collectAsStateWithLifecycle()
    val pinnedReminders by viewModel.pinnedReminders.collectAsStateWithLifecycle()
    val favoriteReminders by viewModel.favoriteReminders.collectAsStateWithLifecycle()
    val archivedReminders by viewModel.archivedReminders.collectAsStateWithLifecycle()
    val displayList by viewModel.displayList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val groupByDay by viewModel.groupByDay.collectAsStateWithLifecycle()

    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf<Reminder?>(null) }
    var archiveDialog by remember { mutableStateOf<Reminder?>(null) }

    val tabs = listOf("Svi", "Aktivni", "Prikvaceni", "Omiljeni", "Arhiva")
    val tabList: List<Reminder> = when (selectedTab) {
        0 -> displayList
        1 -> activeReminders
        2 -> pinnedReminders
        3 -> favoriteReminders
        4 -> archivedReminders
        else -> displayList
    }

    val now = System.currentTimeMillis()
    val upcoming: List<Reminder> = allReminders.filter { r: Reminder -> r.isActive && r.dateTimeMillis > now }
    val overdue: List<Reminder> = allReminders.filter { r: Reminder -> r.isActive && r.dateTimeMillis <= now && r.repeatType == RepeatType.NONE }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF0F0F1A), Color(0xFF1A1A2E), Color(0xFF0F0F1A)))
    )) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0x229D4EDD), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.15f), radius = size.width * 0.4f),
                center = Offset(size.width * 0.1f, size.height * 0.15f), radius = size.width * 0.4f
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            if (showSearch) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { q: String -> viewModel.setSearchQuery(q) },
                                    placeholder = { Text("Pretrazi...", color = Color.White.copy(0.5f)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF9D4EDD),
                                        unfocusedBorderColor = Color.White.copy(0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            } else {
                                Column {
                                    Text("Podsetnici", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                    Text("${allReminders.size} ukupno • ${upcoming.size} nadolazecih",
                                        color = Color.White.copy(0.6f), fontSize = 12.sp)
                                }
                            }
                        },
                        actions = {
                            // Group by day toggle
                            IconButton(onClick = { viewModel.toggleGroupByDay() }) {
                                Icon(if (groupByDay) Icons.Default.ViewDay else Icons.Default.ViewList,
                                    null, tint = if (groupByDay) Color(0xFF9D4EDD) else Color.White)
                            }
                            // Sort menu
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.Sort, null, tint = Color.White)
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(Color(0xFF1A1A2E))
                                ) {
                                    SortOrder.entries.forEach { sort ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (sortOrder == sort) Icon(Icons.Default.Check, null,
                                                        tint = Color(0xFF9D4EDD), modifier = Modifier.size(16.dp))
                                                    else Spacer(Modifier.size(16.dp))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(sort.label, color = Color.White, fontSize = 14.sp)
                                                }
                                            },
                                            onClick = { viewModel.setSortOrder(sort); showSortMenu = false }
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = onSettings) {
                                Icon(Icons.Default.Settings, null, tint = Color.White)
                            }
                            IconButton(onClick = { showSearch = !showSearch; if (!showSearch) viewModel.setSearchQuery("") }) {
                                Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, null, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // Stats row
                    if (!showSearch) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatChip("Nadolazeci", upcoming.size, Color(0xFF4CC9F0), Modifier.weight(1f))
                            StatChip("Istekli", overdue.size, Color(0xFFFF6B6B), Modifier.weight(1f))
                            StatChip("Omiljeni", favoriteReminders.size, Color(0xFFFFD700), Modifier.weight(1f))
                            StatChip("Arhiva", archivedReminders.size, Color(0xFF9E9E9E), Modifier.weight(1f))
                        }
                    }

                    // Category filter chips
                    if (!showSearch && selectedTab == 0) {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { viewModel.setCategory(null) },
                                label = { Text("Sve", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF9D4EDD).copy(0.3f),
                                    selectedLabelColor = Color(0xFF9D4EDD))
                            )
                            Category.entries.filter { it != Category.NONE }.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { viewModel.setCategory(if (selectedCategory == cat) null else cat) },
                                    label = { Text("${cat.emoji} ${cat.label}", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF9D4EDD).copy(0.3f),
                                        selectedLabelColor = Color(0xFF9D4EDD))
                                )
                            }
                        }
                    }

                    // Tabs
                    ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent,
                        contentColor = Color.White, edgePadding = 8.dp,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color(0xFF9D4EDD))
                            }
                        }) {
                        tabs.forEachIndexed { index, title ->
                            Tab(selected = selectedTab == index, onClick = { viewModel.setSelectedTab(index) },
                                text = { Text(title,
                                    color = if (selectedTab == index) Color(0xFF9D4EDD) else Color.White.copy(0.6f),
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp) })
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onAddNew,
                    containerColor = Color(0xFF9D4EDD), contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Novi", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        ) { paddingValues ->
            if (tabList.isEmpty()) {
                EmptyState(Modifier.fillMaxSize().padding(paddingValues), selectedTab)
            } else if (groupByDay && selectedTab != 4) {
                GroupedByDayList(tabList, paddingValues, onEdit, deleteDialog, archiveDialog,
                    onDelete = { archiveDialog = null; deleteDialog = it },
                    onArchive = { deleteDialog = null; archiveDialog = it },
                    onToggleActive = { viewModel.toggleActive(it) },
                    onTogglePinned = { viewModel.togglePinned(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items = tabList) { reminder: Reminder ->
                        ReminderCard(
                            reminder = reminder,
                            isArchived = selectedTab == 4,
                            onEdit = { onEdit(reminder.id) },
                            onDelete = { deleteDialog = reminder },
                            onArchive = { archiveDialog = reminder },
                            onUnarchive = { viewModel.unarchiveReminder(reminder) },
                            onToggleActive = { viewModel.toggleActive(reminder) },
                            onTogglePinned = { viewModel.togglePinned(reminder) },
                            onToggleFavorite = { viewModel.toggleFavorite(reminder) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    deleteDialog?.let { reminder: Reminder ->
        AlertDialog(
            onDismissRequest = { deleteDialog = null },
            containerColor = Color(0xFF1A1A2E), titleContentColor = Color.White,
            textContentColor = Color.White.copy(0.8f),
            title = { Text("Obrisi podsetnik", fontWeight = FontWeight.Bold) },
            text = { Text("Trajno obrisati: ${reminder.title}?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteReminder(reminder); deleteDialog = null }) {
                Text("Obrisi", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { deleteDialog = null }) {
                Text("Otkazi", color = Color(0xFF9D4EDD)) } }
        )
    }

    archiveDialog?.let { reminder: Reminder ->
        AlertDialog(
            onDismissRequest = { archiveDialog = null },
            containerColor = Color(0xFF1A1A2E), titleContentColor = Color.White,
            textContentColor = Color.White.copy(0.8f),
            title = { Text("Arhiviraj podsetnik", fontWeight = FontWeight.Bold) },
            text = { Text("Premjestiti u arhivu: ${reminder.title}?") },
            confirmButton = { TextButton(onClick = { viewModel.archiveReminder(reminder); archiveDialog = null }) {
                Text("Arhiviraj", color = Color(0xFF9D4EDD), fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { archiveDialog = null }) {
                Text("Otkazi", color = Color.White.copy(0.5f)) } }
        )
    }
}

@Composable
fun GroupedByDayList(
    reminders: List<Reminder>, paddingValues: PaddingValues,
    onEdit: (Long) -> Unit, deleteDialog: Reminder?, archiveDialog: Reminder?,
    onDelete: (Reminder) -> Unit, onArchive: (Reminder) -> Unit,
    onToggleActive: (Reminder) -> Unit, onTogglePinned: (Reminder) -> Unit, onToggleFavorite: (Reminder) -> Unit
) {
    val now = System.currentTimeMillis()
    val todayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    val tomorrowStart = todayStart + 86400000L
    val weekEnd = todayStart + 7 * 86400000L

    val groups = linkedMapOf<String, MutableList<Reminder>>()
    val overdue = reminders.filter { it.dateTimeMillis < now && it.repeatType == RepeatType.NONE }
    val today = reminders.filter { it.dateTimeMillis in todayStart until tomorrowStart }
    val tomorrow = reminders.filter { it.dateTimeMillis in tomorrowStart until (tomorrowStart + 86400000L) }
    val thisWeek = reminders.filter { it.dateTimeMillis in (tomorrowStart + 86400000L) until weekEnd }
    val later = reminders.filter { it.dateTimeMillis >= weekEnd }

    if (overdue.isNotEmpty()) groups["⚠️ Isteklo"] = overdue.toMutableList()
    if (today.isNotEmpty()) groups["📅 Danas"] = today.toMutableList()
    if (tomorrow.isNotEmpty()) groups["🌅 Sutra"] = tomorrow.toMutableList()
    if (thisWeek.isNotEmpty()) groups["📆 Ova sedmica"] = thisWeek.toMutableList()
    if (later.isNotEmpty()) groups["🗓️ Kasnije"] = later.toMutableList()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        groups.forEach { (groupName, groupReminders) ->
            item {
                Text(groupName, color = Color(0xFF9D4EDD), fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
            items(items = groupReminders) { reminder: Reminder ->
                ReminderCard(
                    reminder = reminder, isArchived = false,
                    onEdit = { onEdit(reminder.id) },
                    onDelete = { onDelete(reminder) },
                    onArchive = { onArchive(reminder) },
                    onUnarchive = {},
                    onToggleActive = { onToggleActive(reminder) },
                    onTogglePinned = { onTogglePinned(reminder) },
                    onToggleFavorite = { onToggleFavorite(reminder) }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun StatChip(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(0.15f))
        .border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp)).padding(6.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(0.6f), fontSize = 9.sp)
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder, isArchived: Boolean,
    onEdit: () -> Unit, onDelete: () -> Unit, onArchive: () -> Unit, onUnarchive: () -> Unit,
    onToggleActive: () -> Unit, onTogglePinned: () -> Unit, onToggleFavorite: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = reminder.dateTimeMillis < now && reminder.repeatType == RepeatType.NONE
    val reminderColor = Color(reminder.colorHex)
    val sdf = SimpleDateFormat("EEE, dd.MM.yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().clickable { onEdit() }.animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(if (isArchived) 0xFF141414 else 0xFF1A1A2E)),
        border = BorderStroke(1.dp, reminderColor.copy(if (isArchived) 0.2f else 0.4f))) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.width(4.dp).height(64.dp)
                .background(reminderColor.copy(if (isArchived) 0.4f else 1f), RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category emoji
                    Text(reminder.category.emoji, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(reminder.priority.color)))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = reminder.title,
                        color = if (reminder.isActive && !isArchived) Color.White else Color.White.copy(0.5f),
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (!reminder.isActive || isArchived) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )
                    if (reminder.isFavorite) Text("⭐", fontSize = 14.sp)
                    if (reminder.isPinned) Icon(Icons.Default.PushPin, null, tint = Color(0xFF9D4EDD), modifier = Modifier.size(14.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Outlined.AccessTime, null,
                        tint = if (isOverdue && !isArchived) Color(0xFFFF6B6B) else reminderColor,
                        modifier = Modifier.size(12.dp))
                    Text(
                        text = "${sdf.format(Date(reminder.dateTimeMillis))} ${timeFormat.format(Date(reminder.dateTimeMillis))}",
                        color = if (isOverdue && !isArchived) Color(0xFFFF6B6B) else Color.White.copy(0.6f),
                        fontSize = 12.sp
                    )
                    if (isOverdue && !isArchived) {
                        Text("ISTEKLO", color = Color(0xFFFF6B6B), fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(Color(0x22FF6B6B), RoundedCornerShape(4.dp)).padding(horizontal = 3.dp, vertical = 1.dp))
                    }
                    if (isArchived) {
                        Text("ARHIVA", color = Color(0xFF9E9E9E), fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(Color(0x229E9E9E), RoundedCornerShape(4.dp)).padding(horizontal = 3.dp, vertical = 1.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                    MiniChip(reminder.priority.label, Color(reminder.priority.color))
                    MiniChip(reminder.category.label, reminderColor)
                    if (reminder.repeatType != RepeatType.NONE) MiniChip(reminder.repeatType.label, Color(0xFF4CC9F0))
                    if (reminder.geofenceEnabled) MiniChip("📍 Lokacija", Color(0xFF4CAF50))
                }

                if (expanded && reminder.description.isNotBlank()) {
                    Text(reminder.description, color = Color.White.copy(0.5f), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (isArchived) {
                        IconButton(onClick = onUnarchive, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Unarchive, null, tint = Color(0xFF4CC9F0), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B).copy(0.8f), modifier = Modifier.size(18.dp))
                        }
                    } else {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                            Icon(if (reminder.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                                null, tint = if (reminder.isFavorite) Color(0xFFFFD700) else Color.White.copy(0.3f),
                                modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onToggleActive, modifier = Modifier.size(36.dp)) {
                            Icon(if (reminder.isActive) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                null, tint = if (reminder.isActive) Color(0xFF4CC9F0) else Color.White.copy(0.3f),
                                modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onTogglePinned, modifier = Modifier.size(36.dp)) {
                            Icon(if (reminder.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                null, tint = if (reminder.isPinned) Color(0xFF9D4EDD) else Color.White.copy(0.3f),
                                modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Archive, null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B).copy(0.8f), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (reminder.description.isNotBlank()) {
                        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) {
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null, tint = Color.White.copy(0.4f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniChip(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(0.15f))
        .border(0.5.dp, color.copy(0.4f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, selectedTab: Int) {
    val msgs = listOf(
        "Nema podsetnika" to "Dodaj novi podsetnik klikom na dugme ispod",
        "Nema aktivnih" to "Svi podsetnici su iskljuceni",
        "Nema prikvacenih" to "Prikvaci podsetnik da se vidi na zakljucanom ekranu",
        "Nema omiljenih" to "Oznaci podsetnik zvjezdicom da bude omiljeni",
        "Arhiva je prazna" to "Arhivirani podsetnici ce se prikazati ovdje"
    )
    val (title, subtitle) = msgs.getOrElse(selectedTab) { msgs[0] }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(when(selectedTab) { 3 -> "⭐"; 4 -> "🗄️"; else -> "🔔" }, fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Color.White.copy(0.5f), fontSize = 14.sp)
    }
}
