package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.EstadoTareaOficina;
import com.soprint.seguimiento_mensajeros.model.TareaOficina;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import com.soprint.seguimiento_mensajeros.repository.TareaOficinaRepository;
import com.soprint.seguimiento_mensajeros.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TareaOficinaService implements ITareaOficinaService {

    @Autowired
    private TareaOficinaRepository tareaOficinaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FCMService fcmService;

    @Override
    public List<TareaOficina> findAll() {
        return tareaOficinaRepository.findAll();
    }

    @Override
    public Optional<TareaOficina> findById(Long id) {
        return tareaOficinaRepository.findById(id);
    }

    @Override
    public TareaOficina create(TareaOficina tareaOficina) {
        tareaOficina.setFechaCreacion(LocalDateTime.now());
        tareaOficina.setEstado(EstadoTareaOficina.PENDIENTE);

        // Cargar los usuarios completos de la BD antes de guardar para el JSON
        if (tareaOficina.getCreador() != null && tareaOficina.getCreador().getIdUsuario() != null) {
            Usuario creador = usuarioRepository.findById(tareaOficina.getCreador().getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Creador no encontrado"));
            tareaOficina.setCreador(creador);
        }

        Usuario responsableCompleto = null;
        if (tareaOficina.getResponsable() != null && tareaOficina.getResponsable().getIdUsuario() != null) {
            responsableCompleto = usuarioRepository.findById(tareaOficina.getResponsable().getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Responsable no encontrado"));
            tareaOficina.setResponsable(responsableCompleto);
        }

        TareaOficina savedTarea = tareaOficinaRepository.save(tareaOficina);

        // Notificar al responsable
        if (responsableCompleto != null && responsableCompleto.getFcmToken() != null
                && !responsableCompleto.getFcmToken().isEmpty()) {
            fcmService.sendNotification(
                    responsableCompleto.getFcmToken(),
                    "Nueva Tarea Creada",
                    "Te han asignado la tarea: " + savedTarea.getNombre());
        }

        return savedTarea;
    }

    @Override
    public TareaOficina update(Long id, TareaOficina tareaDetails) {
        return tareaOficinaRepository.findById(id).map(tarea -> {
            tarea.setNombre(tareaDetails.getNombre());
            tarea.setDescripcion(tareaDetails.getDescripcion());
            tarea.setFechaLimite(tareaDetails.getFechaLimite());
            tarea.setTipo(tareaDetails.getTipo());

            // Si cambia el responsable, notificar al nuevo
            if (tareaDetails.getResponsable() != null &&
                    !tarea.getResponsable().getIdUsuario().equals(tareaDetails.getResponsable().getIdUsuario())) {

                tarea.setResponsable(tareaDetails.getResponsable());

                Usuario nuevoResponsable = usuarioRepository.findById(tareaDetails.getResponsable().getIdUsuario())
                        .orElse(null);
                if (nuevoResponsable != null && nuevoResponsable.getFcmToken() != null
                        && !nuevoResponsable.getFcmToken().isEmpty()) {
                    fcmService.sendNotification(
                            nuevoResponsable.getFcmToken(),
                            "Tarea Reasignada",
                            "Se te ha reasignado la tarea: " + tarea.getNombre());
                }
            }

            return tareaOficinaRepository.save(tarea);
        }).orElseThrow(() -> new RuntimeException("Tarea de oficina no encontrada con el id " + id));
    }

    @Override
    public void delete(Long id) {
        tareaOficinaRepository.deleteById(id);
    }

    @Override
    public List<TareaOficina> findByResponsable(Long idResponsable) {
        return tareaOficinaRepository.findByResponsableIdUsuario(idResponsable);
    }

    @Override
    public Page<TareaOficina> findByResponsable(Long idResponsable, Pageable pageable) {
        return tareaOficinaRepository.findByResponsableIdUsuario(idResponsable, pageable);
    }

    @Override
    public List<TareaOficina> findByCreador(Long idCreador) {
        return tareaOficinaRepository.findByCreadorIdUsuario(idCreador);
    }

    @Override
    public Page<TareaOficina> findByCreador(Long idCreador, Pageable pageable) {
        return tareaOficinaRepository.findByCreadorIdUsuario(idCreador, pageable);
    }

    @Override
    public TareaOficina completarTarea(Long idTarea) {
        return tareaOficinaRepository.findById(idTarea).map(tarea -> {
            tarea.setEstado(EstadoTareaOficina.COMPLETADA);

            TareaOficina savedTarea = tareaOficinaRepository.save(tarea);

            // Notificar al creador que se completó
            Usuario creador = usuarioRepository.findById(savedTarea.getCreador().getIdUsuario()).orElse(null);
            if (creador != null && creador.getFcmToken() != null && !creador.getFcmToken().isEmpty()) {
                fcmService.sendNotification(
                        creador.getFcmToken(),
                        "Tarea Completada",
                        "El responsable ha completado la tarea: " + savedTarea.getNombre());
            }

            return savedTarea;
        }).orElseThrow(() -> new RuntimeException("Tarea de oficina no encontrada con el id " + idTarea));
    }
}
