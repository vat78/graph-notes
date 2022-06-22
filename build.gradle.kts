allprojects {
    group = "ru.vat78.graph-notes"
}
version = "1.0-SNAPSHOT"

plugins {
    id("org.sonarqube") version "3.3"
}

subprojects{
    apply(plugin = "java")
    apply(plugin = "jacoco")

    tasks.named<Test>("test") {
        finalizedBy(tasks.getByName("jacocoTestReport")) // report is always generated after tests run
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn.add(tasks.getByName("test"))
        reports.xml.required.set(true)
        reports.html.required.set(true)
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/core/Storage.kt")
                }
            })
        )
    }
}