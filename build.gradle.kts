plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "me.rebekka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.3.0")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.5.2")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    implementation("org.apache.jena:jena-core:5.0.0")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs = setOf("-Xss1g")
}