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

    private static final String WEBHOOK_OFICINA_URL = "https://n8n.srv1070869.hstgr.cloud/webhook/notificacion-tarea";

    private final RestTemplate restTemplate;

    public WebhookService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envía una notificación webhook cuando se asigna o reasigna una tarea de oficina.
     */
    @Async
    public void enviarNotificacionOficina(com.soprint.seguimiento_mensajeros.DTO.NotificacionOficinaWebhookPayload payload) {
        try {
            // WhatsApp no permite saltos de línea, tabs ni +4 espacios en las variables
            // de una plantilla. Saneamos los campos de texto libre antes de enviar.
            payload.setNombreTarea(sanitizarParametroWhatsapp(payload.getNombreTarea()));
            payload.setDescripcionTarea(sanitizarParametroWhatsapp(payload.getDescripcionTarea()));
            payload.setNombreResponsable(sanitizarParametroWhatsapp(payload.getNombreResponsable()));
            payload.setNombreCreador(sanitizarParametroWhatsapp(payload.getNombreCreador()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<com.soprint.seguimiento_mensajeros.DTO.NotificacionOficinaWebhookPayload> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(WEBHOOK_OFICINA_URL, request, String.class);

            System.out.println("Webhook de oficina enviado exitosamente para tarea: " + payload.getNombreTarea());
        } catch (Exception e) {
            System.err.println("Error al enviar webhook de oficina: " + e.getMessage());
        }
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

    /**
     * Limpia un texto para que pueda usarse de forma segura como parámetro tanto
     * en el body JSON de n8n como en una plantilla de WhatsApp. Neutraliza:
     *
     * <ul>
     *   <li>Saltos de línea, retornos de carro y tabulaciones -> un espacio.
     *       (WhatsApp Cloud API rechaza con #132018 las variables con \n/\t).</li>
     *   <li>Comillas dobles -> comilla simple. Una " sin escapar rompe el JSON
     *       que n8n construye a mano ("JSON parameter needs to be valid JSON").</li>
     *   <li>Barra invertida -> barra normal. Un \ suelto también invalida el JSON.</li>
     *   <li>Espacios repetidos -> un solo espacio (WhatsApp prohíbe +4 seguidos).</li>
     * </ul>
     *
     * @param texto El texto original (puede ser null)
     * @return El texto saneado en una sola línea, o cadena vacía si era null
     */
    private static String sanitizarParametroWhatsapp(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replaceAll("[\\r\\n\\t]+", " ")
                .replace('"', '\'')
                .replace('\\', '/')
                .replaceAll(" {2,}", " ")
                .trim();
    }
}
