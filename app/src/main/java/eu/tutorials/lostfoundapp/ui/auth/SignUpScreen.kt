package eu.tutorials.lostfoundapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.lostfoundapp.R
import eu.tutorials.lostfoundapp.ui.components.AuthTextField

// Country Data Model
data class Country(
    val name: String,
    val code: String,
    val flag: String,
    val maxDigits: Int
)

val countriesList = listOf(
    Country("India", "+91", "🇮🇳", 10),
    Country("United States", "+1", "🇺🇸", 10),
    Country("United Kingdom", "+44", "🇬🇧", 10),
    Country("Canada", "+1", "🇨🇦", 10),
    Country("Australia", "+61", "🇦🇺", 9),
    Country("Germany", "+49", "🇩🇪", 11),
    Country("UAE", "+971", "🇦🇪", 9)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignUp: (name: String, email: String, phone: String, password: String, confirmPassword: String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(countriesList[0]) } // Default: India (+91)
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_account)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.signup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Field
            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.full_name),
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email),
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- COUNTRY SELECTION + PHONE NUMBER FIELD ---
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    // Digits filter & Selected Country maxDigits Limit
                    val filteredDigits = input.filter { it.isDigit() }
                    if (filteredDigits.length <= selectedCountry.maxDigits) {
                        phone = filteredDigits
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.phone_number)) },
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isCountryDropdownExpanded = true }
                            .padding(start = 12.dp, end = 8.dp)
                    ) {
                        Text(
                            text = "${selectedCountry.flag} ${selectedCountry.code}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Country"
                        )

                        // Dropdown List
                        DropdownMenu(
                            expanded = isCountryDropdownExpanded,
                            onDismissRequest = { isCountryDropdownExpanded = false }
                        ) {
                            countriesList.forEach { country ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = "${country.flag}  ${country.name} (${country.code})")
                                    },
                                    onClick = {
                                        selectedCountry = country
                                        if (phone.length > country.maxDigits) {
                                            phone = phone.take(country.maxDigits)
                                        }
                                        isCountryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                placeholder = { Text("${selectedCountry.maxDigits} digits") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Digit Counter Helper Text
            Text(
                text = "${phone.length} / ${selectedCountry.maxDigits} digits",
                style = MaterialTheme.typography.labelSmall,
                color = if (phone.length == selectedCountry.maxDigits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp, end = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = stringResource(R.string.confirm_password),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        val fullPhoneNumber = "${selectedCountry.code}$phone"
                        onSignUp(name, email, fullPhoneNumber, password, confirmPassword)
                    }
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            Button(
                onClick = {
                    val fullPhoneNumber = "${selectedCountry.code}$phone"
                    onSignUp(name, email, fullPhoneNumber, password, confirmPassword)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoading) stringResource(R.string.creating_account) else stringResource(R.string.sign_up),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateBack) {
                Text(stringResource(R.string.already_have_account))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}