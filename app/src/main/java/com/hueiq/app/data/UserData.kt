package com.hueiq.app.data

/**
 * Represents a signed-in user. All data comes from Google Sign-In
 * and is persisted locally in DataStore — no backend required.
 *
 * @param userId     Unique user ID (Google account email)
 * @param displayName Full name from Google account
 * @param email       Google account email address
 * @param photoUrl    Profile picture URL from Google (nullable)
 */
data class UserData(
    val userId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null
)
