package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.PosicionTareaHistorico;
import com.soprint.seguimiento_mensajeros.model.Tarea;
import com.soprint.seguimiento_mensajeros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosicionTareaHistoricoRepository extends JpaRepository<PosicionTareaHistorico, Long> {

    List<PosicionTareaHistorico> findByTareaOrderByFechaRegistroAsc(Tarea tarea);

    List<PosicionTareaHistorico> findByMensajeroOrderByFechaRegistroDesc(Usuario mensajero);
}
