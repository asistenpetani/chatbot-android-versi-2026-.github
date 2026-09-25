import re

with open('app/src/main/java/com/asistenpetani/MainActivity.kt', 'r') as f:
    content = f.read()

# Pass onBukaLangganan to SettingsScreen
content = content.replace("SettingsScreen(\n                            viewModel = viewModel,\n                            onBackClick = { currentScreen = \"home\" },\n                            onLogoutClick = {\n                                authViewModel.signOut()\n                                viewModel.logout()\n                                currentScreen = \"home\"\n                            }\n                        )", "SettingsScreen(\n                            viewModel = viewModel,\n                            onBackClick = { currentScreen = \"home\" },\n                            onLogoutClick = {\n                                authViewModel.signOut()\n                                viewModel.logout()\n                                currentScreen = \"home\"\n                            },\n                            onBukaLangganan = { currentScreen = \"langganan\" }\n                        )")

# Add "langganan" case
langganan_case = """
                    "langganan" -> {
                        com.asistenpetani.ui.screens.LayarLangganan(
                            onBeliBulananKlik = {
                                android.util.Log.d("Langganan", "Paket Bulanan Dipilih")
                            },
                            onBeliTahunanKlik = {
                                android.util.Log.d("Langganan", "Paket Tahunan Dipilih")
                            }
                        )
                    }
"""

content = content.replace("\"premium_field\" -> {", langganan_case + "\n                    \"premium_field\" -> {")

with open('app/src/main/java/com/asistenpetani/MainActivity.kt', 'w') as f:
    f.write(content)

