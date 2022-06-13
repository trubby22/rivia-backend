import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    implementation("software.amazon.awssdk:url-connection-client:2.17.207")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.234"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.12.221")
    implementation("software.amazon.awssdk:dynamodb-enhanced:2.17.204")
    implementation("aws.sdk.kotlin:dynamodb:0.15.0")
}

tasks {
    val buildZip by creating(Zip::class) {
        from(compileKotlin)
        from(processResources)
        into("lib") {
            from(configurations.runtimeClasspath)
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
