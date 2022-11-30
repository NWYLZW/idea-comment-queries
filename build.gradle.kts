plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.10.0"
}

val javaVersion = "17"

group = "yij.ie"
version = "2.0.0"

repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("test"))
    implementation(kotlin("test-junit"))
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.3")
    // https://jetbrains.org/intellij/sdk/docs/products/webstorm.html
    type.set("IU")

    plugins.set(listOf(
        "JavaScriptLanguage",
    ))
}

tasks {
    test {
        useJUnitPlatform()
    }
    buildSearchableOptions {
        enabled = false
    }
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = javaVersion
    }

    patchPluginXml {
        sinceBuild.set("222.4345.14")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
