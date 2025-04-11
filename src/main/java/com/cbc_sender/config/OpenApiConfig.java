package com.cbc_sender.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT authorization"
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**") // match all paths
                .addOperationCustomizer((operation, handlerMethod) -> {
                    // Add security requirements to all operations
                    operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                    return operation;
                })
                .build();
    }

    @Bean
    public Info apiInfo() {
        return new Info().title("Mail SenderEntity API")
                .version("1.0")
                .description("Mail SenderEntity API Documentation");
    }

}