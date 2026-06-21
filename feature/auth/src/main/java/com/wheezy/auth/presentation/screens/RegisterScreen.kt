package com.wheezy.skyflight.feature.auth.presentation.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val registerState by viewModel.registerState.collectAsState()
    val googleState by viewModel.googleAuthState.collectAsState()

    val context = LocalContext.current
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

    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is AuthState.Success -> onRegisterSuccess()
            is AuthState.Error -> errorMessage = state.message
            else -> Unit
        }
    }

    LaunchedEffect(googleState) {
        when (val state = googleState) {
            is AuthState.Success -> onRegisterSuccess()
            is AuthState.Error -> errorMessage = state.message
            else -> Unit
        }
    }

    val isLoading = registerState is AuthState.Loading || googleState is AuthState.Loading

    AuthBackground {  // Используем AuthBackground
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

            AuthTitle(title = "Register")

            Spacer(Modifier.height(16.dp))

            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                leadingIcon = Icons.Default.Person,
                isError = name.isNotBlank() && name.length < 2,
                errorMessage = if (name.isNotBlank() && name.length < 2) "Name must be at least 2 characters" else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(Modifier.height(12.dp))

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
                        if (
                            name.isNotBlank() &&
                            AuthValidator.isEmailValid(email) &&
                            AuthValidator.isPasswordValid(password) &&
                            !isLoading
                        ) {
                            viewModel.register(name.trim(), email.trim(), password)
                        }
                    }
                )
            )

            Spacer(Modifier.height(16.dp))

            ErrorMessage(message = errorMessage)

            GradientButton(
                text = if (isLoading) "Registering..." else "Sign Up",
                enabled = name.isNotBlank() &&
                        AuthValidator.isEmailValid(email) &&
                        AuthValidator.isPasswordValid(password) &&
                        !isLoading,
                onClick = {
                    if (!isLoading) {
                        viewModel.register(name.trim(), email.trim(), password)
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            AuthDivider()

            Spacer(Modifier.height(16.dp))

            GoogleSignInButton(
                onClick = {
                    if (!isLoading) {
                        launcher.launch(googleClient.signInIntent)
                    }
                },
                isLoading = isLoading
            )

            Spacer(Modifier.height(24.dp))

            AuthNavigationButton(
                text = "Already have an account? Sign In",
                onClick = onNavigateBack
            )
        }
    }
}