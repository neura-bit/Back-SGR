package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.Cliente;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas para decidir si dos clientes son probablemente el mismo.
 *
 * Las reglas y sus pesos salieron de analizar los 1311 clientes reales de
 * produccion; los casos que las justifican estan anotados en cada una. En
 * particular NO se compara el correo: en esta base guarda el correo del
 * vendedor de Soprint, no el del cliente (c.ventas@soprint.ec aparecia en 126
 * clientes distintos), asi que como senal de identidad es ruido puro.
 */
public final class ClienteSimilitud {

    /** Puntaje a partir del cual se avisa al usuario. */
    public static final int UMBRAL_AVISO = 40;

    private ClienteSimilitud() {
    }

    /** Un cliente existente que se parece al que se esta por crear. */
    public static class Coincidencia {
        private final Cliente cliente;
        private final int puntaje;
        private final List<String> motivos;

        Coincidencia(Cliente cliente, int puntaje, List<String> motivos) {
            this.cliente = cliente;
            this.puntaje = puntaje;
            this.motivos = motivos;
        }

        public Cliente getCliente() {
            return cliente;
        }

        public int getPuntaje() {
            return puntaje;
        }

        public List<String> getMotivos() {
            return motivos;
        }
    }

    /** minusculas, sin tildes, sin puntuacion, espacios colapsados. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String t = Normalizer.normalize(texto, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return t.replaceAll("[^a-z0-9]+", " ").trim();
    }

    public static String soloDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /**
     * Detecta rellenos tipo 9999999999, 0000000000 o cadenas demasiado cortas
     * para identificar a nadie. En produccion habia 32 clientes con ruc_ci
     * "9999999999"; tratarlos como iguales fusionaria empresas sin relacion.
     */
    public static boolean esComodin(String digitos) {
        if (digitos == null || digitos.length() < 7) {
            return true;
        }
        return digitos.chars().distinct().count() <= 2;
    }

    /**
     * Cedula base. El RUC de persona natural en Ecuador es la cedula mas el
     * sufijo "001", asi que 1719146399 y 1719146399001 son la misma persona:
     * ese fue justamente el caso que genero un duplicado con 31 tareas.
     */
    public static String identidad(String rucCi) {
        String d = soloDigitos(rucCi);
        if (esComodin(d)) {
            return "";
        }
        return (d.length() == 13 && d.endsWith("001")) ? d.substring(0, 10) : d;
    }

    /** Ultimos 9 digitos, para que 0999... y +593 99... coincidan. */
    public static String telefono(String telefono) {
        String d = soloDigitos(telefono);
        if (esComodin(d)) {
            return "";
        }
        return d.length() >= 9 ? d.substring(d.length() - 9) : d;
    }

    /** Distancia aproximada en metros. Suficiente a escala de una ciudad. */
    public static double metros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = (lat1 - lat2) * 111_320.0;
        double dLon = (lon1 - lon2) * 111_320.0 * Math.cos(Math.toRadians(lat1));
        return Math.hypot(dLat, dLon);
    }

    private static boolean tieneCoords(Cliente c) {
        return c.getLatitud() != null && c.getLongitud() != null
                && !(c.getLatitud() == 0.0 && c.getLongitud() == 0.0);
    }

    /**
     * Compara el cliente que se quiere crear contra uno existente.
     *
     * @return null si no se parecen lo suficiente como para molestar al usuario
     */
    public static Coincidencia comparar(Cliente candidato, Cliente existente) {
        int puntaje = 0;
        List<String> motivos = new ArrayList<>();

        String idA = identidad(candidato.getRucCi());
        String idB = identidad(existente.getRucCi());
        if (!idA.isEmpty() && idA.equals(idB)) {
            puntaje += 55;
            String a = soloDigitos(candidato.getRucCi());
            String b = soloDigitos(existente.getRucCi());
            motivos.add(a.equals(b)
                    ? "Mismo RUC/cédula: " + existente.getRucCi()
                    : "Misma identidad con distinto formato (" + candidato.getRucCi()
                      + " vs " + existente.getRucCi() + ")");
        }

        String nomA = normalizar(candidato.getNombre());
        String nomB = normalizar(existente.getNombre());
        if (!nomA.isEmpty() && nomA.equals(nomB)) {
            puntaje += 50;
            motivos.add("Mismo nombre");
        } else if (!nomA.isEmpty() && !nomB.isEmpty()
                && (nomA.contains(nomB) || nomB.contains(nomA))) {
            puntaje += 30;
            motivos.add("Un nombre contiene al otro");
        }

        // La ubicacion pesa fuerte: para reparto, dos registros en el mismo
        // punto son el mismo destino aunque el nombre este escrito distinto
        // (el caso CIOBALANZE / BIOBALANZE, un error de tipeo de una letra).
        if (tieneCoords(candidato) && tieneCoords(existente)) {
            double d = metros(candidato.getLatitud(), candidato.getLongitud(),
                    existente.getLatitud(), existente.getLongitud());
            if (d < 50) {
                puntaje += 45;
                motivos.add(String.format("Misma ubicación en el mapa (a %.0f m)", d));
            } else if (d < 150) {
                puntaje += 20;
                motivos.add(String.format("Ubicación muy cercana (a %.0f m)", d));
            }
        }

        String dirA = normalizar(candidato.getDireccion());
        String dirB = normalizar(existente.getDireccion());
        if (!dirA.isEmpty() && dirA.equals(dirB)) {
            puntaje += 40;
            motivos.add("Misma dirección");
        }

        String telA = telefono(candidato.getTelefono());
        String telB = telefono(existente.getTelefono());
        if (!telA.isEmpty() && telA.equals(telB)) {
            puntaje += 30;
            motivos.add("Mismo teléfono: " + existente.getTelefono());
        }

        return puntaje >= UMBRAL_AVISO ? new Coincidencia(existente, puntaje, motivos) : null;
    }
}
