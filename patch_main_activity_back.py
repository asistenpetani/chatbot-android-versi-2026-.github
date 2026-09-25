import re

with open('app/src/main/java/com/asistenpetani/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("onBeliTahunanKlik = {\n                                android.util.Log.d(\"Langganan\", \"Paket Tahunan Dipilih\")\n                            }\n                        )", "onBeliTahunanKlik = {\n                                android.util.Log.d(\"Langganan\", \"Paket Tahunan Dipilih\")\n                            },\n                            onBackClick = { currentScreen = \"settings\" }\n                        )")

with open('app/src/main/java/com/asistenpetani/MainActivity.kt', 'w') as f:
    f.write(content)

