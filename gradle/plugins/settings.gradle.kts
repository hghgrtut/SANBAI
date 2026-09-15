dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        from(files("../libs.versions.toml"))
    }
}

include(":convention")

rootProject.name = "plugins"
