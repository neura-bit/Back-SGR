package com.soprint.seguimiento_mensajeros.repository;

import com.soprint.seguimiento_mensajeros.model.ArchivoAdjuntoOficina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivoAdjuntoOficinaRepository extends JpaRepository<ArchivoAdjuntoOficina, Long> {
    List<ArchivoAdjuntoOficina> findByTareaOficinaIdTareaOficina(Long idTareaOficina);
}
