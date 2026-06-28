package com.hueiq.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueiq.app.data.ColorCategory
import com.hueiq.app.data.ColorEntry
import com.hueiq.app.data.ColorLibraryData
import com.hueiq.app.data.CvdType
import com.hueiq.app.data.SavedColor

private fun SavedColor.toColorEntry() = ColorEntry(
    name = name, r = r, g = g, b = b, hex = hex,
    category = ColorCategory.MY_COLORS
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorLibraryScreen(
    onBack: () -> Unit,
    savedColors: List<SavedColor> = emptyList(),
    onColorClick: (r: Int, g: Int, b: Int, name: String) -> Unit = { _, _, _, _ -> }
) {
    var selectedCategory by remember { mutableStateOf(ColorCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }

    val savedEntries = remember(savedColors) { savedColors.map { it.toColorEntry() } }

    val filtered = remember(selectedCategory, searchQuery, savedColors) {
        val base = when (selectedCategory) {
            ColorCategory.MY_COLORS -> savedEntries
            ColorCategory.ALL -> ColorLibraryData.all + savedEntries
            else -> ColorLibraryData.all.filter { it.category == selectedCategory }
        }
        if (searchQuery.isBlank()) base
        else base.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    val totalCount = ColorLibraryData.all.size + savedColors.size

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Color Library", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            searchVisible = !searchVisible
                            if (!searchVisible) searchQuery = ""
                        }) {
                            Icon(
                                imageVector = if (searchVisible) Icons.Outlined.Close else Icons.Outlined.Search,
                                contentDescription = if (searchVisible) "Close search" else "Search"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                AnimatedVisibility(
                    visible = searchVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search colors…") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Category filter chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ColorCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, fontSize = 13.sp) }
                        )
                    }
                }

                HorizontalDivider()
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.hex + it.name }) { entry ->
                ColorCard(entry = entry, onClick = { onColorClick(entry.r, entry.g, entry.b, entry.name) })
            }

            item {
                Text(
                    text = "Showing ${filtered.size} of $totalCount colors",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ColorCard(entry: ColorEntry, onClick: () -> Unit) {
    val context = LocalContext.current
    val swatchColor = Color(entry.r, entry.g, entry.b)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main color swatch
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(swatchColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name
                Text(
                    text = entry.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))

                // Hex code — tappable to copy
                Text(
                    text = entry.hex,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("hex", entry.hex))
                        Toast.makeText(context, "Copied ${entry.hex}", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(Modifier.height(8.dp))

                // CVD simulation row
                CvdSimulationRow(r = entry.r, g = entry.g, b = entry.b, borderColor = borderColor)
            }
        }
    }
}

@Composable
private fun CvdSimulationRow(r: Int, g: Int, b: Int, borderColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CvdType.entries.forEach { cvd ->
            val (sr, sg, sb) = ColorLibraryData.simulate(r, g, b, cvd)
            CvdSwatch(
                color = Color(sr, sg, sb),
                label = cvd.shortLabel,
                fullLabel = cvd.label,
                borderColor = borderColor
            )
        }
    }
}

@Composable
private fun CvdSwatch(color: Color, label: String, fullLabel: String, borderColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
                .border(1.dp, borderColor, RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
