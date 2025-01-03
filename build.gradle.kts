plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "me.rebekka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.5.15")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
    implementation("org.apache.jena:jena-core:5.0.0")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs = listOf("-Xss1g")
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}