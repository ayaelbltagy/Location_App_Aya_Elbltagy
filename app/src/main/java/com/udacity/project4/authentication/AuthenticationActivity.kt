package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val SIGN_IN_REQUEST_CODE = 100
    }

    // Get a reference to the ViewModel scoped to this Fragment
    private val viewModel by viewModels<UserLoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        observeAuthenticateState()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST_CODE) {

            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    this,
                    "Successfully signed in user " + "${FirebaseAuth.getInstance().currentUser?.displayName}!",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)

            } else {
                Toast.makeText(
                    this,
                    "Sign in unsuccessful ${response?.error?.errorCode}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun onLoginClicked(view: View) {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    private fun observeAuthenticateState() {
        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                UserLoginViewModel.AuthenticationStatusClass.AUTHENTICATED -> {
                    // show logout
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                }
                UserLoginViewModel.AuthenticationStatusClass.UNAUTHENTICATED -> {
                    // convert  logout to login
                }
            }
        })
    }
}
