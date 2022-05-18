plugins {
    scala
    java
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.zhaorx"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

application {
    mainClass.set("App")
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("io.netty:netty-all:4.1.75.Final")
    implementation("org.scala-lang:scala3-library_3:3.1.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

