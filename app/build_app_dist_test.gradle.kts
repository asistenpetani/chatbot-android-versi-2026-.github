plugins {
    id("com.android.application")
    id("com.google.firebase.appdistribution")
}
android {
    buildTypes {
        release {
            firebaseAppDistribution {
                artifactType = "APK"
            }
        }
    }
}
