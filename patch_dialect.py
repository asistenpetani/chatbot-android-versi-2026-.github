import re

with open('app/src/main/java/com/asistenpetani/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace the block that formats the quota message
pattern = r"val errorText = when \{\s+loc\.contains\(\"jawa barat\"\).*?else -> \"Maaf, kuota asisten habis\.\"\s+\}"
replacement = """val errorText = com.asistenpetani.domain.util.DialectHelper.getFallbackQuotaMessage(farmerLocation)"""
content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/asistenpetani/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
