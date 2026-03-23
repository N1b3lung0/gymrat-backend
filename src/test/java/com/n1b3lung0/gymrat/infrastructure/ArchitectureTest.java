package com.n1b3lung0.gymrat.infrastructure;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit architecture rules enforcing the hexagonal / clean-architecture
 * constraints for the GymRat backend.
 *
 * <p>Uses {@link ClassFileImporter#importPackages(String...)} so that domain
 * and application classes — packaged as JARs in the multi-module Gradle build —
 * are also scanned.  {@code @AnalyzeClasses} alone only finds classes in
 * directories, not in dependency JARs.
 */
@DisplayName("Architecture rules")
class ArchitectureTest {

    private static final String ROOT_PKG           = "com.n1b3lung0.gymrat";
    private static final String DOMAIN_PKG         = "com.n1b3lung0.gymrat.domain..";
    private static final String APPLICATION_PKG    = "com.n1b3lung0.gymrat.application..";
    private static final String INFRASTRUCTURE_PKG = "com.n1b3lung0.gymrat.infrastructure..";

    private static final String SPRING_PKG    = "org.springframework..";
    private static final String JPA_PKG       = "jakarta.persistence..";
    private static final String HIBERNATE_PKG = "org.hibernate..";
    private static final String LOMBOK_PKG    = "lombok..";

    /** Shared, lazily-built class set (expensive to build, cheap to reuse). */
    private static JavaClasses ALL_CLASSES;

    @BeforeAll
    static void importClasses() {
        // importPackages scans the full classpath — both class directories and JARs —
        // for classes that belong to the given root package.
        // All three Gradle modules (domain, application, infrastructure) end up here.
        ALL_CLASSES = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(
                        "com.n1b3lung0.gymrat.domain",
                        "com.n1b3lung0.gymrat.application",
                        "com.n1b3lung0.gymrat.infrastructure"
                );
    }

    // -------------------------------------------------------------------------
    // Layer architecture — cross-layer access rules
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Layered architecture")
    class LayerArchitecture {

        @Test
        @DisplayName("domain must not access application layer")
        void domainMustNotAccessApplication() {
            noClasses().that().resideInAPackage(DOMAIN_PKG)
                    .should().dependOnClassesThat().resideInAPackage(APPLICATION_PKG)
                    .as("Domain must not access Application")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("domain must not access infrastructure layer")
        void domainMustNotAccessInfrastructure() {
            noClasses().that().resideInAPackage(DOMAIN_PKG)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PKG)
                    .as("Domain must not access Infrastructure")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("application must not access infrastructure layer")
        void applicationMustNotAccessInfrastructure() {
            noClasses().that().resideInAPackage(APPLICATION_PKG)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PKG)
                    .as("Application must not access Infrastructure")
                    .check(ALL_CLASSES);
        }
    }

    // -------------------------------------------------------------------------
    // Domain isolation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Domain isolation")
    class DomainIsolation {

        @Test
        @DisplayName("domain must not depend on Spring")
        void domainDoesNotUseSpring() {
            noClasses().that().resideInAPackage(DOMAIN_PKG)
                    .should().dependOnClassesThat().resideInAPackage(SPRING_PKG)
                    .as("Domain must not depend on Spring")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("domain must not depend on JPA")
        void domainDoesNotUseJpa() {
            noClasses().that().resideInAPackage(DOMAIN_PKG)
                    .should().dependOnClassesThat().resideInAPackage(JPA_PKG)
                    .as("Domain must not depend on JPA (jakarta.persistence)")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("domain must not depend on Hibernate")
        void domainDoesNotUseHibernate() {
            noClasses().that().resideInAPackage(DOMAIN_PKG)
                    .should().dependOnClassesThat().resideInAPackage(HIBERNATE_PKG)
                    .as("Domain must not depend on Hibernate")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("domain must not depend on Lombok")
        void domainDoesNotUseLombok() {
            noClasses().that().resideInAPackage(DOMAIN_PKG)
                    .should().dependOnClassesThat().resideInAPackage(LOMBOK_PKG)
                    .as("Domain must not depend on Lombok")
                    .check(ALL_CLASSES);
        }
    }

    // -------------------------------------------------------------------------
    // Application isolation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Application isolation")
    class ApplicationIsolation {

        @Test
        @DisplayName("application must not depend on Spring")
        void applicationDoesNotUseSpring() {
            noClasses().that().resideInAPackage(APPLICATION_PKG)
                    .should().dependOnClassesThat().resideInAPackage(SPRING_PKG)
                    .as("Application layer must not depend on Spring")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("application must not depend on infrastructure")
        void applicationDoesNotUseInfrastructure() {
            noClasses().that().resideInAPackage(APPLICATION_PKG)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PKG)
                    .as("Application layer must not depend on Infrastructure")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("application must not depend on JPA")
        void applicationDoesNotUseJpa() {
            noClasses().that().resideInAPackage(APPLICATION_PKG)
                    .should().dependOnClassesThat().resideInAPackage(JPA_PKG)
                    .as("Application layer must not depend on JPA")
                    .check(ALL_CLASSES);
        }
    }

    // -------------------------------------------------------------------------
    // Spring annotations confined to infrastructure
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Spring annotations in infrastructure only")
    class SpringAnnotations {

        @Test
        @DisplayName("@Component, @Service, @Repository and @RestController only in infrastructure")
        void springAnnotationsOnlyInInfrastructure() {
            classes().that()
                    .areAnnotatedWith("org.springframework.stereotype.Component")
                    .or().areAnnotatedWith("org.springframework.stereotype.Service")
                    .or().areAnnotatedWith("org.springframework.stereotype.Repository")
                    .or().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().resideInAPackage(INFRASTRUCTURE_PKG)
                    .as("Spring stereotype annotations must only appear in the infrastructure layer")
                    .check(ALL_CLASSES);
        }
    }

    // -------------------------------------------------------------------------
    // Naming conventions
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Naming conventions")
    class NamingConventions {

        @Test
        @DisplayName("handlers reside in application.command or application.query")
        void handlersInCorrectPackage() {
            classes().that().haveSimpleNameEndingWith("Handler")
                    .and().resideInAPackage(APPLICATION_PKG)
                    .should().resideInAPackage("com.n1b3lung0.gymrat.application.command..")
                    .orShould().resideInAPackage("com.n1b3lung0.gymrat.application.query..")
                    .as("Application handlers must reside in application.command or application.query")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("UseCase interfaces reside in application.port.input")
        void useCasePortsInCorrectPackage() {
            classes().that().haveSimpleNameEndingWith("UseCase")
                    .should().resideInAPackage("com.n1b3lung0.gymrat.application.port.input..")
                    .as("UseCase interfaces must reside in application.port.input")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("REST controllers reside in infrastructure.adapter.input.rest")
        void controllersInCorrectPackage() {
            classes().that().haveSimpleNameEndingWith("Controller")
                    .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().resideInAPackage("com.n1b3lung0.gymrat.infrastructure.adapter.input.rest")
                    .as("REST controllers must reside in infrastructure.adapter.input.rest")
                    .check(ALL_CLASSES);
        }

        @Test
        @DisplayName("JPA adapters reside in infrastructure.adapter.output.persistence")
        void jpaAdaptersInCorrectPackage() {
            classes().that().haveSimpleNameEndingWith("JpaAdapter")
                    .should().resideInAPackage("com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence")
                    .as("JPA adapters must reside in infrastructure.adapter.output.persistence")
                    .check(ALL_CLASSES);
        }
    }

    // -------------------------------------------------------------------------
    // Controller dependencies
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Controller dependencies")
    class ControllerDependencies {

        @Test
        @DisplayName("controllers must not depend on concrete Handler classes")
        void controllersDontDependOnHandlers() {
            noClasses().that()
                    .resideInAPackage("com.n1b3lung0.gymrat.infrastructure.adapter.input.rest")
                    .and().haveSimpleNameEndingWith("Controller")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.n1b3lung0.gymrat.application.command..")
                    .orShould().dependOnClassesThat()
                    .resideInAPackage("com.n1b3lung0.gymrat.application.query..")
                    .as("Controllers must not depend on concrete Handler classes — only on UseCase ports")
                    .check(ALL_CLASSES);
        }
    }
}
