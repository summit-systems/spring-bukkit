plugins {
    id("spring-bukkit.shared")
    id("spring-bukkit.publish")
}

dependencies {
    compileOnly(libs.spigot)

    api(projects.springBukkitCore)
    api(libs.spring.data.jpa)
    api(libs.hibernate.core)
    implementation(libs.hikaricp)

    testImplementation(libs.spigot)
    testImplementation(libs.spring.test)
}