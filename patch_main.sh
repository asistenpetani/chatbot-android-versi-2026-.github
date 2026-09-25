#!/bin/bash
awk '
/class MainActivity/ {
    print "import androidx.credentials.CredentialManager"
    print "import androidx.credentials.GetCredentialRequest"
    print "import androidx.credentials.exceptions.GetCredentialException"
    print "import com.google.android.libraries.identity.googleid.GetGoogleIdOption"
    print "import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential"
    print "import com.google.firebase.auth.GoogleAuthProvider"
}
/private val viewModel: AppViewModel by viewModels\(\)/ {
    print "    private fun performGoogleSignIn(authViewModel: com.asistenpetani.ui.viewmodel.AuthViewModel) {"
    print "        val credentialManager = CredentialManager.create(this)"
    print "        val webClientId = getString(R.string.default_web_client_id)"
    print "        val googleIdOption = GetGoogleIdOption.Builder()"
    print "            .setFilterByAuthorizedAccounts(false)"
    print "            .setServerClientId(webClientId)"
    print "            .setAutoSelectEnabled(true)"
    print "            .build()"
    print "        val request = GetCredentialRequest.Builder()"
    print "            .addCredentialOption(googleIdOption)"
    print "            .build()"
    print "        lifecycleScope.launch {"
    print "            try {"
    print "                val result = credentialManager.getCredential(this@MainActivity, request)"
    print "                val credential = result.credential"
    print "                if (credential is androidx.credentials.CustomCredential &&"
    print "                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {"
    print "                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)"
    print "                    val idToken = googleIdTokenCredential.idToken"
    print "                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)"
    print "                    authViewModel.signInWithGoogle(firebaseCredential)"
    print "                } else {"
    print "                    Toast.makeText(this@MainActivity, \"Credential type not supported\", Toast.LENGTH_SHORT).show()"
    print "                }"
    print "            } catch (e: Exception) {"
    print "                Log.e(\"MainActivity\", \"Google Sign In Failed\", e)"
    print "                Toast.makeText(this@MainActivity, \"Login Google gagal: ${e.message}\", Toast.LENGTH_SHORT).show()"
    print "            }"
    print "        }"
    print "    }"
}
/onAdminLoginClick = { email, sandi ->/ {
    print "                onGoogleSignInClick = {"
    print "                    performGoogleSignIn(authViewModel)"
    print "                },"
}
{ print $0 }
' app/src/main/java/com/asistenpetani/MainActivity.kt > temp.kt && mv temp.kt app/src/main/java/com/asistenpetani/MainActivity.kt
