#!/bin/bash
cat << 'INNER_EOF' >> app/src/main/java/com/asistenpetani/ui/screens/LoginScreen.kt

    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = { Text("Login Pemilik Aplikasi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = emailAdmin,
                        onValueChange = { emailAdmin = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sandiAdmin,
                        onValueChange = { sandiAdmin = it },
                        label = { Text("Kata Sandi") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showAdminDialog = false
                    onAdminLoginClick(emailAdmin, sandiAdmin)
                }) {
                    Text("Masuk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
INNER_EOF
