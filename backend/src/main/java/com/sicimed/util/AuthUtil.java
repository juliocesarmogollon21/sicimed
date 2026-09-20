package com.sicimed.util;

import com.sicimed.model.Rol;
import com.sicimed.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;

public final class AuthUtil {
    private AuthUtil() {}

    public static Usuario requireUser(HttpServletRequest req) {
        Object u = req.getAttribute("usuario");
        if (!(u instanceof Usuario user)) {
            throw new SecurityException("No autenticado");
        }
        return user;
    }

    public static Usuario requireRole(HttpServletRequest req, Rol... roles) {
        Usuario user = requireUser(req);
        for (Rol r : roles) {
            if (user.getRol() == r) return user;
        }
        throw new SecurityException("Acceso denegado para rol " + user.getRol());
    }
}
