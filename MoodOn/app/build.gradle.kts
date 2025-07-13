plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.moodon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moodon"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"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\""
        )


        buildFeatures {
            compose = true
            buildConfig = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.11"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = "17"
        }

        packaging {
            resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependencies {
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
        implementation("androidx.startup:startup-runtime:1.1.1")

        // Compose
        implementation("androidx.compose.ui:ui:1.5.3")
        implementation("androidx.compose.material3:material3:1.2.0")
        implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
        debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
        implementation("androidx.activity:activity-compose:1.8.2")
        implementation("androidx.navigation:navigation-compose:2.7.7")
        implementation("androidx.compose.material:material-icons-extended")

        // Hilt
        implementation("com.google.dagger:hilt-android:2.51.1")
        ksp("com.google.dagger:hilt-compiler:2.46.1") // Eğer KAPT yerine KSP ile çalışıyorsan

        implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

        // Room
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")
        ksp("androidx.room:room-compiler:2.6.1")

        // Retrofit
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")

        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
        implementation("com.google.firebase:firebase-auth-ktx")
        implementation("com.google.firebase:firebase-firestore-ktx")
        implementation("com.google.firebase:firebase-analytics-ktx")

        // Diğer
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
        implementation("io.coil-kt:coil-compose:2.4.0")
        implementation("com.google.android.gms:play-services-auth:20.7.0")

        // Test
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    }
}
