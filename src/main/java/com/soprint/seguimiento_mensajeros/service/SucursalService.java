package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Sucursal;
import com.soprint.seguimiento_mensajeros.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        return sucursalRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sucursal> findById(Long id) {
        return sucursalRepository.findById(id);
    }

    @Override
    public Sucursal create(Sucursal sucursal) {
        sucursal.setIdSucursal(null);
        return sucursalRepository.save(sucursal);
    }

    @Override
    public Sucursal update(Long id, Sucursal sucursal) {
        Sucursal existente = sucursalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con id: " + id));

        existente.setNombre(sucursal.getNombre());
        existente.setDireccion(sucursal.getDireccion());
        existente.setCiudad(sucursal.getCiudad());
        existente.setTelefono(sucursal.getTelefono());

        return sucursalRepository.save(existente);
    }

    @Override
    public void delete(Long id) {
        if (!sucursalRepository.existsById(id)) {
            throw new IllegalArgumentException("Sucursal no encontrada con id: " + id);
        }
        sucursalRepository.deleteById(id);
    }
}
