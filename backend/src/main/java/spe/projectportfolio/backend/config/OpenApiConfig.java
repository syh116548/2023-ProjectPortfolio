package spe.projectportfolio.backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                        .title("ProjectPortfolio")
                        .version("v1.0.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("Repository")
                        .url("https://github.com/spe-uob/2023-ProjectPortfolio"))
                ;

    }
}
