package com.wheezy.skyflight.feature.auth.presentation.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.wheezy.skyflight.core.common.utils.AuthValidator
import com.wheezy.skyflight.core.config.Config
import com.wheezy.skyflight.core.ui.components.AuthBackground
import com.wheezy.skyflight.core.ui.components.GradientButton
import com.wheezy.skyflight.feature.auth.presentation.components.*
import com.wheezy.skyflight.feature.auth.presentation.states.AuthState
import com.wheezy.skyflight.feature.auth.presentation.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var saveCredentials by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()
    val googleState by viewModel.googleAuthState.collectAsState()
    val savedEmailState by viewModel.savedEmail.collectAsState()
    val savedPasswordState by viewModel.savedPassword.collectAsState()
    val shouldSaveCredentialsState by viewModel.shouldSaveCredentials.collectAsState()
    val isBiometricAvailableState by viewModel.isBiometricAvailable.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkBiometricAvailability(activity)
        val savedEmail = savedEmailState
        val savedPassword = savedPasswordState
        val shouldSave = shouldSaveCredentialsState
        if (shouldSave && savedEmail != null && savedPassword != null) {
            email = savedEmail
            password = savedPassword
            saveCredentials = true
        }
    }

    fun handleBiometricLogin() {
        viewModel.loginWithBiometric(activity)
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(Config.GOOGLE_SERVER_CLIENT_ID)
        .requestEmail()
        .build()
    val googleClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.googleAuth(it) }
            } catch (_: ApiException) { }
        }
    }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    LaunchedEffect(googleState) {
        when (val state = googleState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader(
                logoPainter = androidx.compose.ui.res.painterResource(id = com.wheezy.skyflight.core.ui.R.drawable.ic_logo),
                title = "SkyBook",
                subtitle = "Find your pass"
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTitle(title = "Login")

            Spacer(Modifier.height(16.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                isError = email.isNotBlank() && !AuthValidator.isEmailValid(email),
                errorMessage = if (email.isNotBlank() && !AuthValidator.isEmailValid(email)) "Invalid email format" else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(12.dp))

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isError = password.isNotBlank() && !AuthValidator.isPasswordValid(password),
                errorMessage = if (password.isNotBlank() && !AuthValidator.isPasswordValid(password)) "Password must be at least 6 characters" else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        if (AuthValidator.isEmailValid(email) && AuthValidator.isPasswordValid(password) && !isLoadingState) {
                            viewModel.login(email.trim(), password, saveCredentials)
                        }
                    }
                )
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveCredentials,
                    onCheckedChange = { saveCredentials = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Save credentials for quick login",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            ErrorMessage(message = errorMessage)

            GradientButton(
                text = if (isLoadingState) "Signing in..." else "Sign In",
                enabled = AuthValidator.isEmailValid(email) && AuthValidator.isPasswordValid(password) && !isLoadingState,
                onClick = {
                    if (!isLoadingState) {
                        viewModel.login(email.trim(), password, saveCredentials)
                    }
                }
            )

            if (isBiometricAvailableState && shouldSaveCredentialsState) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { handleBiometricLogin() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.wheezy.skyflight.core.ui.R.drawable.ic_fingerprint),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login with Fingerprint", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            AuthDivider()

            Spacer(Modifier.height(16.dp))

            GoogleSignInButton(
                onClick = {
                    if (!isLoadingState) {
                        launcher.launch(googleClient.signInIntent)
                    }
                },
                isLoading = isLoadingState
            )

            Spacer(Modifier.height(16.dp))

            AuthNavigationButton(
                text = "Don't have an account? Register",
                onClick = onNavigateToRegister
            )
        }
    }
}