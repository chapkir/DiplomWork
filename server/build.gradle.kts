plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.0.1"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "--add-opens",
            "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED"
        )
    )
}

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter)
    runtimeOnly(libs.postgresql)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testRuntimeOnly(libs.junit.platform.launcher)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.spring.boot.starter.validation)
    compileOnly(libs.projectlombok.lombok)
    annotationProcessor(libs.projectlombok.lombok)
    implementation (libs.bucket4j.core)

    // зависимости для работы с HTTP и JSON
    implementation(libs.okhttp)
    implementation(libs.converter.gson.v290)
    implementation(libs.httpclient)
    implementation(libs.httpmime)


    // Кэширование с использованием Caffeine
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.caffeine)
}

tasks.test {
    useJUnitPlatform()
}