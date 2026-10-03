package com.sicimed.seguridad;

import com.sicimed.modelo.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtServicio {

    private static final Logger log = LoggerFactory.getLogger(JwtServicio.class);
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_NOMBRE = "nombre";
    private static final String CLAIM_MEDICO_ID = "medicoId";
    private static final String CLAIM_PACIENTE_ID = "pacienteId";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtServicio(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
        if (secret.startsWith("SICIMED-clave-de-desarrollo")) {
            log.warn("JWT_SECRET usa la clave de desarrollo. Define JWT_SECRET antes de desplegar.");
        }
    }

    public String generar(SicimedPrincipal principal) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim(CLAIM_USER_ID, principal.getUserId())
                .claim(CLAIM_ROL, principal.getRol().name())
                .claim(CLAIM_NOMBRE, principal.getNombreCompleto())
                .claim(CLAIM_MEDICO_ID, principal.getMedicoId())
                .claim(CLAIM_PACIENTE_ID, principal.getPacienteId())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    public SicimedPrincipal validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new SicimedPrincipal(
                    claims.get(CLAIM_USER_ID, Number.class).longValue(),
                    claims.getSubject(),
                    claims.get(CLAIM_NOMBRE, String.class),
                    Rol.valueOf(claims.get(CLAIM_ROL, String.class)),
                    claims.get(CLAIM_MEDICO_ID, Number.class) == null
                            ? null : claims.get(CLAIM_MEDICO_ID, Number.class).longValue(),
                    claims.get(CLAIM_PACIENTE_ID, Number.class) == null
                            ? null : claims.get(CLAIM_PACIENTE_ID, Number.class).longValue());
        } catch (Exception e) {
            log.debug("Token JWT rechazado: {}", e.getMessage());
            return null;
        }
    }
}
