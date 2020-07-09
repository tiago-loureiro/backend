import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version: String by project
val logback_version: String by project
val jdbi_version: String by project

plugins {
	id("org.springframework.boot") version "2.3.1.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
}

group = "no.echokarriere"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")

	implementation("org.jdbi:jdbi3-bom:$jdbi_version")
	implementation("org.jdbi:jdbi3-sqlobject:$jdbi_version")
	implementation("org.jdbi:jdbi3-kotlin:$jdbi_version")
	implementation("org.jdbi:jdbi3-kotlin-sqlobject:$jdbi_version")
	implementation("org.jdbi:jdbi3-postgres:$jdbi_version")
	implementation("org.postgresql:postgresql:42.2.12.jre7")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}
