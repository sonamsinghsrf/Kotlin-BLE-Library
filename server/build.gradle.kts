plugins {
    alias(libs.plugins.nordic.library)
    alias(libs.plugins.nordic.kotlin)
    alias(libs.plugins.nordic.nexus)
}

group = "no.nordicsemi.android.kotlin.ble"

nordicNexusPublishing {
    POM_ARTIFACT_ID = "server"
    POM_NAME = "Nordic Kotlin library for BLE server side."

    POM_DESCRIPTION = "Nordic Android Kotlin BLE library"
    POM_URL = "https://github.com/NordicPlayground/Kotlin-BLE-Library"
    POM_SCM_URL = "https://github.com/NordicPlayground/Kotlin-BLE-Library"
    POM_SCM_CONNECTION = "scm:git@github.com:NordicPlayground/Kotlin-BLE-Library.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:NordicPlayground/Kotlin-BLE-Library.git"

    POM_DEVELOPER_ID = "syzi"
    POM_DEVELOPER_NAME = "Sylwester Zieliński"
    POM_DEVELOPER_EMAIL = "sylwester.zielinski@nordicsemi.no"
}

android {
    namespace = "no.nordicsemi.android.kotlin.ble.server.main"
}

dependencies {
    api(project(":core"))
    api(project(":logger"))

    implementation(project(":server-api"))
    implementation(project(":mock"))
    implementation(project(":server-mock"))
    implementation(project(":server-android"))

    implementation(libs.androidx.annotation)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.kotlin.junit)
    implementation(libs.nordic.core)
}
