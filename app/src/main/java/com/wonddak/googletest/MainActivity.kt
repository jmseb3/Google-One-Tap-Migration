package com.wonddak.googletest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.wonddak.googletest.ui.theme.GoogleTestTheme

class MainActivity : ComponentActivity() {
    private val googleLoginHelper by lazy { GoogleLoginHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoogleTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoogleLoginView(googleLoginHelper = googleLoginHelper)
                }
            }
        }
    }
}

@Composable
fun GoogleLoginView(
    googleLoginHelper: GoogleLoginHelper
) {
    var showLoading: Boolean by remember {
        mutableStateOf(false)
    }
    var user by remember {
        mutableStateOf("")
    }
    val googleLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            googleLoginHelper.registerToFirebase(
                result,
                failAction = { msg ->
                    showLoading = false
                },
                successAction = {result -> 
                    showLoading = false
                    user = result.user?.uid ?: ""
                }
            )
        }
    Box(modifier = Modifier.fillMaxSize()) {
        //content 
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(
                onClick = {
                    googleLoginHelper.requestGoogleLogin(googleLauncher)
                }
            ) {
                Text(text = "Request Login")
            }
            Text(text = "Uid is : $user")
        }
        //Loading
        if (showLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

}