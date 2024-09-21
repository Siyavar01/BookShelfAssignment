package com.example.bookshelf

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpScreen()
        }
    }
}

@Composable
fun SignUpScreen() {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val selectedCountry = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Cache the country list to avoid multiple API calls
    var countryList by rememberSaveable { mutableStateOf(listOf<Country>()) }
    var countriesFetched by rememberSaveable { mutableStateOf(false) }

    // Fetch countries from the API if they are not fetched yet
    LaunchedEffect(Unit) {
        if (!countriesFetched) {
            coroutineScope.launch {
                RetrofitInstance.api.getCountries().enqueue(object : Callback<List<Country>> {
                    override fun onResponse(call: Call<List<Country>>, response: Response<List<Country>>) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                countryList = it
                                countriesFetched = true  // Mark as fetched to prevent re-fetching
                            } ?: run {
                                Toast.makeText(context, "No countries found", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to fetch countries", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<List<Country>>, t: Throwable) {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                val icon = if (passwordVisible.value) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Country dropdown
        var expanded by rememberSaveable { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedCountry.value.isNotEmpty()) selectedCountry.value else "Select Country")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                countryList.forEach { country ->
                    DropdownMenuItem(onClick = {
                        selectedCountry.value = country.country
                        expanded = false
                    }) {
                        Text(country.country)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (validateEmail(email.value) && validatePassword(password.value)) {
                    Toast.makeText(
                        context,
                        "Sign up successful for ${email.value} in ${selectedCountry.value}",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(context, BookShelfActivity::class.java)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "Invalid email or password",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
    }
}

fun validateEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun validatePassword(password: String): Boolean {
    val passwordPattern = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +                // at least one digit
                "(?=.*[a-z])" +                // at least one lowercase letter
                "(?=.*[A-Z])" +                // at least one uppercase letter
                "(?=.*[!@#\$%^&*(),])" +       // at least one special character
                "(?=\\S+$)" +                  // no whitespace allowed
                ".{8,}" +                      // at least 8 characters
                "$"
    )
    return passwordPattern.matcher(password).matches()
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}
