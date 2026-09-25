import re

with open('app/src/main/java/com/asistenpetani/ui/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

# I will replace `iconRes = icon` with `iconName = icon, temp = "${response.temperature}°C", humidity = "${response.humidity}%", advice = response.advice`
# And for the error catch block: `iconName = "CloudOff", temp = "-", humidity = "-", advice = "Gagal terhubung."`

old_1 = """_weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = titleText,
                    description = response.advice, // Menggunakan nasihat dari Gemini di backend!
                    isWarning = warning,
                    iconRes = icon
                )"""

new_1 = """_weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = titleText,
                    description = response.advice, // Menggunakan nasihat dari Gemini di backend!
                    isWarning = warning,
                    iconName = icon,
                    temp = "${response.temperature}°C",
                    humidity = "${response.humidity}%",
                    advice = response.advice
                )"""

content = content.replace(old_1, new_1)


old_2 = """_weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = "Cuaca Tidak Diketahui",
                    description = "Gagal memuat cuaca dari sistem ASPEK (Cloud Run). Harap periksa koneksi internet.",
                    isWarning = true,
                    iconRes = "CloudOff"
                )"""

new_2 = """_weatherAlert.value = com.asistenpetani.domain.model.WeatherAlert(
                    title = "Cuaca Tidak Diketahui",
                    description = "Gagal memuat cuaca dari sistem ASPEK (Cloud Run). Harap periksa koneksi internet.",
                    isWarning = true,
                    iconName = "CloudOff",
                    temp = "-",
                    humidity = "-",
                    advice = "Sistem gagal terhubung."
                )"""

content = content.replace(old_2, new_2)

with open('app/src/main/java/com/asistenpetani/ui/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)

