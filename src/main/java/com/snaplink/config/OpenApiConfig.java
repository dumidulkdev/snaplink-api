package com.snaplink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI snapLinkOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SnapLink API")
                        .description("""
                                SnapLink shortens long URLs into compact, trackable links with optional expiry, \
                                click limits, analytics, QR codes and abuse protection.""")
                );
    }
}