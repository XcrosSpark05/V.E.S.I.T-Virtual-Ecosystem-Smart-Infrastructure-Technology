package com.example.vesit.features.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vesit.features.auth.data.GoogleAuthUiHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userState = MutableStateFlow(auth.currentUser)
    val userState = _userState.asStateFlow()

    private val _errorEvents = Channel<String>()
    val errorEvents = _errorEvents.receiveAsFlow()

    // 1. ADD THIS FOR THE BYPASS logic
    var isDemoMode by mutableStateOf(false)
    var demoEmail by mutableStateOf("")

    fun bypassLoginForDemo() {
        viewModelScope.launch {
            val fakeUid = "demo_rahul_123"
            val fakeEmail = "rahul.student@ves.ac.in"

            val userData = hashMapOf(
                "email" to fakeEmail,
                "name" to "Rahul",
                "uid" to fakeUid,
                "role" to "student"
            )

            try {
                // Save to Firestore so Teacher UI sees Rahul in the 'users' list
                db.collection("users").document(fakeUid).set(userData).await()

                // Set local demo state
                isDemoMode = true
                demoEmail = fakeEmail

                // Trigger navigation by updating userState (Mocking a user object)
                // Note: auth.currentUser will remain null, handled in navigation
                _userState.value = auth.currentUser
            } catch (e: Exception) {
                _errorEvents.send("Bypass Error: ${e.message}")
            }
        }
    }

    fun signIn(authHelper: GoogleAuthUiHelper) {
        viewModelScope.launch {
            try {
                val idToken = authHelper.signIn()
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val result = auth.signInWithCredential(credential).await()
                    val user = result.user
                    val email = user?.email ?: ""

                    // 1. Domain Check
                    if (email.endsWith("@ves.ac.in") || email.endsWith("@gmail.com")) {

                        // 2. CRITICAL: Save user profile to Firestore
                        // This ensures the Teacher UI can fetch the email for the live list
                        val role = if (email.endsWith("@gmail.com")) "teacher" else "student"
                        val userData = hashMapOf(
                            "email" to email,
                            "uid" to user?.uid,
                            "role" to role
                        )

                        user?.uid?.let { uid ->
                            db.collection("users").document(uid).set(userData).await()
                        }

                        _userState.value = user
                    } else {
                        // Unauthorized domain
                        user?.delete()?.await()
                        auth.signOut()
                        _userState.value = null
                        _errorEvents.send("Access Denied: Use VESIT or Gmail address.")
                    }
                }
            } catch (e: Exception) {
                _errorEvents.send("Login failed: ${e.localizedMessage}")
            }
        }
    }

    fun showError(message: String) {
        viewModelScope.launch {
            _errorEvents.send(message)
        }
    }
}