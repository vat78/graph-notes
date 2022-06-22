plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
}

version = "1.0-SNAPSHOT"

val quarkusVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))

    implementation(enforcedPlatform("io.quarkus:quarkus-bom:$quarkusVersion"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-smallrye-graphql")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-resteasy-jackson")

    implementation("io.quarkiverse.neo4j:quarkus-neo4j:1.3.0")

    implementation("io.quarkus:quarkus-liquibase")
    implementation("org.liquibase.ext:liquibase-neo4j:4.9.1")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-jacoco")
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}