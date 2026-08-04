package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos tomados de los duplicados reales que se encontraron en la base de
 * produccion. Cada test corresponde a un patron concreto que se observo.
 */
class ClienteSimilitudTest {

    private Cliente cliente(String nombre, String rucCi, String telefono,
                            String direccion, Double lat, Double lon) {
        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setRucCi(rucCi);
        c.setTelefono(telefono);
        c.setDireccion(direccion);
        c.setLatitud(lat);
        c.setLongitud(lon);
        return c;
    }

    @Test
    @DisplayName("Cedula y RUC de la misma persona se reconocen como iguales")
    void cedulaYRucSonLaMismaIdentidad() {
        // Caso real: dos registros del mismo cliente, uno con cedula y otro
        // con el RUC de persona natural (cedula + 001), 31 tareas repartidas
        Cliente a = cliente("Richard Macas", "1719146399001", "0963879454", null, null, null);
        Cliente b = cliente("RICHARD STEVEN MACAS", "1719146399", "0963879454", null, null, null);

        ClienteSimilitud.Coincidencia c = ClienteSimilitud.comparar(a, b);

        assertNotNull(c, "deberia detectarse como el mismo cliente");
        assertTrue(c.getPuntaje() >= ClienteSimilitud.UMBRAL_AVISO);
        assertTrue(c.getMotivos().stream().anyMatch(m -> m.contains("Misma identidad")),
                "debe explicar que es la misma identidad con distinto formato");
    }

    @Test
    @DisplayName("Un error de tipeo se detecta por la ubicacion")
    void mismaUbicacionDetectaErrorDeTipeo() {
        // Caso real: CIOBALANZE vs BIOBALANZE, una letra de diferencia, en las
        // mismas coordenadas exactas. Ningun campo de texto los relaciona.
        Cliente a = cliente("CIOBALANZE CONSULTORA AMBIENTAL", null, null, null, -2.89310, -79.00315);
        Cliente b = cliente("BIOBALANZE CONSULTORA AMBIENTAL", null, null, null, -2.89310, -79.00315);

        ClienteSimilitud.Coincidencia c = ClienteSimilitud.comparar(a, b);

        assertNotNull(c, "la ubicacion identica deberia levantar el aviso");
        assertTrue(c.getMotivos().stream().anyMatch(m -> m.contains("Misma ubicación")));
    }

    @Test
    @DisplayName("Tildes y mayusculas no impiden reconocer el mismo nombre")
    void nombreConTildesYMayusculas() {
        Cliente a = cliente("TORRES LLERENA MARÍA RAQUEL", null, null, null, null, null);
        Cliente b = cliente("torres llerena maria raquel", null, null, null, null, null);

        assertNotNull(ClienteSimilitud.comparar(a, b));
    }

    @Test
    @DisplayName("Una sucursal en otra direccion NO se marca como duplicado")
    void sucursalNoEsDuplicado() {
        // Caso real: LUXVIAJES tiene dos oficinas distintas en Guayaquil bajo
        // el mismo RUC. Aqui coinciden identidad y nombre, asi que el aviso
        // aparece igual, pero es correcto: quien decide es la persona.
        Cliente a = cliente("LUXVIAJES AGENCIA DE VIAJES S.A.S.", "0993380696001", null,
                "W4WC+45G, Guayaquil 090101, Ecuador", -2.19000, -79.88000);
        Cliente b = cliente("LUXVIAJES AGENCIA DE VIAJES S.A.S.", "0993380696001", null,
                "Edif. PLATINIUM 1, Piso 8 oficina 801, Guayaquil", -2.16000, -79.90000);

        ClienteSimilitud.Coincidencia c = ClienteSimilitud.comparar(a, b);

        assertNotNull(c, "se avisa porque comparten RUC y nombre");
        assertTrue(c.getMotivos().stream().noneMatch(m -> m.contains("Misma ubicación")),
                "pero NO debe alegar que estan en el mismo sitio");
    }

    @Test
    @DisplayName("Los rellenos 9999999999 no relacionan clientes distintos")
    void comodinesNoRelacionan() {
        // En produccion habia 32 clientes con ruc_ci "9999999999" y 12 con
        // telefono "0999999999": tratarlos como iguales fusionaria empresas
        // que no tienen nada que ver
        Cliente a = cliente("FERRETERIA EL TORNILLO", "9999999999", "0999999999",
                "Av. Amazonas 100", -0.20000, -78.50000);
        Cliente b = cliente("PANADERIA LA ESPIGA", "9999999999", "0999999999",
                "Calle Rocafuerte 500", -2.17000, -79.92000);

        assertNull(ClienteSimilitud.comparar(a, b),
                "no deberian relacionarse solo por los valores comodin");
    }

    @Test
    @DisplayName("Clientes sin nada en comun no generan aviso")
    void clientesDistintosNoAvisan() {
        Cliente a = cliente("COMERCIAL ANDINA", "1791234567001", "0987654321",
                "Av. 6 de Diciembre 1234", -0.18000, -78.48000);
        Cliente b = cliente("DISTRIBUIDORA COSTA", "0991111111001", "0912345678",
                "Av. Francisco de Orellana 55", -2.16000, -79.90000);

        assertNull(ClienteSimilitud.comparar(a, b));
    }

    @Test
    @DisplayName("Mismo nombre y misma direccion: el duplicado clasico")
    void mismoNombreMismaDireccion() {
        // Caso real: BETSY SALAN aparecia tres veces con todo identico
        Cliente a = cliente("BETSY SALAN", "0916215601001", "+593 96 718 8538",
                "El Oro 6917, Guayaquil 090416, Ecuador", null, null);
        Cliente b = cliente("BETSY SALAN", "0916215601001", "0967188538",
                "El Oro 6917, Guayaquil 090416, Ecuador", null, null);

        ClienteSimilitud.Coincidencia c = ClienteSimilitud.comparar(a, b);

        assertNotNull(c);
        assertTrue(c.getMotivos().stream().anyMatch(m -> m.contains("Mismo teléfono")),
                "el telefono debe coincidir pese al prefijo +593 y los espacios");
    }
}
