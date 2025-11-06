plugins {
    java
    `maven-publish`
}

repositories {
    mavenCentral()
}

val targetJavaVersion = 21

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

tasks {
    compileJava {
        options.release = targetJavaVersion
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    publishing {
        publications {
            create<MavenPublication>(project.name) {
                from(components["java"])
                pom {
                    developers {
                        developer {
                            id = "md5sha256"
                            name = "Andrew Wong"
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/MCCitiesNetwork/chestshop-database.git")
                        developerConnection.set("scm:git:ssh://github.com/MCCitiesNetwork/chestshop-database.git")
                        url.set("https://github.com/MCCitiesNetwork/chestshop-database")
                    }
                }
            }
        }
    }
}