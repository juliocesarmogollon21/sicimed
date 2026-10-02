package com.sicimed.seguridad;

import com.sicimed.modelo.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SicimedPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String nombreCompleto;
    private final Rol rol;
    private final Long medicoId;
    private final Long pacienteId;

    public SicimedPrincipal(Long userId, String username, String nombreCompleto,
                            Rol rol, Long medicoId, Long pacienteId) {
        this.userId = userId;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
    }

    public Long getUserId() { return userId; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Rol getRol() { return rol; }
    public Long getMedicoId() { return medicoId; }
    public Long getPacienteId() { return pacienteId; }

    public boolean esAdmin() { return rol == Rol.ADMIN; }
    public boolean esRecepcion() { return rol == Rol.RECEPCIONISTA; }
    public boolean esMedico() { return rol == Rol.MEDICO; }
    public boolean esPaciente() { return rol == Rol.PACIENTE; }

    public boolean esGestor() { return esAdmin() || esRecepcion(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
