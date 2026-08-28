package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Sucursal;
import com.soprint.seguimiento_mensajeros.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SucursalService implements ISucursalService {

    private final SucursalRepository sucursalRepository;

    public SucursalService(SucursalRepository sucursalRepository) {
        this.sucursalRepository = sucursalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sucursal> findAll() {
        return sucursalRepository.findByActivoTrue();
    }

    /**
     * Solo sucursales activas, igual que findAll: si se da de baja una
     * sucursal se deja de operar esa ciudad, así que no debe seguir
     * ofreciéndose al registrar clientes nuevos. Los clientes ya registrados
     * conservan su ciudad; la web la sigue mostrando aunque no esté en la
     * lista.
     *
     * `sucursal.ciudad` también es texto libre, así que se deduplica sin
     * distinguir mayúsculas ("Quito" y "quito" son una sola opción) y se
     * devuelve la forma tal como la escribió el administrador.
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findCiudades() {
        Map<String, String> porClave = new LinkedHashMap<>();
        for (Sucursal sucursal : sucursalRepository.findByActivoTrue()) {
            String ciudad = sucursal.getCiudad();
            if (ciudad == null || ciudad.trim().isEmpty()) {
                continue;
            }
            String limpia = ciudad.trim();
            porClave.putIfAbsent(limpia.toUpperCase(), limpia);
        }
        return porClave.values().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sucursal> findById(Long id) {
        return sucursalRepository.findById(id).filter(Sucursal::getActivo);
    }

    @Override
    public Sucursal create(Sucursal sucursal) {
        sucursal.setIdSucursal(null);
        sucursal.setActivo(true);
        return sucursalRepository.save(sucursal);
    }

    @Override
    public Sucursal update(Long id, Sucursal sucursal) {
        Sucursal existente = sucursalRepository.findById(id)
                .filter(Sucursal::getActivo)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada o inactiva con id: " + id));

        existente.setNombre(sucursal.getNombre());
        existente.setDireccion(sucursal.getDireccion());
        existente.setCiudad(sucursal.getCiudad());
        existente.setTelefono(sucursal.getTelefono());

        return sucursalRepository.save(existente);
    }

    @Override
    public void delete(Long id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con id: " + id));

        sucursal.setActivo(false);
        sucursalRepository.save(sucursal);
    }
}
