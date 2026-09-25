import re

with open('app/src/main/java/com/asistenpetani/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Add parameter to SettingsScreen
content = content.replace("fun SettingsScreen(\n    viewModel: AppViewModel,\n    onBackClick: () -> Unit,\n    onLogoutClick: () -> Unit\n)", "fun SettingsScreen(\n    viewModel: AppViewModel,\n    onBackClick: () -> Unit,\n    onLogoutClick: () -> Unit,\n    onBukaLangganan: () -> Unit = {}\n)")

button_code = """
                // Subscription button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBukaLangganan),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFFBEB))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                "Berlangganan Bebas Iklan",
                                fontWeight = FontWeight.Bold,
                                color = MidnightBlue,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                }
"""

content = content.replace("// Settings button 1", button_code + "\n                // Settings button 1")
content = content.replace("import androidx.compose.material.icons.filled.Warning", "import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Star")


with open('app/src/main/java/com/asistenpetani/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)

