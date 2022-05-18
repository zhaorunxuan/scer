plugins {
    java
    scala
}

group = "me.zhaorx"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.typelevel:shapeless3-deriving_3:3.0.4")
    // implementation("com.chuusai:shapeless_2.13:2.3.9")
    implementation("org.scala-lang:scala3-library_3:3.1.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}