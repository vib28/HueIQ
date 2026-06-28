package com.hueiq.app.data

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.abs

data class ColorMatch(val name: String, val hex: String)

object ColorNameDatabase {

    private data class NamedColor(val name: String, val r: Int, val g: Int, val b: Int)

    // ~150 named colors — CSS + X11 palette, spread across the full spectrum
    private val colors = listOf(
        // Whites & near-whites
        NamedColor("White", 255, 255, 255),
        NamedColor("Snow", 255, 250, 250),
        NamedColor("Ivory", 255, 255, 240),
        NamedColor("Floral White", 255, 250, 240),
        NamedColor("Ghost White", 248, 248, 255),
        NamedColor("Seashell", 255, 245, 238),
        NamedColor("Linen", 250, 240, 230),
        NamedColor("Old Lace", 253, 245, 230),
        NamedColor("Antique White", 250, 235, 215),
        NamedColor("Bisque", 255, 228, 196),
        NamedColor("Moccasin", 255, 228, 181),
        NamedColor("Blanched Almond", 255, 235, 205),
        NamedColor("Papaya Whip", 255, 239, 213),
        NamedColor("Peach Puff", 255, 218, 185),
        NamedColor("Misty Rose", 255, 228, 225),
        NamedColor("Lavender Blush", 255, 240, 245),
        NamedColor("Alice Blue", 240, 248, 255),
        NamedColor("Honeydew", 240, 255, 240),
        NamedColor("Mint Cream", 245, 255, 250),
        NamedColor("Azure", 240, 255, 255),

        // Grays
        NamedColor("Gainsboro", 220, 220, 220),
        NamedColor("Light Gray", 211, 211, 211),
        NamedColor("Silver", 192, 192, 192),
        NamedColor("Dark Gray", 169, 169, 169),
        NamedColor("Gray", 128, 128, 128),
        NamedColor("Dim Gray", 105, 105, 105),
        NamedColor("Light Slate Gray", 119, 136, 153),
        NamedColor("Slate Gray", 112, 128, 144),
        NamedColor("Dark Slate Gray", 47, 79, 79),
        NamedColor("Charcoal", 54, 69, 79),

        // Blacks
        NamedColor("Near Black", 20, 20, 20),
        NamedColor("Black", 0, 0, 0),

        // Reds
        NamedColor("Red", 255, 0, 0),
        NamedColor("Dark Red", 139, 0, 0),
        NamedColor("Firebrick", 178, 34, 34),
        NamedColor("Crimson", 220, 20, 60),
        NamedColor("Indian Red", 205, 92, 92),
        NamedColor("Light Coral", 240, 128, 128),
        NamedColor("Salmon", 250, 128, 114),
        NamedColor("Dark Salmon", 233, 150, 122),
        NamedColor("Light Salmon", 255, 160, 122),
        NamedColor("Tomato", 255, 99, 71),
        NamedColor("Scarlet", 196, 30, 58),
        NamedColor("Ruby", 155, 17, 30),
        NamedColor("Venetian Red", 200, 8, 21),
        NamedColor("Brick Red", 203, 65, 84),
        NamedColor("Rust", 183, 65, 14),

        // Pinks
        NamedColor("Pink", 255, 192, 203),
        NamedColor("Light Pink", 255, 182, 193),
        NamedColor("Hot Pink", 255, 105, 180),
        NamedColor("Deep Pink", 255, 20, 147),
        NamedColor("Magenta", 255, 0, 255),
        NamedColor("Fuchsia", 255, 0, 255),
        NamedColor("Orchid", 218, 112, 214),
        NamedColor("Plum", 221, 160, 221),
        NamedColor("Violet", 238, 130, 238),
        NamedColor("Medium Orchid", 186, 85, 211),
        NamedColor("Rose", 255, 0, 127),
        NamedColor("Blush", 222, 93, 131),

        // Oranges
        NamedColor("Orange Red", 255, 69, 0),
        NamedColor("Orange", 255, 165, 0),
        NamedColor("Dark Orange", 255, 140, 0),
        NamedColor("Coral", 255, 127, 80),
        NamedColor("Burnt Orange", 204, 85, 0),
        NamedColor("Amber", 255, 191, 0),
        NamedColor("Tangerine", 242, 133, 0),
        NamedColor("Pumpkin", 255, 117, 24),
        NamedColor("Peach", 255, 203, 164),
        NamedColor("Apricot", 251, 206, 177),

        // Yellows
        NamedColor("Yellow", 255, 255, 0),
        NamedColor("Gold", 255, 215, 0),
        NamedColor("Light Yellow", 255, 255, 224),
        NamedColor("Lemon Chiffon", 255, 250, 205),
        NamedColor("Light Goldenrod", 250, 250, 210),
        NamedColor("Khaki", 240, 230, 140),
        NamedColor("Dark Khaki", 189, 183, 107),
        NamedColor("Goldenrod", 218, 165, 32),
        NamedColor("Dark Goldenrod", 184, 134, 11),
        NamedColor("Mustard", 255, 219, 88),
        NamedColor("Lemon Yellow", 255, 244, 79),
        NamedColor("Banana Yellow", 255, 225, 53),
        NamedColor("Straw", 228, 217, 111),

        // Greens
        NamedColor("Green Yellow", 173, 255, 47),
        NamedColor("Chartreuse", 127, 255, 0),
        NamedColor("Lawn Green", 124, 252, 0),
        NamedColor("Lime", 0, 255, 0),
        NamedColor("Lime Green", 50, 205, 50),
        NamedColor("Yellow Green", 154, 205, 50),
        NamedColor("Medium Spring Green", 0, 250, 154),
        NamedColor("Spring Green", 0, 255, 127),
        NamedColor("Light Green", 144, 238, 144),
        NamedColor("Pale Green", 152, 251, 152),
        NamedColor("Dark Sea Green", 143, 188, 143),
        NamedColor("Medium Sea Green", 60, 179, 113),
        NamedColor("Sea Green", 46, 139, 87),
        NamedColor("Forest Green", 34, 139, 34),
        NamedColor("Green", 0, 128, 0),
        NamedColor("Dark Green", 0, 100, 0),
        NamedColor("Olive Green", 107, 142, 35),
        NamedColor("Olive", 128, 128, 0),
        NamedColor("Dark Olive Green", 85, 107, 47),
        NamedColor("Fern Green", 79, 121, 66),
        NamedColor("Sage Green", 143, 188, 143),
        NamedColor("Hunter Green", 53, 94, 59),
        NamedColor("Emerald", 0, 201, 87),
        NamedColor("Jade", 0, 168, 107),
        NamedColor("Mint Green", 152, 255, 152),

        // Teals & Cyans
        NamedColor("Cyan", 0, 255, 255),
        NamedColor("Aqua", 0, 255, 255),
        NamedColor("Light Cyan", 224, 255, 255),
        NamedColor("Pale Turquoise", 175, 238, 238),
        NamedColor("Aquamarine", 127, 255, 212),
        NamedColor("Medium Aquamarine", 102, 205, 170),
        NamedColor("Turquoise", 64, 224, 208),
        NamedColor("Medium Turquoise", 72, 209, 204),
        NamedColor("Dark Turquoise", 0, 206, 209),
        NamedColor("Light Sea Green", 32, 178, 170),
        NamedColor("Cadet Blue", 95, 158, 160),
        NamedColor("Teal", 0, 128, 128),
        NamedColor("Dark Cyan", 0, 139, 139),
        NamedColor("Dark Teal", 0, 80, 80),

        // Blues
        NamedColor("Steel Blue", 70, 130, 180),
        NamedColor("Dodger Blue", 30, 144, 255),
        NamedColor("Deep Sky Blue", 0, 191, 255),
        NamedColor("Cornflower Blue", 100, 149, 237),
        NamedColor("Sky Blue", 135, 206, 235),
        NamedColor("Light Sky Blue", 135, 206, 250),
        NamedColor("Light Steel Blue", 176, 196, 222),
        NamedColor("Powder Blue", 176, 224, 230),
        NamedColor("Pale Blue", 173, 216, 230),
        NamedColor("Light Blue", 173, 216, 230),
        NamedColor("Royal Blue", 65, 105, 225),
        NamedColor("Blue", 0, 0, 255),
        NamedColor("Medium Blue", 0, 0, 205),
        NamedColor("Dark Blue", 0, 0, 139),
        NamedColor("Navy", 0, 0, 128),
        NamedColor("Midnight Blue", 25, 25, 112),
        NamedColor("IBM Blue", 0, 114, 178),
        NamedColor("Cobalt Blue", 0, 71, 171),
        NamedColor("Periwinkle", 204, 204, 255),
        NamedColor("Slate Blue", 106, 90, 205),
        NamedColor("Medium Slate Blue", 123, 104, 238),
        NamedColor("Dark Slate Blue", 72, 61, 139),

        // Purples
        NamedColor("Lavender", 230, 230, 250),
        NamedColor("Thistle", 216, 191, 216),
        NamedColor("Medium Purple", 147, 112, 219),
        NamedColor("Dark Orchid", 153, 50, 204),
        NamedColor("Dark Violet", 148, 0, 211),
        NamedColor("Blue Violet", 138, 43, 226),
        NamedColor("Purple", 128, 0, 128),
        NamedColor("Dark Magenta", 139, 0, 139),
        NamedColor("Indigo", 75, 0, 130),
        NamedColor("Amethyst", 153, 102, 204),
        NamedColor("Lilac", 200, 162, 200),
        NamedColor("Mauve", 224, 176, 255),
        NamedColor("Grape", 111, 45, 168),
        NamedColor("Eggplant", 97, 64, 81),

        // Browns
        NamedColor("Cornsilk", 255, 248, 220),
        NamedColor("Sandy Brown", 244, 164, 96),
        NamedColor("Burlywood", 222, 184, 135),
        NamedColor("Tan", 210, 180, 140),
        NamedColor("Wheat", 245, 222, 179),
        NamedColor("Navajo White", 255, 222, 173),
        NamedColor("Peru", 205, 133, 63),
        NamedColor("Chocolate", 210, 105, 30),
        NamedColor("Saddle Brown", 139, 69, 19),
        NamedColor("Sienna", 160, 82, 45),
        NamedColor("Brown", 165, 42, 42),
        NamedColor("Dark Brown", 101, 67, 33),
        NamedColor("Maroon", 128, 0, 0),
        NamedColor("Burnt Sienna", 233, 116, 81),
        NamedColor("Raw Umber", 115, 74, 18),
        NamedColor("Caramel", 196, 122, 54),
        NamedColor("Coffee", 111, 78, 55),
        NamedColor("Chestnut", 149, 69, 53),
        NamedColor("Mahogany", 192, 64, 0),
        NamedColor("Copper", 184, 115, 51)
    )

    // Precomputed Lab values for each named color
    private val labColors: List<Triple<DoubleArray, String, String>> by lazy {
        colors.map { c ->
            Triple(rgbToLab(c.r, c.g, c.b), c.name, "#%02X%02X%02X".format(c.r, c.g, c.b))
        }
    }

    fun nearest(r: Int, g: Int, b: Int): ColorMatch {
        val lab = rgbToLab(r, g, b)
        var bestDist = Double.MAX_VALUE
        var bestName = "Unknown"
        var bestHex = "#%02X%02X%02X".format(r, g, b)

        for ((namedLab, name, _) in labColors) {
            val dL = lab[0] - namedLab[0]
            val da = lab[1] - namedLab[1]
            val db = lab[2] - namedLab[2]
            val dist = dL * dL + da * da + db * db
            if (dist < bestDist) {
                bestDist = dist
                bestName = name
                bestHex = "#%02X%02X%02X".format(r, g, b)
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
