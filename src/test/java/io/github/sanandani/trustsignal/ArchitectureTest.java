package io.github.sanandani.trustsignal;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "io.github.sanandani.trustsignal")
class ArchitectureTest {
    @ArchTest static final ArchRule modelHasNoProjectDependencies = noClasses().that().resideInAPackage("..model..").should().dependOnClassesThat().resideInAnyPackage("..score..", "..verify..", "..example..");
    @ArchTest static final ArchRule coreHasNoWallClockCalls = noClasses().should().callMethod(java.time.Instant.class, "now").orShould().callMethod(System.class, "currentTimeMillis");
}
