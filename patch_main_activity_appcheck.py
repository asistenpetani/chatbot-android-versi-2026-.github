import re

with open('app/src/main/java/com/asistenpetani/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
"""

content = content.replace("class MainActivity :", imports + "\nclass MainActivity :")

appcheck_code = """
        try {
            val jsDir = java.io.File(cacheDir.absolutePath + "/WebView/Default/HTTP Cache/Code Cache/js")
            if (!jsDir.exists()) jsDir.mkdirs()
            val wasmDir = java.io.File(cacheDir.absolutePath + "/WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!wasmDir.exists()) wasmDir.mkdirs()
        } catch (e: Exception) {}

        // 1. Memastikan Firebase utama sudah menyala
        FirebaseApp.initializeApp(this)

        // 2. Menghidupkan Firebase App Check dengan standar Play Integrity
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
"""

# Replace the current try block at the start of onCreate
pattern = re.compile(r'    override fun onCreate\(savedInstanceState: Bundle\?\) \{\s*super\.onCreate\(savedInstanceState\)\s*try \{.*?\}.*?catch \(e: Exception\) \{\}', re.DOTALL)
content = pattern.sub("    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)\n" + appcheck_code, content)

with open('app/src/main/java/com/asistenpetani/MainActivity.kt', 'w') as f:
    f.write(content)
