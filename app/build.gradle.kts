import java.util.Properties
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.appdistribution)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kotlin.serialization)
  // alias(libs.plugins.oss.licenses)
}

// 1. SOLUSI PARADOKS CI/CD: Secara dinamis buat google-services.json dummy agar kompilasi GitHub Actions CI/CD tidak crash jika file tersebut di-gitignore.
val googleServicesJson = file("google-services.json")
if (!googleServicesJson.exists()) {
    googleServicesJson.writeText("""
        {
          "project_info": {
            "project_number": "956692315956",
            "project_id": "asistenpetani-ai-app",
            "storage_bucket": "asistenpetani-ai-app.firebasestorage.app"
          },
          "client": [
            {
              "client_info": {
                "mobilesdk_app_id": "1:956692315956:android:c11e6b7f2d8f523cceef61",
                "android_client_info": {
                  "package_name": "com.asistenpetani"
                }
              },
              "oauth_client": [
                {
                  "client_id": "956692315956-ulavsgag68hun3tofvsbp3urk38tt2ab.apps.googleusercontent.com",
                  "client_type": 1,
                  "android_info": {
                    "package_name": "com.asistenpetani",
                    "certificate_hash": "95f4170207d034d56c2903a4697e879ee6b6e828"
                  }
                },
                {
                  "client_id": "956692315956-i3180tjq5tv5e2bjf21mt2js0tk975t4.apps.googleusercontent.com",
                  "client_type": 3
                }
              ],
              "api_key": [
                {
                  "current_key": "AIzaSyDkXVke6HbyzFbCLnLBuKqm21GAGGjV_Gc"
                }
              ],
              "services": {
                "appinvite_service": {
                  "other_platform_oauth_client": []
                }
              }
            }
          ],
          "configuration_version": "1"
        }
    """.trimIndent())
}

// Release version 1.0.0.9 with package com.asistenpetani
android {
  namespace = "com.asistenpetani"
  compileSdk = 36

  defaultConfig {
    // 2. WORKSPACE PRODUKSI APK: com.asistenpetani
    applicationId = "com.asistenpetani"
    minSdk = 28
    targetSdk = 36
    versionCode = 14
    versionName = "1.0.0.14"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3607117191604085~3335010000"
    manifestPlaceholders["MAPS_API_KEY"] = ""
  }

  signingConfigs {
    create("release") {
      val p12KeystorePath = "${rootDir}/app/keystore_aspek.p12"
      val primaryKeystorePath = "${rootDir}/app/my-upload-key.jks"
      val secondaryKeystorePath = "${rootDir}/my-upload-key.jks"
      val resolvedKeystorePath = when {
        file(p12KeystorePath).exists() -> p12KeystorePath
        file(primaryKeystorePath).exists() -> primaryKeystorePath
        file(secondaryKeystorePath).exists() -> secondaryKeystorePath
        else -> "${rootDir}/debug.keystore"
      }
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: resolvedKeystorePath
      storeFile = file(keystorePath)
      
      val isDefaultDebug = keystorePath.endsWith("debug.keystore")
      storePassword = System.getenv("STORE_PASSWORD") ?: "android"
      keyAlias = System.getenv("KEY_ALIAS") ?: if (isDefaultDebug) "androiddebugkey" else "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    debug {
      val appDistributionKey = file("${rootDir}/firebase-app-distribution-key.json")
      if (appDistributionKey.exists()) {
        firebaseAppDistribution {
          artifactType = "APK"
          serviceCredentialsFile = appDistributionKey.absolutePath
          groups = "internal-testers"
        }
      }
    }
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
      ndk {
        debugSymbolLevel = "FULL"
      }
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-metadata-version-check")
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

val envExampleFile = rootProject.file(".env.example")
if (!envExampleFile.exists()) {
  envExampleFile.writeText("# Environment Variables Example\nADMOB_APP_ID=ca-app-pub-3607117191604085~3335010000\nADMOB_PUBLISHER_ID=pub-3607117191604085\nADMOB_UNIT_ID=ca-app-pub-3607117191604085/6618132781\nADMOB_INTERSTITIAL_ID=ca-app-pub-3607117191604085/6618132781\nADMOB_REWARDED_ID=ca-app-pub-3607117191604085/6618132781\nADMOB_BANNER_ID=ca-app-pub-3607117191604085/6618132781\nMAPS_API_KEY=placeholder\nAPI_KEY_WEB_CLIENT=placeholder\nAsisten_Petani_Gemini_API_Key=placeholder\nGEMINI_API_KEY=placeholder\nGENERATIVE_LANGUAGE_API_KEY=placeholder\nGenerative_Language_API_Key=placeholder\nNODE_ENV=placeholder\nandroid_key=placeholder\napi_key_cuaca_asistenpetani=placeholder\njsonKeyaisgeminikey=placeholder\nsertifikas_webpush=placeholder\n")
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = if (rootProject.file(".env").exists()) ".env" else "secret.properties"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_URL")
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(libs.play.services.oss.licenses)
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.retrofit.kotlinx.serialization.converter)
  implementation(libs.androidx.work)
  implementation(libs.billing.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.appcheck.playintegrity)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.firebase.analytics)
  implementation(libs.play.services.ads)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.maps.compose)
  implementation(libs.maps.utils)
  implementation(libs.play.services.maps)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.ai.ondevice)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.storage)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.play.services.auth)
  implementation(libs.retrofit)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services.auth)
  implementation(libs.googleid)
  implementation(libs.play.integrity)
  implementation(libs.hilt.android)
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
  implementation(libs.play.services.oss.licenses)
  ksp(libs.hilt.compiler)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

// Optimization: Strictly optimize dependency caching properties in Gradle & Android Studio.
// This prevents Gradle from checking for library metadata/version updates on every sync/build,
// drastically reducing internet download volume and ensuring maximum cached reuse.
configurations.all {
  resolutionStrategy {
    cacheDynamicVersionsFor(30, "days") // Cache dynamic version targets for up to 30 days
    cacheChangingModulesFor(30, "days") // Cache changing modules (like SNAPSHOTs) for up to 30 days
  }
}
