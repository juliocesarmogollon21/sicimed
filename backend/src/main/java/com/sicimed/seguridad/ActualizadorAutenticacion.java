package com.sicimed.seguridad;

import com.sicimed.excepcion.AccesoDenegadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ActualizadorAutenticacion {

    public SicimedPrincipal actual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof SicimedPrincipal principal) {
            return principal;
        }
        throw new AccesoDenegadoException("No autenticado");
    }
}
