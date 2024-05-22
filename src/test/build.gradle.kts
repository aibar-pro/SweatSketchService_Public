//plugins {
//    kotlin("jvm") version "1.9.23"
//}
//
//group = "pro.aibar.sweatsketch"
//version = "0.1.0"
//
//repositories {
//    mavenCentral()
//}
//
//dependencies {
//    testImplementation(kotlin("test"))
//
//    // Testing
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.21")
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
//    testImplementation("com.h2database:h2:1.4.200")
//    testImplementation("org.jetbrains.exposed:exposed-core:0.39.2")
//    testImplementation("org.jetbrains.exposed:exposed-dao:0.39.2")
//    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.39.2")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
//    testImplementation("io.ktor:ktor-server-tests:1.6.7")
//    testImplementation("com.auth0:java-jwt:3.18.1")
//}
//
//tasks.test {
//    useJUnitPlatform()
//}
//kotlin {
//    jvmToolchain(17)
//}