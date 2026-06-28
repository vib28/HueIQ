package com.hueiq.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
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
import com.hueiq.app.data.ColorLibraryData
import com.hueiq.app.data.CvdType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDetailScreen(
    r: Int,
    g: Int,
    b: Int,
    colorName: String,
    isSaved: Boolean,
    onSave: () -> Unit,
    onRemove: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedCvd by remember { mutableStateOf(CvdType.DEUTERANOPIA) }
    val hex = "#%02X%02X%02X".format(r, g, b)

    val (sr, sg, sb) = remember(selectedCvd) {
        ColorLibraryData.simulate(r, g, b, selectedCvd)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(colorName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = if (isSaved) onRemove else onSave) {
                        Icon(
                            imageVector = if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from library" else "Save to library",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Original color — full-width swatch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(r, g, b))
            )

            // Name + hex + copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(colorName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        hex,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("hex", hex))
                    Toast.makeText(context, "Copied $hex", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copy hex",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider()

            Text(
                "How colorblind users see this",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )

            // CVD type selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CvdType.entries.forEach { cvd ->
                    FilterChip(
                        selected = selectedCvd == cvd,
                        onClick = { selectedCvd = cvd },
                        label = { Text(cvd.label, fontSize = 13.sp) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Simulated color swatch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(sr, sg, sb))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    )
            )

            Spacer(Modifier.height(16.dp))

            // CVD info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(selectedCvd.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = when (selectedCvd) {
                            CvdType.DEUTERANOPIA ->
                                "Green receptor deficiency (~6% of males). Red and green hues appear similar, making them difficult to distinguish."
                            CvdType.PROTANOPIA ->
                                "Red receptor deficiency (~2% of males). Reds appear dark and muted; the red-green distinction is lost."
                            CvdType.TRITANOPIA ->
                                "Blue receptor deficiency (~0.01% of people). Blue and yellow hues appear similar and washed out."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
