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
    }
}