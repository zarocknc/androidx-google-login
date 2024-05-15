package com.pointererp.pointerapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.pointererp.pointerapp.ui.theme.PointerAppTheme
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PointerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        GoogleSignInButton()
                    }

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isGooglePlayServicesAvailable =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)

    Column {
        if (isGooglePlayServicesAvailable == ConnectionResult.SUCCESS) {
            Text(text = "GOOGLE DISPONIBLE")
        } else {
            Text(text = "GOOGLE NO DISPONIBLE")
        }
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        Text(text = "This is a test")
    }

}

@Composable
fun GoogleSignInButton() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    val onClick: () -> Unit = {
        val credentialManager = CredentialManager.create(context = context)
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("CAMBIAR GOR GOOGLE OAUTH ID (web): https://www.youtube.com/watch?v=P_jZMDmodG4")
            .setNonce(hashedNonce)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        coroutineScope.launch {
            try {

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val googleIdToken = googleIdTokenCredential.idToken
                Log.i(TAG, googleIdToken)
                Toast.makeText(context, "Estas logeado ", Toast.LENGTH_LONG).show()
            } catch (e: GetCredentialException) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    Button(onClick = onClick) {
        Text(text = "Sign in with Google")
    }
}