import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.10"
    antlr
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testCompile("junit", "junit", "4.12")
    antlr("org.antlr:antlr4:4.8-1")
    implementation("org.antlr:antlr4-runtime:4.8-1")
}

tasks.getByName("compileJava").dependsOn("generateGrammarSource")
tasks.getByName("compileTestJava").dependsOn("generateGrammarSource")
tasks.getByName("compileKotlin").dependsOn("generateGrammarSource")
tasks.getByName("compileTestKotlin").dependsOn("generateGrammarSource")

tasks.compileKotlin {
    this.source(file("build/generated-src/antlr/main/"))
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "14"
}

// disable runtime nullable call and argument checks for improved performance - they're left in tests to catch early bugs
tasks.getByName<KotlinCompile>("compileKotlin").kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + listOf(
        "-progressive",
        "-Xjvm-default=enable",
        "-Xno-param-assertions",
        "-Xno-call-assertions",
        "-verbose"
    )
}
tasks.getByName<KotlinCompile>("compileTestKotlin").kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + listOf(
        "-progressive",
        "-Xjvm-default=enable",
        "-Xopt-in=kotlin.RequiresOptIn"
    )
}