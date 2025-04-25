plugins {
    id("com.gradle.develocity") version "4.0.1"
}

rootProject.name = "gradle-kotlin-publish-plugin"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = false // bad for CI, and not critical for local runs
    }
}
