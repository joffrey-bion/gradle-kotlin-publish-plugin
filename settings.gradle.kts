plugins {
    id("com.gradle.enterprise") version "3.15.1"
}

rootProject.name = "gradle-kotlin-publish-plugin"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}
