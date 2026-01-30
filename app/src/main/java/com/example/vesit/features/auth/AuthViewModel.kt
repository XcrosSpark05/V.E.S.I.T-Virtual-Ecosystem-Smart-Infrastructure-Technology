package com.example.vesit.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vesit.features.auth.data.GoogleAuthUiHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _userState = MutableStateFlow(auth.currentUser)
    val userState = _userState.asStateFlow()

    // Channel to send error messages to the UI
    private val _errorEvents = Channel<String>()
    val errorEvents = _errorEvents.receiveAsFlow()

    fun signIn(authHelper: GoogleAuthUiHelper) {
        viewModelScope.launch {
            try {
                val idToken = authHelper.signIn()
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val result = auth.signInWithCredential(credential).await()
                    val user = result.user

                    // Institutional Domain Check
                    if (user?.email?.endsWith("@ves.ac.in") == true) {
                        _userState.value = user
                    } else {
                        // 1. Delete the user from Firebase Auth database
                        user?.delete()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("Unauthorized user record deleted successfully.")
                            }
                        }
                        // 2. Sign out and clear state
                        auth.signOut()
                        _userState.value = null
                        _errorEvents.send("Access Denied: Please use your VESIT email.")
                    }
                }
            } catch (e: Exception) {
                _errorEvents.send("Login failed: ${e.localizedMessage}")
            }
        }
    }
}