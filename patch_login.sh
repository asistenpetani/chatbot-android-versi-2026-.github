#!/bin/bash
sed -i '/var showPrivacyDialog/a \    var showAdminDialog by remember { mutableStateOf(false) }' app/src/main/java/com/asistenpetani/ui/screens/LoginScreen.kt
