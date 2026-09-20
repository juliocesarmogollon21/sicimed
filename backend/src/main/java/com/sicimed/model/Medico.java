package com.sicimed.model;

public class Medico {
    private Long id;
    private Long usuarioId;
    private String nombreCompleto;
    private Long especialidadId;
    private String especialidadNombre;
    private Long sedeId;
    private String sedeNombre;
    private String cmp;
    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public Long getEspecialidadId() { return especialidadId; }
    public void setEspecialidadId(Long especialidadId) { this.especialidadId = especialidadId; }
    public String getEspecialidadNombre() { return especialidadNombre; }
    public void setEspecialidadNombre(String especialidadNombre) { this.especialidadNombre = especialidadNombre; }
    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }
    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }
    public String getCmp() { return cmp; }
    public void setCmp(String cmp) { this.cmp = cmp; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
