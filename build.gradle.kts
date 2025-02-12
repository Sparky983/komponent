import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("org.jetbrains.dokka") version "2.0.0"
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    js {
        browser {}
        compilerOptions {
            target = "es2015"
        }
    }
}

mavenPublishing {
    configure(KotlinMultiplatform(
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        sourcesJar = true
    ))
    signAllPublications()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        description = project.description
        url = "https://github.com/Sparky983/komponent"
        licenses {
            license {
                name = "MIT License"
                url = "https://www.opensource.org/licenses/mit-license"
            }
        }
        developers {
            developer {
                id = "Sparky983"
            }
        }
        scm {
            url = "https://github.com/Sparky983/komponent"
            connection = "scm:git:git://github.com/Sparky983/komponent.git"
            developerConnection = "scm:git:ssh://git@github.com/Sparky983/komponent.git"
        }
    }
}

tasks.register<Task>("generate") {
    description = "Generate node API"

    doLast {
        generate(file("src/jsMain/kotlin/me/sparky983/komponent/generated"))
    }
}