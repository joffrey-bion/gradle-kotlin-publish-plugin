plugins {
    id("com.gradle.develocity") version "3.18.2"
}

rootProject.name = "gradle-kotlin-publish-plugin"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = false // bad for CI, and not critical for local runs
    }
}
