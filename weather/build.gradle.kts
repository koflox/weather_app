import java.util.*

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id(libs.plugins.android.library.get().pluginId)
    alias(libs.plugins.kotlin.android)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.android.navigation.safeargs.get().pluginId)
}

fun String.toProperties() = Properties().apply {
    rootProject.file(this@toProperties).run {
        if (exists())
            load(inputStream())
        else
            println("Warning: ${this@toProperties} file is absent")
    }
}

val apiKeys = "api_key.properties".toProperties()

android {
    namespace = "com.koflox.weather"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        val keyOpenWeatherApi = apiKeys["openWeatherApiKey"] ?: throw IllegalArgumentException("Missing openWeatherApiKey")
        debug {
            buildConfigField("String", "API_KEY_OPEN_WEATHER_MAP", "$keyOpenWeatherApi")
        }
        release {
            buildConfigField("String", "API_KEY_OPEN_WEATHER_MAP", "$keyOpenWeatherApi")
            isMinifyEnabled = false
            setProguardFiles(listOf("proguard-android-optimize.txt", "proguard-rules.pro"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeKotlinCompilerExtension.get()
    }
}

dependencies {
    implementation(project(":common-ui"))
    implementation(project(":common-android-res"))
    implementation(project(":common-jvm-util"))

    implementation(libs.android.lifecycle.viewmodel.ktx)
    implementation(libs.android.core.ktx)
    implementation(libs.app.compat)
    implementation(libs.constraint.layout)
    implementation(libs.kotlin.coroutines)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.interceptor)
    implementation(libs.koin)
    implementation(libs.koin.android)
    implementation(libs.glide)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling.preview)
    implementation(libs.compose.constraintlayout)
    implementation(libs.coil.compose)
    debugImplementation(libs.compose.tooling)
}
