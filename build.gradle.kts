plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    kotlin("plugin.compose") version "2.2.20"
    id("org.jetbrains.compose") version "1.7.3"
}

group = "com.wongwingchun"

version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(files("plugin-api-jvm-1.0.0.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Compose
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.materialIconsExtended)
}

tasks.jar {
    archiveFileName.set("plugin.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs/lib"))
}

tasks.register<Zip>("packagePlugin") {
    group = "build"
    description = "Packages the plugin into a .micyou-plugin.zip file"

    dependsOn(tasks.jar)
    dependsOn("copyDependencies")

    archiveFileName.set("SpeakerYou.micyou-plugin.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    // Add plugin.json
    from("src/main/resources/plugin.json")

    // Add compiled classes (rename to plugin.jar)
    from(tasks.jar.get().outputs.files) { rename { "plugin.jar" } }

    // Add dependencies
    from(layout.buildDirectory.dir("libs/lib")) { into("lib") }

    // Add icon if exists
    from("src/main/resources") { include("icon.png") }
}
