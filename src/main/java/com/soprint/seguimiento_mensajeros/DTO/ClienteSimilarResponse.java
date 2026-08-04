package com.soprint.seguimiento_mensajeros.DTO;

import com.soprint.seguimiento_mensajeros.service.ClienteSimilitud;

import java.util.List;

/**
 * Cliente ya registrado que se parece al que se esta por crear, junto con las
 * razones concretas del parecido.
 *
 * El objetivo es que el asesor pueda revisar el cliente existente antes de
 * decidir si de verdad necesita crear uno nuevo: no bloquea la creacion, solo
 * la informa. Los duplicados de esta base nacieron precisamente de crear a
 * ciegas cuando el buscador no encontraba al cliente.
 */
public class ClienteSimilarResponse {

    private ClienteResponse cliente;
    private int puntaje;
    private List<String> motivos;
    /** Cantidad de tareas ya registradas contra este cliente. */
    private long tareas;

    public static ClienteSimilarResponse from(ClienteSimilitud.Coincidencia coincidencia, long tareas) {
        ClienteSimilarResponse dto = new ClienteSimilarResponse();
        dto.cliente = ClienteResponse.fromEntity(coincidencia.getCliente());
        dto.puntaje = coincidencia.getPuntaje();
        dto.motivos = coincidencia.getMotivos();
        dto.tareas = tareas;
        return dto;
    }

    public ClienteResponse getCliente() {
        return cliente;
    }

    public void setCliente(ClienteResponse cliente) {
        this.cliente = cliente;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    public List<String> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<String> motivos) {
        this.motivos = motivos;
    }

    public long getTareas() {
        return tareas;
    }

    public void setTareas(long tareas) {
        this.tareas = tareas;
    }
}
