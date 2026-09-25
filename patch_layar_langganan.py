import re

with open('app/src/main/java/com/asistenpetani/ui/screens/LayarLangganan.kt', 'r') as f:
    content = f.read()

# Add onBackClick parameter
content = content.replace("fun LayarLangganan(\n    onBeliBulananKlik: () -> Unit,\n    onBeliTahunanKlik: () -> Unit\n)", "fun LayarLangganan(\n    onBeliBulananKlik: () -> Unit,\n    onBeliTahunanKlik: () -> Unit,\n    onBackClick: () -> Unit = {}\n)")

content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack")

back_button_code = """
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
        }
"""
content = content.replace("horizontalAlignment = Alignment.CenterHorizontally,\n        verticalArrangement = Arrangement.Center\n    ) {", "horizontalAlignment = Alignment.CenterHorizontally,\n        verticalArrangement = Arrangement.Center\n    ) {" + back_button_code)

with open('app/src/main/java/com/asistenpetani/ui/screens/LayarLangganan.kt', 'w') as f:
    f.write(content)

