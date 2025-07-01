package com.malikhain.kuripot_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.malikhain.kuripot_app.viewmodel.PasscodeViewModel

@Composable
fun PasscodeScreen(
    viewModel: PasscodeViewModel,
    onUnlock: () -> Unit
) {
    val pin by viewModel.pin.collectAsState()
    val error by viewModel.error.collectAsState()
    val unlocked by viewModel.unlocked.collectAsState()
    val isPasscodeSet by viewModel.isPasscodeSet.collectAsState()

    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }

    if (unlocked) {
        LaunchedEffect(Unit) { onUnlock() }
    }

    if (!isPasscodeSet) {
        // Show set passcode dialog
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Set Passcode", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { if (it.length <= 4) passcode = it.filter { c -> c.isDigit() } },
                        label = { Text("Passcode") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPasscode,
                        onValueChange = { if (it.length <= 4) confirmPasscode = it.filter { c -> c.isDigit() } },
                        label = { Text("Confirm Passcode") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error ?: "", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setPasscode(passcode, confirmPasscode) },
                        enabled = passcode.length == 4 && confirmPasscode.length == 4
                    ) {
                        Text("Set Passcode")
                    }
                }
            }
        }
    } else {
        // Show unlock UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter Passcode", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { i ->
                    OutlinedTextField(
                        value = pin.getOrNull(i)?.toString() ?: "",
                        onValueChange = { value ->
                            val newPin =
                                pin.substring(0, i) + value.take(1) + pin.substring((i + 1).coerceAtMost(pin.length))
                            viewModel.onPinChange(newPin.take(4))
                        },
                        modifier = Modifier.width(48.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.submitPin() },
                enabled = pin.length == 4
            ) {
                Text("Unlock")
            }
            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
} 