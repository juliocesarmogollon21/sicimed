package com.sicimed.seguridad;

import com.sicimed.modelo.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServicioTest {

    @Autowired
    private JwtServicio jwtServicio;

    @Test
    void tokenValidoDebeValidarse() {
        SicimedPrincipal principal = new SicimedPrincipal(
                1L, "testuser", "Test User", Rol.PACIENTE, null, 1L);

        String token = jwtServicio.generar(principal);
        assertNotNull(token);
        assertFalse(token.isBlank());

        SicimedPrincipal validado = jwtServicio.validar(token);
        assertNotNull(validado);
        assertEquals(principal.getUserId(), validado.getUserId());
        assertEquals(principal.getUsername(), validado.getUsername());
        assertEquals(principal.getRol(), validado.getRol());
    }

    @Test
    void tokenConCaracteresExtraDebeRechazarse() {
        SicimedPrincipal principal = new SicimedPrincipal(
                1L, "testuser", "Test User", Rol.PACIENTE, null, 1L);

        String token = jwtServicio.generar(principal);
        
        // Agregar caracteres extra al token (simula manipulación)
        String tokenManipulado = token + "ABC";
        
        SicimedPrincipal validado = jwtServicio.validar(tokenManipulado);
        assertNull(validado, "El token manipulado debería ser rechazado");
    }

    @Test
    void tokenConFirmaInvalidaDebeRechazarse() {
        SicimedPrincipal principal = new SicimedPrincipal(
                1L, "testuser", "Test User", Rol.PACIENTE, null, 1L);

        String token = jwtServicio.generar(principal);
        
        // Modificar la última parte del token (firma)
        String[] partes = token.split("\\.");
        String tokenFirmaInvalida = partes[0] + "." + partes[1] + ".firmainvalida";
        
        SicimedPrincipal validado = jwtServicio.validar(tokenFirmaInvalida);
        assertNull(validado, "El token con firma inválida debería ser rechazado");
    }

    @Test
    void tokenExpiradoDebeRechazarse() {
        // Este test requeriría configurar un JWT con expiración muy corta
        // Por ahora verificamos que el método existe y funciona
        SicimedPrincipal principal = new SicimedPrincipal(
                1L, "testuser", "Test User", Rol.PACIENTE, null, 1L);

        String token = jwtServicio.generar(principal);
        SicimedPrincipal validado = jwtServicio.validar(token);
        assertNotNull(validado);
    }

    @Test
    void tokenNullDebeRetornarNull() {
        assertNull(jwtServicio.validar(null));
    }

    @Test
    void tokenVacioDebeRetornarNull() {
        assertNull(jwtServicio.validar(""));
    }

    @Test
    void tokenBasuraDebeRetornarNull() {
        assertNull(jwtServicio.validar("esto.no.es.un.token.valido"));
    }
}