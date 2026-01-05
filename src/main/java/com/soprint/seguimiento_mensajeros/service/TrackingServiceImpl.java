package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.PosicionMensajeroResponse;
import com.soprint.seguimiento_mensajeros.DTO.PosicionRequest;
import com.soprint.seguimiento_mensajeros.model.PosicionMensajeroActual;
import com.soprint.seguimiento_mensajeros.model.PosicionTareaHistorico;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.PosicionMensajeroActualRepository;
import com.soprint.seguimiento_mensajeros.repository.PosicionTareaHistoricoRepository;
import com.soprint.seguimiento_mensajeros.repository.TareaRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrackingServiceImpl implements TrackingService {

    private final PosicionMensajeroActualRepository posicionActualRepository;
    private final PosicionTareaHistoricoRepository historicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TareaRepository tareaRepository;

    public TrackingServiceImpl(PosicionMensajeroActualRepository posicionActualRepository,
                               PosicionTareaHistoricoRepository historicoRepository,
                               UsuarioRepository usuarioRepository,
                               TareaRepository tareaRepository) {
        this.posicionActualRepository = posicionActualRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tareaRepository = tareaRepository;
    }

    @Override
    @Transactional
    public void registrarPosicion(PosicionRequest request) {

        Usuario mensajero = usuarioRepository.findById(request.getIdMensajero())
                .orElseThrow(() -> new RuntimeException("Mensajero no encontrado"));

        Tarea tarea = tareaRepository.findById(request.getIdTarea())
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        LocalDateTime ahora = LocalDateTime.now();

        // 1) Guardar histórico
        PosicionTareaHistorico historico = new PosicionTareaHistorico();
        historico.setMensajero(mensajero);
        historico.setTarea(tarea);
        historico.setLatitud(request.getLatitud());
        historico.setLongitud(request.getLongitud());
        historico.setFechaRegistro(ahora);

        historicoRepository.save(historico);

        // 2) Actualizar posición actual del mensajero
        PosicionMensajeroActual actual = posicionActualRepository
                .findByMensajero(mensajero)
                .orElseGet(() -> {
                    PosicionMensajeroActual p = new PosicionMensajeroActual();
                    p.setMensajero(mensajero);
                    return p;
                });

        actual.setTareaActual(tarea);
        actual.setLatitud(request.getLatitud());
        actual.setLongitud(request.getLongitud());
        actual.setFechaUltimaActualizacion(ahora);

        posicionActualRepository.save(actual);
    }

    @Override
    public List<PosicionMensajeroResponse> obtenerPosicionesActuales() {
        List<PosicionMensajeroActual> posiciones = posicionActualRepository.findAll();

        return posiciones.stream()
                .map(pos -> {
                    Usuario m = pos.getMensajero();
                    Tarea t = pos.getTareaActual();

                    PosicionMensajeroResponse dto = new PosicionMensajeroResponse();
                    // OJO: aquí usa los getters reales de tus entidades
                    dto.setIdMensajero(m.getIdUsuario());              // si tu campo es idUsuario
                    dto.setNombreCompleto(m.getNombre() + " " + m.getApellido());
                    dto.setIdTareaActual(t != null ? t.getIdTarea() : null);
                    dto.setNombreTareaActual(t != null ? t.getNombre() : null);
                    dto.setLatitud(pos.getLatitud());
                    dto.setLongitud(pos.getLongitud());
                    dto.setFechaUltimaActualizacion(pos.getFechaUltimaActualizacion());
                    dto.setFotoPerfil(m.getFotoPerfil());

                    return dto;
                })
                .toList();
    }
}
