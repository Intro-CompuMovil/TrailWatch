plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.trailwatch"
    compileSdk = 34  // Actualizado a 34

    defaultConfig {
        applicationId = "com.example.trailwatch"
        minSdk = 26  // Actualizado de 21 a 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8  // Asegúrate de que coincide con tu configuración
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"  // Asegúrate de que coincide con las opciones de compilación
    }

    buildFeatures {
        viewBinding = true  // Si estás utilizando View Binding
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // OSMDroid
    implementation("org.osmdroid:osmdroid-android:6.1.12")

    // Firebase In-App Messaging
    implementation("com.google.firebase:firebase-inappmessaging-display:20.3.2")

    // Guava
    implementation("com.google.guava:guava:31.0.1-android")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // CameraX
    val cameraxVersion = "1.1.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Dependencias de testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
