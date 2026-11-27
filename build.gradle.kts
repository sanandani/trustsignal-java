plugins {
    `java-library`
    application
    jacoco
}

application { mainClass.set("io.github.sanandani.trustsignal.example.Main") }

group = "io.github.sanandani"
version = "0.1.0"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
    withJavadocJar()
    modularity.inferModulePath.set(true)
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation(libs.jqwik)
    testImplementation(libs.archunit)
}

val jmhSourceSet = sourceSets.create("jmh") {
        java.srcDir("src/jmh/java")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += output + compileClasspath
}

dependencies {
    add("jmhImplementation", libs.jmh.core)
    add("jmhAnnotationProcessor", libs.jmh.generator)
}

configurations.configureEach { resolutionStrategy.activateDependencyLocking() }
dependencyLocking { lockAllConfigurations() }

tasks.test { useJUnitPlatform(); finalizedBy(tasks.jacocoTestReport) }
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports { xml.required.set(true); html.required.set(true) }
}
tasks.jacocoTestCoverageVerification {
    violationRules { rule { limit { minimum = BigDecimal("0.85") } } }
}
tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.register<Exec>("buildSite") {
    group = "documentation"
    description = "Build the static site, rendered docs, and API Javadoc."
    dependsOn(tasks.javadoc)
    inputs.dir("site"); inputs.dir("docs"); inputs.file("gradle/site.mjs")
    outputs.dir(layout.buildDirectory.dir("site"))
    doFirst {
        val out = layout.buildDirectory.dir("site").get().asFile
        out.deleteRecursively(); out.mkdirs()
        copy { from("site"); into(out) }
        file("site/app.js").copyTo(File(out, "app.js"), overwrite = true)
        copy { from(tasks.javadoc); into(File(out, "api")) }
        file("site/404.html").copyTo(File(out, "docs/404.html"), overwrite = true)
    }
    doFirst { environment("PAGES_BASE_URL", System.getenv("PAGES_BASE_URL") ?: "/") }
    commandLine("node", "gradle/site.mjs", "docs", "build/site/docs", "site")
}
