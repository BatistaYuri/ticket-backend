package br.com.yuri.ticketbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME =
            "bearerAuth";

    @Bean
    OpenAPI ticketBackendOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Ticket Backend API")
                                .description(
                                        "API for ticket queue management"
                                )
                                .version("1.0.0")
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }

    @Bean
    OpenApiCustomizer managerSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach(
                    (path, pathItem) -> {
                        if (!path.startsWith("/api/manager/")) {
                            return;
                        }

                        pathItem.readOperations().forEach(
                                operation ->
                                        operation.addSecurityItem(
                                                new SecurityRequirement()
                                                        .addList(
                                                                SECURITY_SCHEME_NAME
                                                        )
                                        )
                        );
                    }
            );
        };
    }
}