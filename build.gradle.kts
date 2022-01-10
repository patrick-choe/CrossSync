plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "io.github.patrick-choe"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    api(kotlin("stdlib"))
    api("io.lettuce:lettuce-core:6.1.5.RELEASE")

    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("io.github.waterfallmc:waterfall-api:1.18-R0.1-SNAPSHOT")
}

tasks {
    create<Task>("copyToServer") {
        dependsOn("copyToWaterfall", "copyToPaper")
    }

    create<Copy>("copyToWaterfall") {
        from(shadowJar)
        into("W:\\Servers\\waterfall\\plugins")
    }

    create<Copy>("copyToPaper") {
        from(jar)
        into("W:\\Servers\\waterfall\\servers\\plugins")
    }
}