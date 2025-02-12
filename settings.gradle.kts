rootProject.name = "komponent"

includeBuild("gradle-plugin") {
    dependencySubstitution {
        substitute(module("me.sparky983.komponent:gradle-plugin:0.1.0")).using(project(":"))
    }
}
includeBuild("compiler")
