package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.PosicionMensajeroActual;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosicionMensajeroActualRepository extends JpaRepository<PosicionMensajeroActual, Long> {

    Optional<PosicionMensajeroActual> findByMensajero(Usuario mensajero);

    List<PosicionMensajeroActual> findByFechaUltimaActualizacionAfter(java.time.LocalDateTime fecha);
}
