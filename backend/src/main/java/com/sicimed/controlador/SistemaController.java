package com.sicimed.controlador;

import com.sicimed.configuracion.EstadoSemilla;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sistema")
@SecurityRequirements
@Tag(name = "Sistema", description = "Estado del backend")
public class SistemaController {

    private final EstadoSemilla estadoSemilla;

    public SistemaController(EstadoSemilla estadoSemilla) {
        this.estadoSemilla = estadoSemilla;
    }

    @GetMapping("/estado")
    @Operation(summary = "Comprobar que el backend responde",
            description = "Devuelve preparado=false mientras la base de datos aun no termina de inicializarse")
    public Map<String, Object> estado() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("servicio", "sicimed-backend");
        respuesta.put("estado", "activo");
        respuesta.put("preparado", estadoSemilla.estaListo());
        respuesta.put("version", "1.0.0");
        respuesta.put("documentacion", "/swagger-ui.html");
        return respuesta;
    }
}
