package com.hueiq.app.data

import android.content.Context
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ColorCategory(val label: String) {
    ALL("All"),
    MY_COLORS("My Colors"),
    WHITES("Whites"),
    GRAYS("Grays"),
    REDS("Reds"),
    PINKS("Pinks"),
    ORANGES("Oranges"),
    YELLOWS("Yellows"),
    GREENS("Greens"),
    TEALS("Teals"),
    BLUES("Blues"),
    PURPLES("Purples"),
    BROWNS("Browns")
}

enum class CvdType(val label: String, val shortLabel: String) {
    DEUTERANOPIA("Deuteranopia", "D"),
    PROTANOPIA("Protanopia", "P"),
    TRITANOPIA("Tritanopia", "T")
}

data class ColorEntry(
    val name: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val hex: String,
    val category: ColorCategory
)

object ColorLibraryData {

    @Volatile private var _all: List<ColorEntry> = emptyList()
    val all: List<ColorEntry> get() = _all

    // Called once from AuthViewModel.init{}. Idempotent.
    fun init(context: Context) {
        if (_all.isNotEmpty()) return
        val json = context.assets.open("colornames.bestof.json")
            .bufferedReader().use { it.readText() }
        val arr = org.json.JSONArray(json)
        val result = ArrayList<ColorEntry>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val hex = obj.getString("hex")
            val name = obj.getString("name")
            val r = Integer.parseInt(hex.substring(1, 3), 16)
            val g = Integer.parseInt(hex.substring(3, 5), 16)
            val b = Integer.parseInt(hex.substring(5, 7), 16)
            result.add(ColorEntry(name, r, g, b, hex.uppercase(), assignCategory(r, g, b)))
        }
        _all = result
    }

    /** Simulates how the given RGB looks under a CVD type using standard matrices (sRGB space). */
    fun simulate(r: Int, g: Int, b: Int, cvd: CvdType): Triple<Int, Int, Int> {
        val rF = r / 255f
        val gF = g / 255f
        val bF = b / 255f

        val (rS, gS, bS) = when (cvd) {
            CvdType.PROTANOPIA -> Triple(
                0.567f * rF + 0.433f * gF,
                0.558f * rF + 0.442f * gF,
                0.242f * gF + 0.758f * bF
            )
            CvdType.DEUTERANOPIA -> Triple(
                0.625f * rF + 0.375f * gF,
                0.700f * rF + 0.300f * gF,
                0.300f * gF + 0.700f * bF
            )
            CvdType.TRITANOPIA -> Triple(
                0.950f * rF + 0.050f * gF,
                0.433f * gF + 0.567f * bF,
                0.475f * gF + 0.525f * bF
            )
        }
        return Triple(
            (rS * 255f).roundToInt().coerceIn(0, 255),
            (gS * 255f).roundToInt().coerceIn(0, 255),
            (bS * 255f).roundToInt().coerceIn(0, 255)
        )
    }

    private fun assignCategory(r: Int, g: Int, b: Int): ColorCategory {
        val rf = r / 255f; val gf = g / 255f; val bf = b / 255f
        val max = maxOf(rf, gf, bf); val min = minOf(rf, gf, bf); val delta = max - min
        val l = (max + min) / 2f
        val s = if (delta == 0f) 0f
                else delta / (1f - abs(2f * l - 1f))
        val h = if (delta == 0f) 0f else when (max) {
            rf -> 60f * (((gf - bf) / delta) % 6f)
            gf -> 60f * ((bf - rf) / delta + 2f)
            else -> 60f * ((rf - gf) / delta + 4f)
        }.let { if (it < 0f) it + 360f else it }

        return when {
            l >= 0.92f                                          -> ColorCategory.WHITES
            s < 0.12f || (l < 0.15f && s < 0.30f)             -> ColorCategory.GRAYS
            h < 45f && l in 0.10f..0.50f && s in 0.15f..0.70f -> ColorCategory.BROWNS
            h < 15f || h >= 345f                               -> ColorCategory.REDS
            h < 45f                                            -> ColorCategory.ORANGES
            h < 70f                                            -> ColorCategory.YELLOWS
            h < 165f                                           -> ColorCategory.GREENS
            h < 195f                                           -> ColorCategory.TEALS
            h < 260f                                           -> ColorCategory.BLUES
            h < 320f                                           -> ColorCategory.PURPLES
            else                                               -> ColorCategory.PINKS
        }
    }
}
