package com.n1b3lung0.gymrat.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Exposes API metadata and a {@code bearerAuth} security scheme placeholder
 * ready to be activated when Spring Security JWT support is added in Step 76.
 *
 * <p>Swagger UI: {@code http://localhost:8080/swagger-ui.html}<br>
 * OpenAPI JSON: {@code http://localhost:8080/v3/api-docs}
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gymRatOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GymRat API")
                        .version("v1")
                        .description("""
                                REST API for the GymRat workout tracking application.

                                Manage **exercises**, **workouts**, **exercise sessions** and **series sets** \
                                following a clean Hexagonal Architecture with CQRS and DDD principles.
                                """)
                        .contact(new Contact()
                                .name("GymRat Team")
                                .url("https://github.com/n1b3lung0/gymrat"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token — will be enforced once Spring Security is enabled (Step 76)")));
    }
}

