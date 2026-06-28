package com.hueiq.app.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hueiq.app.data.SavedColor
import com.hueiq.app.ui.camera.ColorAnalyzer
import com.hueiq.app.ui.camera.DetectedColor
import com.hueiq.app.ui.camera.ScanColorViewModel
import java.util.concurrent.Executors

@Composable
fun ScanColorScreen(
    onBack: () -> Unit,
    savedColors: List<SavedColor> = emptyList(),
    onSaveColor: (r: Int, g: Int, b: Int, name: String) -> Unit = { _, _, _, _ -> },
    onViewDetails: (r: Int, g: Int, b: Int, name: String) -> Unit = { _, _, _, _ -> },
    viewModel: ScanColorViewModel = viewModel()
) {
    val context = LocalContext.current
    val detectedColor by viewModel.detectedColor.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            CameraPreviewLayer(onColorSampled = viewModel::onColorSampled)
            ReticleOverlay()
            ColorInfoPanel(
                detectedColor = detectedColor,
                savedColors = savedColors,
                onSaveColor = onSaveColor,
                onViewDetails = onViewDetails,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            PermissionRationale(
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        }

        // Back button always visible on top
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 40.dp, start = 8.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun CameraPreviewLayer(onColorSampled: (Int, Int, Int) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, ColorAnalyzer(onColorSampled))
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ReticleOverlay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val ringRadius = 55.dp.toPx()
            val dotRadius = 5.dp.toPx()
            val tickLength = 14.dp.toPx()
            val tickGap = 8.dp.toPx()
            val strokeWidth = 2.dp.toPx()
            val ringColor = Color.White.copy(alpha = 0.88f)

            // Outer ring
            drawCircle(
                color = ringColor,
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(width = strokeWidth)
            )

            // Center dot
            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = Offset(cx, cy)
            )

            // Tick marks (top / bottom / left / right)
            val outer = ringRadius + tickGap
            val inner = outer + tickLength

            // Top
            drawLine(ringColor, Offset(cx, cy - outer), Offset(cx, cy - inner), strokeWidth)
            // Bottom
            drawLine(ringColor, Offset(cx, cy + outer), Offset(cx, cy + inner), strokeWidth)
            // Left
            drawLine(ringColor, Offset(cx - outer, cy), Offset(cx - inner, cy), strokeWidth)
            // Right
            drawLine(ringColor, Offset(cx + outer, cy), Offset(cx + inner, cy), strokeWidth)
        }
    }
}

@Composable
private fun ColorInfoPanel(
    detectedColor: DetectedColor?,
    savedColors: List<SavedColor>,
    onSaveColor: (r: Int, g: Int, b: Int, name: String) -> Unit,
    onViewDetails: (r: Int, g: Int, b: Int, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.72f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        if (detectedColor == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Point camera at a color",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 15.sp
                )
            }
        } else {
            val swatchColor = Color(detectedColor.r, detectedColor.g, detectedColor.b)
            val isAlreadySaved = savedColors.any { it.hex == detectedColor.hex }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Top row: swatch + name/hex + copy icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(swatchColor)
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detectedColor.name,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = detectedColor.hex,
                            color = Color.White.copy(alpha = 0.70f),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("hex", detectedColor.hex))
                        Toast.makeText(context, "Copied ${detectedColor.hex}", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy hex code",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom row: Save + View Details buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isAlreadySaved) {
                                onSaveColor(detectedColor.r, detectedColor.g, detectedColor.b, detectedColor.name)
                                Toast.makeText(context, "Saved to My Colors", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isAlreadySaved,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.White.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = if (isAlreadySaved) "Saved" else "Save to Library",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { onViewDetails(detectedColor.r, detectedColor.g, detectedColor.b, detectedColor.name) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("View Details", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera Access Needed",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "HueIQ needs camera access to scan colors from real-world objects.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Camera Permission")
                }
            }
        }
    }
}
