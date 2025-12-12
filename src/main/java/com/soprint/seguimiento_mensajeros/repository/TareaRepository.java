package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByMensajeroAsignadoIdUsuario(Long idMensajero);

    // Método para obtener la tarea con el código más alto (para autogenerar el
    // siguiente)
    Optional<Tarea> findTopByOrderByCodigoDesc();
}
