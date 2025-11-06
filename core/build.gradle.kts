plugins {
    id("chestshop-database.library-conventions")
}

repositories {
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnlyApi(libs.paper)
}