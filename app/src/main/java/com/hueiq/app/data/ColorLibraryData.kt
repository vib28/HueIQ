package com.hueiq.app.data

import kotlin.math.roundToInt

enum class ColorCategory(val label: String) {
    ALL("All"),
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

    val all: List<ColorEntry> by lazy { buildList() }

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

    private fun entry(name: String, r: Int, g: Int, b: Int, cat: ColorCategory) =
        ColorEntry(name, r, g, b, "#%02X%02X%02X".format(r, g, b), cat)

    private fun buildList() = listOf(
        // ── Whites ───────────────────────────────────────────────────────────
        entry("White",            255, 255, 255, ColorCategory.WHITES),
        entry("Snow",             255, 250, 250, ColorCategory.WHITES),
        entry("Ivory",            255, 255, 240, ColorCategory.WHITES),
        entry("Floral White",     255, 250, 240, ColorCategory.WHITES),
        entry("Ghost White",      248, 248, 255, ColorCategory.WHITES),
        entry("Seashell",         255, 245, 238, ColorCategory.WHITES),
        entry("Linen",            250, 240, 230, ColorCategory.WHITES),
        entry("Old Lace",         253, 245, 230, ColorCategory.WHITES),
        entry("Antique White",    250, 235, 215, ColorCategory.WHITES),
        entry("Bisque",           255, 228, 196, ColorCategory.WHITES),
        entry("Moccasin",         255, 228, 181, ColorCategory.WHITES),
        entry("Blanched Almond",  255, 235, 205, ColorCategory.WHITES),
        entry("Papaya Whip",      255, 239, 213, ColorCategory.WHITES),
        entry("Peach Puff",       255, 218, 185, ColorCategory.WHITES),
        entry("Misty Rose",       255, 228, 225, ColorCategory.WHITES),
        entry("Lavender Blush",   255, 240, 245, ColorCategory.WHITES),
        entry("Alice Blue",       240, 248, 255, ColorCategory.WHITES),
        entry("Honeydew",         240, 255, 240, ColorCategory.WHITES),
        entry("Mint Cream",       245, 255, 250, ColorCategory.WHITES),
        entry("Azure",            240, 255, 255, ColorCategory.WHITES),

        // ── Grays ────────────────────────────────────────────────────────────
        entry("Gainsboro",        220, 220, 220, ColorCategory.GRAYS),
        entry("Light Gray",       211, 211, 211, ColorCategory.GRAYS),
        entry("Silver",           192, 192, 192, ColorCategory.GRAYS),
        entry("Dark Gray",        169, 169, 169, ColorCategory.GRAYS),
        entry("Gray",             128, 128, 128, ColorCategory.GRAYS),
        entry("Dim Gray",         105, 105, 105, ColorCategory.GRAYS),
        entry("Light Slate Gray", 119, 136, 153, ColorCategory.GRAYS),
        entry("Slate Gray",       112, 128, 144, ColorCategory.GRAYS),
        entry("Dark Slate Gray",   47,  79,  79, ColorCategory.GRAYS),
        entry("Charcoal",          54,  69,  79, ColorCategory.GRAYS),
        entry("Near Black",        20,  20,  20, ColorCategory.GRAYS),
        entry("Black",              0,   0,   0, ColorCategory.GRAYS),

        // ── Reds ─────────────────────────────────────────────────────────────
        entry("Red",              255,   0,   0, ColorCategory.REDS),
        entry("Dark Red",         139,   0,   0, ColorCategory.REDS),
        entry("Firebrick",        178,  34,  34, ColorCategory.REDS),
        entry("Crimson",          220,  20,  60, ColorCategory.REDS),
        entry("Indian Red",       205,  92,  92, ColorCategory.REDS),
        entry("Light Coral",      240, 128, 128, ColorCategory.REDS),
        entry("Salmon",           250, 128, 114, ColorCategory.REDS),
        entry("Dark Salmon",      233, 150, 122, ColorCategory.REDS),
        entry("Light Salmon",     255, 160, 122, ColorCategory.REDS),
        entry("Tomato",           255,  99,  71, ColorCategory.REDS),
        entry("Scarlet",          196,  30,  58, ColorCategory.REDS),
        entry("Ruby",             155,  17,  30, ColorCategory.REDS),
        entry("Venetian Red",     200,   8,  21, ColorCategory.REDS),
        entry("Brick Red",        203,  65,  84, ColorCategory.REDS),
        entry("Rust",             183,  65,  14, ColorCategory.REDS),

        // ── Pinks ────────────────────────────────────────────────────────────
        entry("Pink",             255, 192, 203, ColorCategory.PINKS),
        entry("Light Pink",       255, 182, 193, ColorCategory.PINKS),
        entry("Hot Pink",         255, 105, 180, ColorCategory.PINKS),
        entry("Deep Pink",        255,  20, 147, ColorCategory.PINKS),
        entry("Magenta",          255,   0, 255, ColorCategory.PINKS),
        entry("Orchid",           218, 112, 214, ColorCategory.PINKS),
        entry("Plum",             221, 160, 221, ColorCategory.PINKS),
        entry("Violet",           238, 130, 238, ColorCategory.PINKS),
        entry("Medium Orchid",    186,  85, 211, ColorCategory.PINKS),
        entry("Rose",             255,   0, 127, ColorCategory.PINKS),
        entry("Blush",            222,  93, 131, ColorCategory.PINKS),
        entry("Lavender Blush",   255, 240, 245, ColorCategory.PINKS),

        // ── Oranges ──────────────────────────────────────────────────────────
        entry("Orange Red",       255,  69,   0, ColorCategory.ORANGES),
        entry("Orange",           255, 165,   0, ColorCategory.ORANGES),
        entry("Dark Orange",      255, 140,   0, ColorCategory.ORANGES),
        entry("Coral",            255, 127,  80, ColorCategory.ORANGES),
        entry("Burnt Orange",     204,  85,   0, ColorCategory.ORANGES),
        entry("Tangerine",        242, 133,   0, ColorCategory.ORANGES),
        entry("Pumpkin",          255, 117,  24, ColorCategory.ORANGES),
        entry("Peach",            255, 203, 164, ColorCategory.ORANGES),
        entry("Apricot",          251, 206, 177, ColorCategory.ORANGES),
        entry("Burnt Sienna",     233, 116,  81, ColorCategory.ORANGES),

        // ── Yellows ──────────────────────────────────────────────────────────
        entry("Yellow",           255, 255,   0, ColorCategory.YELLOWS),
        entry("Gold",             255, 215,   0, ColorCategory.YELLOWS),
        entry("Amber",            255, 191,   0, ColorCategory.YELLOWS),
        entry("Light Yellow",     255, 255, 224, ColorCategory.YELLOWS),
        entry("Lemon Chiffon",    255, 250, 205, ColorCategory.YELLOWS),
        entry("Light Goldenrod",  250, 250, 210, ColorCategory.YELLOWS),
        entry("Khaki",            240, 230, 140, ColorCategory.YELLOWS),
        entry("Dark Khaki",       189, 183, 107, ColorCategory.YELLOWS),
        entry("Goldenrod",        218, 165,  32, ColorCategory.YELLOWS),
        entry("Dark Goldenrod",   184, 134,  11, ColorCategory.YELLOWS),
        entry("Mustard",          255, 219,  88, ColorCategory.YELLOWS),
        entry("Lemon Yellow",     255, 244,  79, ColorCategory.YELLOWS),
        entry("Straw",            228, 217, 111, ColorCategory.YELLOWS),

        // ── Greens ───────────────────────────────────────────────────────────
        entry("Green Yellow",     173, 255,  47, ColorCategory.GREENS),
        entry("Chartreuse",       127, 255,   0, ColorCategory.GREENS),
        entry("Lawn Green",       124, 252,   0, ColorCategory.GREENS),
        entry("Lime",               0, 255,   0, ColorCategory.GREENS),
        entry("Lime Green",        50, 205,  50, ColorCategory.GREENS),
        entry("Yellow Green",     154, 205,  50, ColorCategory.GREENS),
        entry("Medium Spring Green", 0, 250, 154, ColorCategory.GREENS),
        entry("Spring Green",       0, 255, 127, ColorCategory.GREENS),
        entry("Light Green",      144, 238, 144, ColorCategory.GREENS),
        entry("Pale Green",       152, 251, 152, ColorCategory.GREENS),
        entry("Dark Sea Green",   143, 188, 143, ColorCategory.GREENS),
        entry("Medium Sea Green",  60, 179, 113, ColorCategory.GREENS),
        entry("Sea Green",         46, 139,  87, ColorCategory.GREENS),
        entry("Forest Green",      34, 139,  34, ColorCategory.GREENS),
        entry("Green",              0, 128,   0, ColorCategory.GREENS),
        entry("Dark Green",         0, 100,   0, ColorCategory.GREENS),
        entry("Olive Green",      107, 142,  35, ColorCategory.GREENS),
        entry("Olive",            128, 128,   0, ColorCategory.GREENS),
        entry("Dark Olive Green",  85, 107,  47, ColorCategory.GREENS),
        entry("Fern Green",        79, 121,  66, ColorCategory.GREENS),
        entry("Hunter Green",      53,  94,  59, ColorCategory.GREENS),
        entry("Emerald",            0, 201,  87, ColorCategory.GREENS),
        entry("Jade",               0, 168, 107, ColorCategory.GREENS),
        entry("Mint Green",       152, 255, 152, ColorCategory.GREENS),

        // ── Teals ────────────────────────────────────────────────────────────
        entry("Cyan",               0, 255, 255, ColorCategory.TEALS),
        entry("Aqua",               0, 255, 255, ColorCategory.TEALS),
        entry("Light Cyan",       224, 255, 255, ColorCategory.TEALS),
        entry("Pale Turquoise",   175, 238, 238, ColorCategory.TEALS),
        entry("Aquamarine",       127, 255, 212, ColorCategory.TEALS),
        entry("Medium Aquamarine",102, 205, 170, ColorCategory.TEALS),
        entry("Turquoise",         64, 224, 208, ColorCategory.TEALS),
        entry("Medium Turquoise",  72, 209, 204, ColorCategory.TEALS),
        entry("Dark Turquoise",     0, 206, 209, ColorCategory.TEALS),
        entry("Light Sea Green",   32, 178, 170, ColorCategory.TEALS),
        entry("Cadet Blue",        95, 158, 160, ColorCategory.TEALS),
        entry("Teal",               0, 128, 128, ColorCategory.TEALS),
        entry("Dark Cyan",          0, 139, 139, ColorCategory.TEALS),
        entry("Dark Teal",          0,  80,  80, ColorCategory.TEALS),

        // ── Blues ────────────────────────────────────────────────────────────
        entry("Steel Blue",        70, 130, 180, ColorCategory.BLUES),
        entry("Dodger Blue",       30, 144, 255, ColorCategory.BLUES),
        entry("Deep Sky Blue",      0, 191, 255, ColorCategory.BLUES),
        entry("Cornflower Blue",  100, 149, 237, ColorCategory.BLUES),
        entry("Sky Blue",         135, 206, 235, ColorCategory.BLUES),
        entry("Light Sky Blue",   135, 206, 250, ColorCategory.BLUES),
        entry("Light Steel Blue", 176, 196, 222, ColorCategory.BLUES),
        entry("Powder Blue",      176, 224, 230, ColorCategory.BLUES),
        entry("Light Blue",       173, 216, 230, ColorCategory.BLUES),
        entry("Royal Blue",        65, 105, 225, ColorCategory.BLUES),
        entry("Blue",               0,   0, 255, ColorCategory.BLUES),
        entry("Medium Blue",        0,   0, 205, ColorCategory.BLUES),
        entry("Dark Blue",          0,   0, 139, ColorCategory.BLUES),
        entry("Navy",               0,   0, 128, ColorCategory.BLUES),
        entry("Midnight Blue",     25,  25, 112, ColorCategory.BLUES),
        entry("IBM Blue",           0, 114, 178, ColorCategory.BLUES),
        entry("Cobalt Blue",        0,  71, 171, ColorCategory.BLUES),
        entry("Periwinkle",       204, 204, 255, ColorCategory.BLUES),
        entry("Slate Blue",       106,  90, 205, ColorCategory.BLUES),
        entry("Medium Slate Blue",123, 104, 238, ColorCategory.BLUES),
        entry("Dark Slate Blue",   72,  61, 139, ColorCategory.BLUES),

        // ── Purples ──────────────────────────────────────────────────────────
        entry("Lavender",         230, 230, 250, ColorCategory.PURPLES),
        entry("Thistle",          216, 191, 216, ColorCategory.PURPLES),
        entry("Medium Purple",    147, 112, 219, ColorCategory.PURPLES),
        entry("Dark Orchid",      153,  50, 204, ColorCategory.PURPLES),
        entry("Dark Violet",      148,   0, 211, ColorCategory.PURPLES),
        entry("Blue Violet",      138,  43, 226, ColorCategory.PURPLES),
        entry("Purple",           128,   0, 128, ColorCategory.PURPLES),
        entry("Dark Magenta",     139,   0, 139, ColorCategory.PURPLES),
        entry("Indigo",            75,   0, 130, ColorCategory.PURPLES),
        entry("Amethyst",         153, 102, 204, ColorCategory.PURPLES),
        entry("Lilac",            200, 162, 200, ColorCategory.PURPLES),
        entry("Mauve",            224, 176, 255, ColorCategory.PURPLES),
        entry("Grape",            111,  45, 168, ColorCategory.PURPLES),
        entry("Eggplant",          97,  64,  81, ColorCategory.PURPLES),

        // ── Browns ───────────────────────────────────────────────────────────
        entry("Sandy Brown",      244, 164,  96, ColorCategory.BROWNS),
        entry("Burlywood",        222, 184, 135, ColorCategory.BROWNS),
        entry("Tan",              210, 180, 140, ColorCategory.BROWNS),
        entry("Wheat",            245, 222, 179, ColorCategory.BROWNS),
        entry("Navajo White",     255, 222, 173, ColorCategory.BROWNS),
        entry("Peru",             205, 133,  63, ColorCategory.BROWNS),
        entry("Chocolate",        210, 105,  30, ColorCategory.BROWNS),
        entry("Saddle Brown",     139,  69,  19, ColorCategory.BROWNS),
        entry("Sienna",           160,  82,  45, ColorCategory.BROWNS),
        entry("Brown",            165,  42,  42, ColorCategory.BROWNS),
        entry("Dark Brown",       101,  67,  33, ColorCategory.BROWNS),
        entry("Maroon",           128,   0,   0, ColorCategory.BROWNS),
        entry("Raw Umber",        115,  74,  18, ColorCategory.BROWNS),
        entry("Caramel",          196, 122,  54, ColorCategory.BROWNS),
        entry("Coffee",           111,  78,  55, ColorCategory.BROWNS),
        entry("Chestnut",         149,  69,  53, ColorCategory.BROWNS),
        entry("Mahogany",         192,  64,   0, ColorCategory.BROWNS),
        entry("Copper",           184, 115,  51, ColorCategory.BROWNS),
        entry("Cornsilk",         255, 248, 220, ColorCategory.BROWNS),
        entry("Dark Goldenrod",   184, 134,  11, ColorCategory.BROWNS)
    )
}
