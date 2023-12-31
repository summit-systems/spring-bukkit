rootProject.name = "spring-bukkit"

includeBuild("build-logic")
include("spring-bukkit-command")
include("spring-bukkit-core")
include("spring-bukkit-jpa")
include("spring-bukkit-coroutines")
include("spring-bukkit-starter")
include("spring-bukkit-support")
include("spring-bukkit-view")

pluginManagement {
    plugins {
        val kotlinVersion = extra["kotlin.version"].toString()
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.spring") version kotlinVersion apply false
        kotlin("plugin.jpa") version kotlinVersion apply false

        val springBootVersion = extra["spring.boot.version"].toString()
        id("org.springframework.boot") version springBootVersion apply false
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    versionCatalogs {
        create("libs") {
            library("spigot", "org.spigotmc:spigot-api:${extra["spigot.version"]}")
            library("paper", "io.papermc.paper:paper-api:${extra["spigot.version"]}")

            library("kotlin-stdlib", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${extra["kotlin.version"]}")
            library("kotlin-reflect", "org.jetbrains.kotlin:kotlin-reflect:${extra["kotlin.version"]}")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:${extra["kotlinx.coroutines.version"]}")

            library("spring-boot-autoconfigure", "org.springframework.boot:spring-boot-autoconfigure:${extra["spring.boot.version"]}")
            library("spring-aspects", "org.springframework:spring-aspects:${extra["spring.version"]}")
            library("spring-tx", "org.springframework:spring-tx:${extra["spring.version"]}")
            library("jakarta-annotation", "jakarta.annotation:jakarta.annotation-api:${extra["jakarta.annotation.version"]}")

            library("spring-data-jpa", "org.springframework.data:spring-data-jpa:${extra["spring.data.jpa.version"]}")
            library("hibernate-core", "org.hibernate.orm:hibernate-core:${extra["hibernate.version"]}")
            library("hikaricp", "com.zaxxer:HikariCP:${extra["hikaricp.version"]}")

            library("spring-test", "org.springframework.boot:spring-boot-starter-test:${extra["spring.boot.version"]}")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")