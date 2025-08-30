import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val abis = arrayOf("x86_64", "x86", "arm64-v8a", "armeabi-v7a")
val daemonDir = file("../daemon")

android {
    namespace = "com.cyruspyre.lemon"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cyruspyre.lemon"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    splits.abi {
        isEnable = true
        reset()
        include(*abis)
        isUniversalApk = true
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin.compilerOptions {
        freeCompilerArgs.addAll("-Xwhen-guards", "-Xnested-type-aliases")
        jvmTarget = JvmTarget.JVM_21
    }

    viewBinding.isEnabled = true
    packagingOptions.jniLibs.useLegacyPackaging = true
}

val daemonBuild = tasks.register("daemonBuild") {
    val targets = abis.map {
        var wtf = ""
        val tmp = when (it) {
            "armeabi-v7a" -> {
                wtf = "eabi"
                "armv7"
            }

            "arm64-v8a" -> "aarch64"
            "x86" -> "i686"
            else -> it
        }

        "$tmp-linux-android$wtf"
    }
    val cmd = mutableListOf("cargo", "build")
    val mode = if (tasks.asMap.contains("assembleRelease")) {
        cmd.add("-r")
        "release"
    } else "debug"

    for (target in targets) {
        cmd.add("--target")
        cmd.add(target)
    }

    val output = providers.exec {
        commandLine = cmd
        workingDir = daemonDir
        isIgnoreExitValue = true
    }
    val res = output.result.get()

    if (res.exitValue != 0) {
        System.err.println(cmd.joinToString(" "))
        System.err.print(output.standardError.asText.get())
        throw StopExecutionException()
    }

    for ((i, triple) in targets.withIndex()) {
        val from = file("../daemon/target/$triple/$mode/daemon")
        val target = file("src/main/jniLibs/${abis[i]}/libdaemon.so")

        from.copyTo(target, true)
    }
}
val daemonClean = tasks.register<Exec>("daemonClean") {
    commandLine("cargo", "clean")
    delete("src/main/jniLibs")
    workingDir = daemonDir
}

tasks.preBuild.dependsOn(daemonBuild)
tasks.clean.dependsOn(daemonClean)

dependencies {
    implementation(libs.flexbox)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
}