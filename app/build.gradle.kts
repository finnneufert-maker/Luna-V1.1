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
}
