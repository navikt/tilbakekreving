plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("org.apache.commons:commons-csv:1.14.0")
}
