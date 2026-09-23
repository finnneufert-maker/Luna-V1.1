plugins { id("com.android.application") }

android {
    namespace = "de.luna.assistant"
    compileSdk = 35
    defaultConfig {
        applicationId = "de.luna.assistant.v18"
        minSdk = 26
        targetSdk = 35
        // Keep applicationId stable forever: Android preserves private data only across
        // updates of the same package.
        versionCode = 13
        versionName = "1.9.0"
        val backendUrl = providers.gradleProperty("LUNA_BACKEND_URL").orElse("").get()
            .replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "LUNA_BACKEND_URL", "\"$backendUrl\"")
    }
    buildFeatures { buildConfig = true }
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("LUNA_KEYSTORE_PATH") ?: "luna-release.jks")
            storePassword = System.getenv("LUNA_KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("LUNA_KEY_ALIAS") ?: "luna-release"
            keyPassword = System.getenv("LUNA_KEY_PASSWORD") ?: ""
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
}
