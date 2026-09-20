package com.sicimed.util;

import com.sicimed.model.Usuario;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tokens simples en memoria para el MVP (estilo académico). */
public final class TokenStore {

    private static final Map<String, Usuario> TOKENS = new ConcurrentHashMap<>();

    private TokenStore() {}

    public static String issue(Usuario usuario) {
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        // copia sin password
        Usuario safe = new Usuario();
        safe.setId(usuario.getId());
        safe.setUsername(usuario.getUsername());
        safe.setNombreCompleto(usuario.getNombreCompleto());
        safe.setEmail(usuario.getEmail());
        safe.setRol(usuario.getRol());
        safe.setActivo(usuario.isActivo());
        TOKENS.put(token, safe);
        return token;
    }

    public static Usuario resolve(String token) {
        if (token == null || token.isBlank()) return null;
        return TOKENS.get(token);
    }

    public static void revoke(String token) {
        if (token != null) TOKENS.remove(token);
    }
}
