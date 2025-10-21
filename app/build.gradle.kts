plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "mx.aro.atizaapp_equipo1"
    compileSdk = 36

    defaultConfig {
        applicationId = "mx.aro.atizaapp_equipo1"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.android.gms:play-services-auth:20.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    // Coroutines / GMS
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui.unit)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)




    // zxing scanner
    implementation (libs.zxing.android.embedded)

    // Permisos
    implementation (libs.accompanist.permissions)


    // Agregar retrofit y json
    implementation(libs.retrofitLib)
    implementation(libs.gsonLib)

    // Agregar coil
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Dependencia de Google Play Services
    implementation(libs.play.services.code.scanner)


    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}