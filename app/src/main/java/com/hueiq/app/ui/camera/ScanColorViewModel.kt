package com.hueiq.app.ui.camera

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hueiq.app.data.ColorNameDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DetectedColor(
    val r: Int,
    val g: Int,
    val b: Int,
    val hex: String,
    val name: String
)

class ScanColorViewModel(application: Application) : AndroidViewModel(application) {

    private val _detectedColor = MutableStateFlow<DetectedColor?>(null)
    val detectedColor = _detectedColor.asStateFlow()

    fun onColorSampled(r: Int, g: Int, b: Int) {
        val match = ColorNameDatabase.nearest(r, g, b)
        _detectedColor.value = DetectedColor(r, g, b, "#%02X%02X%02X".format(r, g, b), match.name)
    }
}
