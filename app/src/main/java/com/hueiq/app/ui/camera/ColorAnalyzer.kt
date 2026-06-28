package com.hueiq.app.ui.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.roundToInt

class ColorAnalyzer(private val onColorSampled: (r: Int, g: Int, b: Int) -> Unit) :
    ImageAnalysis.Analyzer {

    private var lastSampleMs = 0L
    private val throttleMs = 120L

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastSampleMs < throttleMs) {
            image.close()
            return
        }
        lastSampleMs = now

        val (r, g, b) = sampleCenter(image)
        image.close()
        onColorSampled(r, g, b)
    }

    // Samples a 21×21 region at the image center from YUV_420_888.
    // Y plane is full-res; U/V planes are half-res (one value per 2×2 block).
    private fun sampleCenter(image: ImageProxy): Triple<Int, Int, Int> {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        val width = image.width
        val height = image.height
        val cx = width / 2
        val cy = height / 2
        val half = 10 // sample 21×21

        var rSum = 0.0; var gSum = 0.0; var bSum = 0.0; var count = 0

        val yBytes = ByteArray(yBuffer.remaining()).also { yBuffer.get(it) }
        val uBytes = ByteArray(uBuffer.remaining()).also { uBuffer.get(it) }
        val vBytes = ByteArray(vBuffer.remaining()).also { vBuffer.get(it) }

        for (dy in -half..half) {
            for (dx in -half..half) {
                val px = (cx + dx).coerceIn(0, width - 1)
                val py = (cy + dy).coerceIn(0, height - 1)

                val yIdx = py * yRowStride + px
                if (yIdx >= yBytes.size) continue

                val uvRow = (py / 2) * uvRowStride
                val uvCol = (px / 2) * uvPixelStride
                val uIdx = uvRow + uvCol
                val vIdx = uvRow + uvCol

                if (uIdx >= uBytes.size || vIdx >= vBytes.size) continue

                val Y = (yBytes[yIdx].toInt() and 0xFF).toDouble()
                val U = ((uBytes[uIdx].toInt() and 0xFF) - 128).toDouble()
                val V = ((vBytes[vIdx].toInt() and 0xFF) - 128).toDouble()

                rSum += (Y + 1.402 * V).coerceIn(0.0, 255.0)
                gSum += (Y - 0.344136 * U - 0.714136 * V).coerceIn(0.0, 255.0)
                bSum += (Y + 1.772 * U).coerceIn(0.0, 255.0)
                count++
            }
        }

        if (count == 0) return Triple(128, 128, 128)
        return Triple(
            (rSum / count).roundToInt().coerceIn(0, 255),
            (gSum / count).roundToInt().coerceIn(0, 255),
            (bSum / count).roundToInt().coerceIn(0, 255)
        )
    }
}
