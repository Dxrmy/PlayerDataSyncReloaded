// Nested :forge-versions:* are not root `subprojects { }` children.
subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}
