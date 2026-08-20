plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "2.3.8"
}

android {
    val deploymentApiUrl = providers.gradleProperty("MOODPRINT_API_BASE_URL")
    val releaseSyncEnabled = deploymentApiUrl.isPresent
    namespace = "com.moodprint.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.moodprint.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true; buildConfig = true }

    buildTypes {
        getByName("debug") {
            // 기본값은 로컬 서버이며, 팀 배포용 APK는 Gradle 속성으로 실제 서버를 주입한다.
            buildConfigField("String", "MOODPRINT_API_BASE_URL", "\"${deploymentApiUrl.orNull ?: "http://10.0.2.2:8080/api/v1"}\"")
            buildConfigField("boolean", "MOODPRINT_REMOTE_SYNC_ENABLED", "true")
        }
        getByName("release") {
            // A missing production URL disables networking while retaining the Room outbox.
            buildConfigField("String", "MOODPRINT_API_BASE_URL", "\"${deploymentApiUrl.orNull ?: "https://disabled.invalid/api/v1"}\"")
            buildConfigField("boolean", "MOODPRINT_REMOTE_SYNC_ENABLED", releaseSyncEnabled.toString())
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.room:room-testing:2.8.4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
