package com.soprint.seguimiento_mensajeros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarea;

    // ===== RELACIONES PRINCIPALES =====

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado_tarea", nullable = false)
    private EstadoTarea estadoTarea;

    // ===== USUARIOS RELACIONADOS =====
    // Asesor que crea la tarea
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asesor_crea", nullable = false)
    private Usuario asesorCrea;

    // Mensajero asignado (puede iniciar en null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mensajero_asignado")
    private Usuario mensajeroAsignado;

    // Supervisor que asigna (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_supervisor_asigna")
    private Usuario supervisorAsigna;

    // ===== CAMPOS DE LA TAREA =====

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    // Tiempo total (por ejemplo en minutos)
    @Column(name = "tiempo_total")
    private Long tiempoTotal;

    @Column(name = "comentario", length = 1000)
    private String comentario;

    @Column(name = "observacion", length = 2000)
    private String observacion;

    @Column(name = "proceso", length = 255)
    private String proceso;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    // SOLO NÚMEROS DE 4 DÍGITOS: "0001" ..."9999"
    @Pattern(regexp = "\\d{4}", message = "El código debe tener exactamente 4 dígitos numéricos")
    @Column(name = "codigo", length = 4, nullable = false)
    private String codigo;

}
