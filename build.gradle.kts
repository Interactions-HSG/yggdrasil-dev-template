import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
  mavenCentral()
}

val vertxVersion = "4.5.1"
val junitVersion = "5.3.2"

dependencies {
  implementation("io.vertx:vertx-core:$vertxVersion")
  implementation("org.hyperagents:yggdrasil:0.0-SNAPSHOT")

  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("io.vertx:vertx-web-client:$vertxVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

application {
  mainClass = "io.vertx.core.Launcher"
}

val mainVerticleName = "org.hyperagents.yggdrasil.MainVerticle"

tasks {
  named<ShadowJar>("shadowJar") {
    manifest {
      attributes(mapOf("Main-Verticle" to mainVerticleName))
    }
    mergeServiceFiles {
      include("META-INF/services/io.vertx.core.spi.VerticleFactory")
    }
  }

  named<JavaExec>("run") {
    args = mutableListOf("run", mainVerticleName, "--launcher-class=${application.mainClass.get()}",
      "-conf=conf/config.json")
  }

  compileJava {
    options.compilerArgs.addAll(listOf("-parameters"))
  }

  test {
    useJUnitPlatform()
  }
}
