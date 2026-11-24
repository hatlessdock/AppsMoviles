package colegio.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "solicitudes")
public class SolicitudEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombreUsuario;
    private String motivoUso;
    private int puestoId;
    private int moduloId;
    private boolean requiereFlexibilidad;
    private String motivoFlexibilidad;
    private boolean tieneFotoAdjunta;
    private String estado;
    private String fechaReserva;
    private boolean esPrioritaria;
    private String documentoAdjunto;

    public SolicitudEntity() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getMotivoUso() { return motivoUso; }
    public void setMotivoUso(String motivoUso) { this.motivoUso = motivoUso; }
    public int getPuestoId() { return puestoId; }
    public void setPuestoId(int puestoId) { this.puestoId = puestoId; }
    public int getModuloId() { return moduloId; }
    public void setModuloId(int moduloId) { this.moduloId = moduloId; }
    public boolean isRequiereFlexibilidad() { return requiereFlexibilidad; }
    public void setRequiereFlexibilidad(boolean requiereFlexibilidad) { this.requiereFlexibilidad = requiereFlexibilidad; }
    public String getMotivoFlexibilidad() { return motivoFlexibilidad; }
    public void setMotivoFlexibilidad(String motivoFlexibilidad) { this.motivoFlexibilidad = motivoFlexibilidad; }
    public boolean isTieneFotoAdjunta() { return tieneFotoAdjunta; }
    public void setTieneFotoAdjunta(boolean tieneFotoAdjunta) { this.tieneFotoAdjunta = tieneFotoAdjunta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(String fechaReserva) { this.fechaReserva = fechaReserva; }
    public boolean isEsPrioritaria() { return esPrioritaria; }
    public void setEsPrioritaria(boolean esPrioritaria) { this.esPrioritaria = esPrioritaria; }
    public String getDocumentoAdjunto() { return documentoAdjunto; }
    public void setDocumentoAdjunto(String documentoAdjunto) { this.documentoAdjunto = documentoAdjunto; }
}