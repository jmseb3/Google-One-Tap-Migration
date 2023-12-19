package com.wonddak.googletest

import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


class GoogleLoginHelper(
    context: Context
) {

    companion object {
        const val TAG = "GoogleLogin"
    }

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    init {
        oneTapClient = Identity.getSignInClient(context)

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("INPUT_WEB_CLIENT_ID")
                    // Only show accounts previously used to sign in.(true)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()
    }

    fun requestGoogleLogin(
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSender =
                        IntentSenderRequest.Builder(
                            result.pendingIntent.intentSender
                        ).apply {
                            setFillInIntent(null)
                        }.build()
                    launcher.launch(intentSender)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                e.localizedMessage?.let { Log.d(TAG, it) }
            }
    }

    fun registerToFirebase(
        result: ActivityResult,
        failAction: (msg: String) -> Unit,
        successAction: (result: AuthResult) -> Unit
    ) {
        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
        val token = credential.googleIdToken
        try {
            if (token == null) {
                // error
            } else {
                // firebase auth register
                val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
                val auth = Firebase.auth
                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener {
                        successAction(it)
                    }
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    failAction("One-tap dialog was closed.")
                }

                CommonStatusCodes.NETWORK_ERROR -> {
                    failAction("One-tap encountered a network error.")
                }

                else -> {
                    failAction("Couldn't get credential from result. (${e.localizedMessage})")
                }
            }
        }
    }
}