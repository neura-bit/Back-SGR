package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.PosicionMensajeroResponse;
import com.soprint.seguimiento_mensajeros.DTO.PosicionRequest;

import java.util.List;

public interface TrackingService {

    void registrarPosicion(PosicionRequest request);

    List<PosicionMensajeroResponse> obtenerPosicionesActuales();
}
