dependencies {
    compileOnly(libs.spigot)

    api(projects.serialization)

    api(libs.kotlinx.serialization.json)
    api(libs.kotlin.reflect)

    testImplementation(libs.paper)
    testImplementation(libs.spring.test)
    testImplementation(libs.mockbukkit)
}

projectGrapher {
    group = "serialization"
}