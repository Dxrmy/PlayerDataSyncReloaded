// Nested :versions:* projects are not direct children of the root, so root `subprojects { }` does not
// apply JavaCompile options here. Without an explicit --release, the Java 25 toolchain emits class file 69,
// which Paper 1.21.x's plugin remapper cannot read.
subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
