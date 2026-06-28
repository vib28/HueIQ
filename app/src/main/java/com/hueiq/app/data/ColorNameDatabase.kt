package com.hueiq.app.data

import android.content.Context
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.abs

data class ColorMatch(val name: String, val hex: String)

object ColorNameDatabase {

    private data class NamedColor(val name: String, val r: Int, val g: Int, val b: Int)

    @Volatile
    private var labColors: List<Triple<DoubleArray, String, String>> = emptyList()

    // Called once from ScanColorViewModel.init{}. Idempotent.
    fun init(context: Context) {
        if (labColors.isNotEmpty()) return
        val json = context.assets.open("colornames.bestof.json")
            .bufferedReader().use { it.readText() }
        val arr = org.json.JSONArray(json)
        val parsed = ArrayList<NamedColor>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val hex = obj.getString("hex")   // "#RRGGBB"
            val name = obj.getString("name")
            val r = Integer.parseInt(hex.substring(1, 3), 16)
            val g = Integer.parseInt(hex.substring(3, 5), 16)
            val b = Integer.parseInt(hex.substring(5, 7), 16)
            parsed.add(NamedColor(name, r, g, b))
        }
        labColors = parsed.map { c ->
            Triple(rgbToLab(c.r, c.g, c.b), c.name, "#%02X%02X%02X".format(c.r, c.g, c.b))
        }
    }

    fun nearest(r: Int, g: Int, b: Int): ColorMatch {
        val lab = rgbToLab(r, g, b)
        var bestDist = Double.MAX_VALUE
        var bestName = "Unknown"
        var bestHex = "#%02X%02X%02X".format(r, g, b)

        for ((namedLab, name, namedHex) in labColors) {
            val dL = lab[0] - namedLab[0]
            val da = lab[1] - namedLab[1]
            val db = lab[2] - namedLab[2]
            val dist = dL * dL + da * da + db * db
            if (dist < bestDist) {
                bestDist = dist
                bestName = name
                bestHex = namedHex
            }
        }
        return ColorMatch(bestName, bestHex)
    }

    private fun rgbToLab(r: Int, g: Int, b: Int): DoubleArray {
        // sRGB → linear
        fun linearize(v: Double) =
            if (v > 0.04045) ((v + 0.055) / 1.055).pow(2.4) else v / 12.92

        val rLin = linearize(r / 255.0)
        val gLin = linearize(g / 255.0)
        val bLin = linearize(b / 255.0)

        // Linear RGB → XYZ (D65)
        val x = rLin * 0.4124564 + gLin * 0.3575761 + bLin * 0.1804375
        val y = rLin * 0.2126729 + gLin * 0.7151522 + bLin * 0.0721750
        val z = rLin * 0.0193339 + gLin * 0.1191920 + bLin * 0.9503041

        // XYZ → Lab (D65 white point)
        fun f(t: Double) = if (t > 0.008856) t.pow(1.0 / 3.0) else 7.787 * t + 16.0 / 116.0
        val fx = f(x / 0.95047)
        val fy = f(y / 1.00000)
        val fz = f(z / 1.08883)

        val L = 116.0 * fy - 16.0
        val a = 500.0 * (fx - fy)
        val labB = 200.0 * (fy - fz)
        return doubleArrayOf(L, a, labB)
    }
}
