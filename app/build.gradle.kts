plugins { id("com.android.application") }

android {
    namespace = "de.luna.assistant"
    compileSdk = 35
    defaultConfig {
        applicationId = "de.luna.assistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.1.0"
        val backendUrl = providers.gradleProperty("LUNA_BACKEND_URL").orElse("").get()
            .replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "LUNA_BACKEND_URL", "\"$backendUrl\"")
    }
    buildFeatures { buildConfig = true }
}
