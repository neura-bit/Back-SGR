package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.DTO.TareaWebhookPayload;
import com.soprint.seguimiento_mensajeros.DTO.TareaFinalizadaWebhookPayload;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para enviar notificaciones a webhooks externos.
 */
@Service
public class WebhookService {

    private static final String WEBHOOK_URL = "https://n8n.srv1070869.hstgr.cloud/webhook/8b0b6048-78f7-45ee-b923-48d8ac2eb81d";
    private static final String WEBHOOK_TAREA_FINALIZADA_URL = "https://n8n.srv1070869.hstgr.cloud/webhook/notificacion-tarea-completada";

    private final RestTemplate restTemplate;

    public WebhookService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envía una notificación al webhook cuando se crea una tarea.
     * Se ejecuta de forma asíncrona para no bloquear el flujo principal.
     * 
     * @param payload El payload con la información de la tarea y cliente
     */
    @Async
    public void enviarNotificacionTareaCreada(TareaWebhookPayload payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TareaWebhookPayload> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(WEBHOOK_URL, request, String.class);

            System.out.println("Webhook enviado exitosamente para tarea: " + payload.getCodigoTarea());
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al enviar webhook: " + e.getMessage());
        }
    }

    /**
     * Envía una notificación al webhook cuando se finaliza una tarea.
     * Notifica al asesor creador con los detalles de la finalización.
     * Se ejecuta de forma asíncrona para no bloquear el flujo principal.
     * 
     * @param payload El payload con la información de la tarea finalizada
     */
    @Async
    public void enviarNotificacionTareaFinalizada(TareaFinalizadaWebhookPayload payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TareaFinalizadaWebhookPayload> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(WEBHOOK_TAREA_FINALIZADA_URL, request, String.class);

            System.out.println("Webhook de tarea finalizada enviado exitosamente para tarea: " + payload.getIdTarea()
                    + " al correo: " + payload.getCorreoAsesor());
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al enviar webhook de tarea finalizada: " + e.getMessage());
        }
    }
}
