plugins {
    java
    id("maven-publish")
}

group = "net.swofty"
version = "3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation(project(":packer"))
    implementation("net.minestom:minestom-snapshots:807a887fbd") {
        exclude(group = "org.jboss.shrinkwrap.resolver", module = "shrinkwrap-resolver-depchain")
    }
}