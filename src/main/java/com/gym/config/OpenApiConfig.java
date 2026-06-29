package com.gym.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gymOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym CRM API")
                        .description("""
                                REST API for a Gym CRM system covering trainee and trainer registration,
                                profile management, trainer-trainee assignment, training scheduling, 
                                and training type reference data.
                                
                                Authentication: most endpoints require the `X-Username` and `X-Password` 
                                headers, validated against stored credentials on every call. Registration 
                                and the training types listing are the only endpoints that do not require 
                                authentication.
                                
                                Username and password are auto-generated at registration and cannot be 
                                changed except via the dedicated change-password endpoint.
                                """)
                        .version("v2.0")
                        .contact(new Contact()
                                .name("Gym CRM"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Username")
                                .description("Username header required for authenticated endpoints"))
                        .addSecuritySchemes("passwordAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Password")
                                .description("Password header required for authenticated endpoints")));
    }
}