package com.sicimed.configuracion;

import org.springframework.stereotype.Component;

@Component
public class EstadoSemilla {

    private volatile boolean listo = false;

    public void marcarListo() { this.listo = true; }

    public boolean estaListo() { return this.listo; }
}
