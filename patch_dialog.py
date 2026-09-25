import re

with open('app/src/main/java/com/asistenpetani/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# I will find the AlertDialog for showMicInfoDialog
start_idx = content.find("    if (showMicInfoDialog) {")
end_idx = content.find("    if (showMicResultDialog) {")

if start_idx != -1 and end_idx != -1:
    new_dialog = """    if (showMicInfoDialog) {
        AlertDialog(
            onDismissRequest = { showMicInfoDialog = false },
            title = {
                Text("Asisten Suara", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Ketuk 'Mulai Bicara' lalu sampaikan pertanyaan atau kendala pertanian Anda dengan jelas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMicInfoDialog = false
                        chatController.startListening(
                            onResult = { text ->
                                recognizedSpeechText = text
                                isSpeechNoisyOrEmpty = text.trim().length < 3
                                showMicResultDialog = true
                            },
                            onError = { errorMsg ->
                                android.widget.Toast.makeText(context, "Error: $errorMsg", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AspekGreen)
                ) {
                    Text("Mulai Bicara", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicInfoDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
"""
    content = content[:start_idx] + new_dialog + content[end_idx:]
    with open('app/src/main/java/com/asistenpetani/ui/screens/DashboardScreen.kt', 'w') as f:
        f.write(content)
