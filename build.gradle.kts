plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.metaorta"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.modelmapper:modelmapper:3.2.1")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.kafka:spring-kafka:3.3.1")
    implementation ("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.auth0:java-jwt:4.4.0")

    implementation("org.apache.kafka:kafka_2.13:3.9.0")
    implementation ("org.json:json:20210307")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.0")
    implementation("org.modelmapper:modelmapper:3.2.2")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
