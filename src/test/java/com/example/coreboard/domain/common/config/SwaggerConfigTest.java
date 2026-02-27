package com.example.coreboard.domain.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {
    @Test
    @DisplayName("스웨거_OpenAPI에_BearerAuth_스키마와_SecurityRequirement가_등록된다")
    void openAPI_registers_bearerAuth_scheme_and_requirement() {
        SwaggerConfig config = new SwaggerConfig();

        OpenAPI openAPI = config.openAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey("bearerAuth");
        assertThat(openAPI.getSecurity())
                .isNotNull()
                .anySatisfy(req -> assertThat(req).containsKey("bearerAuth"));
    }
}