plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.0.1"
    id("com.github.ben-manes.versions") version "0.50.0"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.isFork = true
    options.isIncremental = true
    options.isFailOnError = true
}

// Правильно добавляем JVM аргументы для запуска приложения
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    jvmArgs = listOf(
        "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED"
    )
}

// Также добавляем JVM аргументы для тестов
tasks.withType<Test> {
    jvmArgs = listOf(
        "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED"
    )
}

tasks.bootJar {
    exclude("**/dev-*/**")
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
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
    implementation(libs.bucket4j.core)

    implementation(libs.okhttp)
    implementation(libs.converter.gson.v290)
    implementation(libs.httpclient)
    implementation(libs.httpmime)

    implementation(libs.spring.boot.starter.cache)
    implementation(libs.caffeine)

    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // MapStruct for DTO mapping
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // RabbitMQ for async notification processing
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    implementation("com.drewnoakes:metadata-extractor:2.16.0")
    implementation("net.coobird:thumbnailator:0.4.8")
    implementation("org.sejda.imageio:webp-imageio:0.1.1")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

tasks.register("checkDependencies") {
    dependsOn("dependencyUpdates")
    doLast {
        println("Check completed")
    }
}