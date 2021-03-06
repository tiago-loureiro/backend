import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.ForcedType

val argonVersion: String by project
val arrowVersion: String by project
val detektVersion: String by project
val flywayVersion: String by project
val graphqlKotlinVersion: String by project
val graphqlScalarsVersion: String by project
val graphqlVersion: String by project
val hikariVersion: String by project
val jooqVersion: String by project
val jooqPluginVersion: String by project
val jsonPathVersion: String by project
val junitVersion: String by project
val kotlinLoggingVersion: String by project
val kotlinVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val postgresVersion: String by project

plugins {
    jacoco
    application
    kotlin("jvm")
    kotlin("kapt")
    id("org.flywaydb.flyway")
    id("org.jlleitschuh.gradle.ktlint")
    id("nu.studer.jooq")
    id("org.sonarqube")
    id("io.gitlab.arturbosch.detekt")
}

group = "no.echokarriere"
version = "0.0.1-SNAPSHOT"

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "11"
        // This might unexpectedly break our code in the future, fingers crossed
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
    }
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}

application {
    mainClass.set("no.echokarriere.ApplicationKt")
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://dl.bintray.com/arrow-kt/arrow-kt/") }
}

dependencies {
    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor", "ktor-server-core", ktorVersion)
    implementation("io.ktor", "ktor-server-host-common", ktorVersion)
    implementation("io.ktor", "ktor-server-sessions", ktorVersion)
    implementation("io.ktor", "ktor-jackson", ktorVersion)
    implementation("io.ktor", "ktor-auth", ktorVersion)
    implementation("io.ktor", "ktor-auth-jwt", ktorVersion)

    implementation("de.mkammerer", "argon2-jvm", argonVersion)

    implementation("io.arrow-kt", "arrow-core", arrowVersion)
    implementation("io.arrow-kt", "arrow-syntax", arrowVersion)
    kapt("io.arrow-kt", "arrow-meta", arrowVersion)

    implementation("org.jooq", "jooq", jooqVersion)
    implementation("org.flywaydb", "flyway-core", flywayVersion)
    implementation("com.zaxxer", "HikariCP", hikariVersion)
    implementation("org.postgresql", "postgresql", postgresVersion)
    jooqGenerator("org.postgresql", "postgresql", postgresVersion)

    implementation("com.graphql-java", "graphql-java", graphqlVersion)
    implementation("com.expediagroup", "graphql-kotlin-schema-generator", graphqlKotlinVersion)
    implementation("com.graphql-java", "graphql-java-extended-scalars", graphqlScalarsVersion)

    implementation("io.github.microutils", "kotlin-logging", kotlinLoggingVersion)
    implementation("ch.qos.logback", "logback-classic", logbackVersion)

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
    testImplementation("io.ktor", "ktor-server-tests", ktorVersion)
    testImplementation("io.rest-assured", "json-path", jsonPathVersion)

    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", detektVersion)
}

sourceSets {
    val flyway by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
    main {
        output.dir(flyway.output)
    }
}

flyway {
    url = (System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/echokarriere")
    user = (System.getenv("DB_USER") ?: "karriere")
    password = (System.getenv("DB_PASSWORD") ?: "password")
    locations = arrayOf("filesystem:src/main/resources/db/migrations")
}

tasks.flywayMigrate { dependsOn("flywayClasses") }
tasks.withType<Test> {
    useJUnitPlatform()
    failFast = true
    systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showStackTraces = true
        showCauses = true
    }
    finalizedBy(tasks.jacocoTestReport)
}

jooq {
    version.set(jooqVersion)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)
    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = (System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/echokarriere")
                    user = (System.getenv("DB_USER") ?: "karriere")
                    password = (System.getenv("DB_PASSWORD") ?: "password")
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.addAll(
                            arrayOf(
                                ForcedType()
                                    .withIncludeTypes("userType")
                                    .withUserType("no.echokarriere.user.UserType")
                                    .withConverter("no.echokarriere.user.UserTypeConverter")
                                    .withEnumConverter(true)
                            ).toList()
                        )
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "no.echokarriere"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    dependsOn(tasks.flywayMigrate)
    inputs.files(fileTree("src/main/resources/db/migrations"))
        .withPropertyName("migrations")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    allInputsDeclared.set(true)
    outputs.cacheIf { true }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.isEnabled = true
        xml.destination = File("$buildDir/reports/jacoco/test/jacoco.xml")
        html.isEnabled = true
    }
}

tasks.sonarqube {
    dependsOn(tasks.test)
}

sonarqube {
    properties {
        property("sonar.projectKey", "echo-karriere_backend")
        property("sonar.organization", "echo-karriere")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "$rootDir/src/main/kotlin")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/test/jacoco.xml")
        property("sonar.kotlin.detekt.reportPaths", "$buildDir/reports/detekt/detekt.xml")
    }
}

detekt {
    toolVersion = detektVersion
    config = files(".detekt.yml")
    input = files("src/main/kotlin")
    parallel = true
    buildUponDefaultConfig = true
    autoCorrect = true
}
