package com.hueiq.app.ui.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hueiq.app.data.SavedColor
import com.hueiq.app.data.UserData
import com.hueiq.app.data.UserRepository
import com.hueiq.app.ui.theme.ThemeMode
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthMode { LOGIN, SIGNUP }

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "AuthViewModel"

    /**
     * ── HOW TO GET YOUR WEB CLIENT ID ──────────────────────────────────────────
     * 1. Go to https://console.cloud.google.com/apis/credentials
     * 2. Create a project (or select an existing one)
     * 3. Click "Create Credentials" → "OAuth client ID"
     * 4. Type = Web application → give it a name → Save
     * 5. Copy the "Client ID" and paste it below (keep the .apps.googleusercontent.com suffix)
     * 6. Also create an Android client ID with your app's SHA-1 fingerprint:
     *    Run: ./gradlew signingReport  and use the debug SHA-1
     * ───────────────────────────────────────────────────────────────────────────
     */
    private val WEB_CLIENT_ID = "219118991260-i0dk1ta1tngt7b2ojvmd92jud9d70m6d.apps.googleusercontent.com"

    private val userRepository = UserRepository(application)

    // Starts as Loading so the splash screen waits for the DataStore read
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Loading)
    val signInState = _signInState.asStateFlow()

    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode = _authMode.asStateFlow()

    /** Emits the user's saved theme preference — read by MainActivity. */
    val themeMode = userRepository.themeFlow

    /** Emits the list of colors the user has saved from the scanner. */
    val savedColors = userRepository.savedColorsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // On cold start, check if a user session is already saved locally
        viewModelScope.launch {
            val savedUser = userRepository.userFlow.first()
            _signInState.value = if (savedUser != null) {
                Log.d(TAG, "Restored session for: ${savedUser.displayName}")
                SignInState.Success(savedUser.displayName, savedUser.email, savedUser.photoUrl)
            } else {
                SignInState.Idle
            }
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        if (_signInState.value is SignInState.Error) _signInState.value = SignInState.Idle
    }

    /**
     * Two-step Google Sign-In using Credential Manager:
     *  Step 1 – show only previously used accounts (fast, no friction)
     *  Step 2 – if none found, show account picker for all Google accounts on device
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _signInState.value = SignInState.Loading
            try {
                attemptGoogleSignIn(context, filterByAuthorizedAccounts = true)
            } catch (e: NoCredentialException) {
                // No previously used account — show full account picker
                try {
                    attemptGoogleSignIn(context, filterByAuthorizedAccounts = false)
                } catch (e2: GetCredentialException) {
                    handleCredentialError(e2)
                }
            } catch (e: GetCredentialException) {
                handleCredentialError(e)
            }
        }
    }

    private suspend fun attemptGoogleSignIn(context: Context, filterByAuthorizedAccounts: Boolean) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setAutoSelectEnabled(filterByAuthorizedAccounts) // auto-select only on step 1
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(request = request, context = context)
        handleCredential(result.credential)
    }

    private suspend fun handleCredential(credential: Credential) {
        if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            _signInState.value = SignInState.Error("Unexpected credential type")
            return
        }
        try {
            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
            val userData = UserData(
                userId      = googleCred.id, // unique Google account email
                displayName = googleCred.displayName ?: googleCred.id.substringBefore("@"),
                email       = googleCred.id,
                photoUrl    = googleCred.profilePictureUri?.toString()
            )
            userRepository.saveUser(userData)
            Log.d(TAG, "Signed in and saved locally: ${userData.displayName}")
            _signInState.value = SignInState.Success(userData.displayName, userData.email, userData.photoUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Google credential", e)
            _signInState.value = SignInState.Error("Failed to parse sign-in response")
        }
    }

    private fun handleCredentialError(e: GetCredentialException) {
        Log.e(TAG, "Sign-in failed", e)
        val message = when {
            e.message?.contains("cancel", ignoreCase = true) == true ->
                "Sign in canceled."
            e.message?.contains("No credentials", ignoreCase = true) == true ->
                "No Google accounts found on this device."
            e.message?.contains("invalid_client", ignoreCase = true) == true ||
            e.message?.contains("client ID", ignoreCase = true) == true ->
                "Configuration error: check your Web Client ID."
            else -> "Sign in failed: ${e.message ?: "Unknown error"}"
        }
        _signInState.value = SignInState.Error(message)
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.clearUser()
            _signInState.value = SignInState.Idle
            Log.d(TAG, "User signed out, local data cleared")
        }
    }

    fun cycleTheme(current: ThemeMode) {
        viewModelScope.launch { userRepository.cycleTheme(current) }
    }

    fun saveColor(color: SavedColor) {
        viewModelScope.launch { userRepository.saveColor(color) }
    }

    fun removeColor(hex: String) {
        viewModelScope.launch { userRepository.removeColor(hex) }
    }
}

sealed class SignInState {
    object Idle : SignInState()
    object Loading : SignInState()
    data class Success(
        val displayName: String,
        val email: String,
        val photoUrl: String? = null
    ) : SignInState()
    data class Error(val message: String) : SignInState()
}

