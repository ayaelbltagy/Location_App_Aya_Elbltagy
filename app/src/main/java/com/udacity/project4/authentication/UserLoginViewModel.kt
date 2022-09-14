package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class UserLoginViewModel :ViewModel() {

    enum class AuthenticationStatusClass {
        AUTHENTICATED, UNAUTHENTICATED
    }
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationStatusClass.AUTHENTICATED
        } else {
            AuthenticationStatusClass.UNAUTHENTICATED
        }
    }
}