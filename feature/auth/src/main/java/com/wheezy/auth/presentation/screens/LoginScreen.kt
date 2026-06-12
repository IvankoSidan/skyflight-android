package com.wheezy.skyflight.feature.auth.presentation.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.wheezy.skyflight.core.common.utils.AuthValidator.isEmailValid
import com.wheezy.skyflight.core.common.utils.AuthValidator.isPasswordValid
import com.wheezy.skyflight.core.ui.R
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GlassCardDefaults
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()
    val googleState by viewModel.googleAuthState.collectAsState()

    val context = LocalContext.current
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.server_client_id))
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
        when (loginState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> errorMessage = (loginState as AuthState.Error).message
            else -> Unit
        }
    }

    LaunchedEffect(googleState) {
        when (googleState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> errorMessage = (googleState as AuthState.Error).message
            else -> Unit
        }
    }

    val isLoading = loginState is AuthState.Loading || googleState is AuthState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.shape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                config = GlassCardDefaults.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthHeader(
                        logoPainter = painterResource(id = R.drawable.ic_logo),
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
                        leadingIcon = androidx.compose.material.icons.Icons.Default.Email,
                        isError = email.isNotBlank() && !isEmailValid(email),
                        errorMessage = if (email.isNotBlank() && !isEmailValid(email)) "Invalid email format" else null,
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
                        isError = password.isNotBlank() && !isPasswordValid(password),
                        errorMessage = if (password.isNotBlank() && !isPasswordValid(password)) "Password must be at least 6 characters" else null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                if (isEmailValid(email) && isPasswordValid(password) && !isLoading) {
                                    viewModel.login(email.trim(), password)
                                }
                            }
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    RememberMeCheckbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )

                    Spacer(Modifier.height(16.dp))

                    ErrorMessage(message = errorMessage)

                    GradientButton(
                        text = if (isLoading) "Signing in..." else "Sign In",
                        enabled = isEmailValid(email) && isPasswordValid(password) && !isLoading,
                        onClick = {
                            if (!isLoading) {
                                viewModel.login(email.trim(), password)
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

                    Spacer(Modifier.height(16.dp))

                    AuthNavigationButton(
                        text = "Don't have an account? Register",
                        onClick = onNavigateToRegister
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}