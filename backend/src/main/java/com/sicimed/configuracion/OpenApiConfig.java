package com.sicimed.configuracion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_SEGURIDAD = "bearerAuth";

    @Bean
    public OpenAPI sicimedOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SICIMED API")
                        .version("1.0.0")
                        .description("""
                                API REST del Sistema Web Distribuido para la Gestion de Citas Medicas.

                                **Autenticacion:** `POST /api/auth/login` devuelve un token JWT.
                                En Swagger UI, pulsen *Authorize* y peguen el token (sin la palabra Bearer).

                                **Codigos de respuesta:** 200 OK, 201 Created, 400 Bad Request,
                                401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict,
                                500 Internal Server Error.
                                """)
                        .license(new License().name("Uso academico")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD))
                .components(new Components().addSecuritySchemes(ESQUEMA_SEGURIDAD,
                        new SecurityScheme()
                                .name(ESQUEMA_SEGURIDAD)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
