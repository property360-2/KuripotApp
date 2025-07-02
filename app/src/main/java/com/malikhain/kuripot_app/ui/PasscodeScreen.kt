package com.malikhain.kuripot_app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    val showForgotPasscodeDialog by viewModel.showForgotPasscodeDialog.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var showPasscode by remember { mutableStateOf(false) }
    var showConfirmPasscode by remember { mutableStateOf(false) }

    // Animation for error shake
    val errorShake by animateFloatAsState(
        targetValue = if (error != null) 1f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "errorShake"
    )

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
                    
                    // Passcode field with show/hide
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { if (it.length <= 4) passcode = it.filter { c -> c.isDigit() } },
                        label = { Text("Passcode") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (showPasscode) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showPasscode = !showPasscode }) {
                                Icon(
                                    imageVector = if (showPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPasscode) "Hide passcode" else "Show passcode"
                                )
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Confirm passcode field with show/hide
                    OutlinedTextField(
                        value = confirmPasscode,
                        onValueChange = { if (it.length <= 4) confirmPasscode = it.filter { c -> c.isDigit() } },
                        label = { Text("Confirm Passcode") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (showConfirmPasscode) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPasscode = !showConfirmPasscode }) {
                                Icon(
                                    imageVector = if (showConfirmPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPasscode) "Hide confirm passcode" else "Show confirm passcode"
                                )
                            }
                        }
                    )
                    
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            error ?: "", 
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.scale(1f + errorShake * 0.05f)
                        )
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
            
            // Pin input fields with animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.scale(1f + errorShake * 0.05f)
            ) {
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
                Text(
                    error ?: "", 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.scale(1f + errorShake * 0.05f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { viewModel.showForgotPasscodeDialog() }
            ) {
                Text("Forgot Passcode?")
            }
        }
    }

    // Handle haptic feedback for errors
    LaunchedEffect(error) {
        if (error != null) {
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
    }

    // Forgot Passcode Dialog
    if (showForgotPasscodeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideForgotPasscodeDialog() },
            title = { Text("Forgot Passcode") },
            text = { 
                Text(
                    "Warning: Resetting your passcode will permanently delete all your data including notes, budgets, and settings. This action cannot be undone.\n\nAre you sure you want to continue?"
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetPasscode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset & Delete All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideForgotPasscodeDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
} 