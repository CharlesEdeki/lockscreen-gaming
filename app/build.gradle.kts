import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.lsg.lockarcade"
    compileSdk = 35
    defaultConfig {
        applicationId = "dev.lsg.lockarcade"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

dependencies {
    implementation(project(":core:engine"))
    implementation(project(":feature:lockscreen"))
    // Each game is its own module; remove a line to drop a game from the build.
    implementation(project(":games:snake"))
    implementation(project(":games:bricks"))
    implementation(project(":games:dodge"))
}
