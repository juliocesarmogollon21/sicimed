package com.sicimed.repositorio;

import com.sicimed.modelo.Rol;
import com.sicimed.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long>,
        JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND u.rol IN :roles ORDER BY u.nombreCompleto")
    List<Usuario> findActivosPorRoles(@Param("roles") List<Rol> roles);

    @Query("SELECT u FROM Usuario u WHERE (:activo IS NULL OR u.activo = :activo) ORDER BY u.nombreCompleto")
    List<Usuario> findFiltrando(@Param("activo") Boolean activo);
}
