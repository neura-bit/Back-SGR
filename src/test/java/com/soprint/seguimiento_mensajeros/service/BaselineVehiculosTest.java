package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.TipoVehiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaselineVehiculosTest {

    private static final double DELTA = 0.01;

    /** Agrega `total` tareas del mensajero con el vehiculo, `aTiempo` de ellas a tiempo. */
    private static void tareas(List<BaselineVehiculos.Registro> lista, long idMensajero, TipoVehiculo vehiculo,
                               int total, int aTiempo, Long minutos) {
        for (int i = 0; i < total; i++) {
            lista.add(new BaselineVehiculos.Registro(idMensajero, vehiculo, i < aTiempo, minutos));
        }
    }

    @Test
    @DisplayName("El mensajero en camion no queda por debajo del de moto solo por el vehiculo")
    void camionMejorQueSusParesSuperaAMotoPeorQueLosSuyos() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        // Resto de la flota: camiones llegan a tiempo el 60%, motos el 95%
        tareas(r, 1, TipoVehiculo.CAMION, 25, 15, 90L);
        tareas(r, 2, TipoVehiculo.MOTO, 20, 19, 30L);
        tareas(r, 3, TipoVehiculo.MOTO, 20, 19, 30L);
        // Evaluados: camion 70% (sobre su flota), moto 90% (bajo su flota)
        tareas(r, 10, TipoVehiculo.CAMION, 10, 7, 90L);
        tareas(r, 20, TipoVehiculo.MOTO, 10, 9, 30L);

        BaselineVehiculos b = new BaselineVehiculos(r);
        BaselineVehiculos.Evaluacion camion = b.evaluar(10L);
        BaselineVehiculos.Evaluacion moto = b.evaluar(20L);

        assertEquals(70.0, camion.cumplimientoReal(), DELTA);
        assertEquals(60.0, camion.cumplimientoEsperado(), DELTA);
        assertEquals(10.0, camion.diferenciaVsPares(), DELTA);

        assertEquals(90.0, moto.cumplimientoReal(), DELTA);
        assertEquals(95.0, moto.cumplimientoEsperado(), DELTA);
        assertEquals(-5.0, moto.diferenciaVsPares(), DELTA);

        assertTrue(camion.cumplimientoAjustado() > moto.cumplimientoAjustado(),
                "con el ajuste, el de camion debe quedar por encima");
    }

    @Test
    @DisplayName("El ajustado es el % general de la flota mas la diferencia contra sus pares")
    void ajustadoEnEscalaDeLaFlota() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        tareas(r, 1, TipoVehiculo.AUTO, 20, 16, null);  // flota auto 80%
        tareas(r, 10, TipoVehiculo.AUTO, 10, 9, null);   // evaluado 90%

        BaselineVehiculos b = new BaselineVehiculos(r);
        // General: 25 de 30 = 83.33%, diferencia +10
        assertEquals(83.33, b.getPorcentajeGeneral(), DELTA);
        assertEquals(93.33, b.evaluar(10L).cumplimientoAjustado(), DELTA);
    }

    @Test
    @DisplayName("El unico mensajero con un vehiculo no se compara contra si mismo")
    void sinParesNoHayBase() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        tareas(r, 1, TipoVehiculo.CAMION, 40, 20, 90L);
        tareas(r, 2, TipoVehiculo.MOTO, 40, 38, 30L);

        BaselineVehiculos.Evaluacion unico = new BaselineVehiculos(r).evaluar(1L);

        assertNull(unico.cumplimientoEsperado());
        assertNull(unico.cumplimientoAjustado());
        assertNull(unico.indiceEficiencia());
        assertEquals(0, unico.tareasEvaluadas());
        assertEquals(40, unico.tareasPorVehiculo().get(TipoVehiculo.CAMION));
    }

    @Test
    @DisplayName("Con menos de 5 tareas propias no se da indice")
    void pocasTareasDelMensajero() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        tareas(r, 1, TipoVehiculo.MOTO, 30, 27, 30L);
        tareas(r, 10, TipoVehiculo.MOTO, 4, 4, 30L);

        BaselineVehiculos.Evaluacion e = new BaselineVehiculos(r).evaluar(10L);

        assertEquals(4, e.tareasEvaluadas());
        assertNull(e.cumplimientoAjustado());
    }

    @Test
    @DisplayName("Con varios vehiculos, lo esperado se pondera por las tareas hechas con cada uno")
    void mezclaDeVehiculos() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        tareas(r, 1, TipoVehiculo.MOTO, 20, 18, null);    // 90%
        tareas(r, 2, TipoVehiculo.CAMION, 20, 10, null);  // 50%
        tareas(r, 10, TipoVehiculo.MOTO, 6, 6, null);
        tareas(r, 10, TipoVehiculo.CAMION, 4, 2, null);

        BaselineVehiculos.Evaluacion e = new BaselineVehiculos(r).evaluar(10L);

        // (6 * 90 + 4 * 50) / 10 = 74
        assertEquals(74.0, e.cumplimientoEsperado(), DELTA);
        assertEquals(80.0, e.cumplimientoReal(), DELTA);
        assertEquals(10, e.tareasEvaluadas());
    }

    @Test
    @DisplayName("Vehiculo con pocas tareas en la flota no cuenta; el resto si")
    void vehiculoSinBaseSeExcluyeDelCalculo() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        tareas(r, 1, TipoVehiculo.MOTO, 20, 18, null);
        tareas(r, 2, TipoVehiculo.PIE, 5, 5, null);       // flota a pie: pocas tareas
        tareas(r, 10, TipoVehiculo.MOTO, 6, 6, null);
        tareas(r, 10, TipoVehiculo.PIE, 3, 0, null);

        BaselineVehiculos b = new BaselineVehiculos(r);
        BaselineVehiculos.Evaluacion e = b.evaluar(10L);

        assertEquals(6, e.tareasEvaluadas());
        assertEquals(100.0, e.cumplimientoReal(), DELTA);
        assertEquals(90.0, e.cumplimientoEsperado(), DELTA);

        BaselineVehiculos.Referencia pie = b.referencias().stream()
                .filter(x -> x.vehiculo() == TipoVehiculo.PIE).findFirst().orElseThrow();
        assertFalse(pie.suficiente());
        assertEquals(2, pie.mensajeros());
    }

    @Test
    @DisplayName("Tareas sin fecha limite no suman ni restan al cumplimiento")
    void tareasSinPlazo() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        tareas(r, 1, TipoVehiculo.AUTO, 20, 10, null);
        tareas(r, 10, TipoVehiculo.AUTO, 5, 5, null);
        for (int i = 0; i < 10; i++) {
            r.add(new BaselineVehiculos.Registro(10L, TipoVehiculo.AUTO, null, null));
        }

        BaselineVehiculos.Evaluacion e = new BaselineVehiculos(r).evaluar(10L);

        assertEquals(5, e.tareasEvaluadas());
        assertEquals(100.0, e.cumplimientoReal(), DELTA);
        assertEquals(15, e.tareasPorVehiculo().get(TipoVehiculo.AUTO));
    }

    @Test
    @DisplayName("Eficiencia: tiempo que tardan sus pares con ese vehiculo sobre el suyo")
    void indiceDeEficiencia() {
        List<BaselineVehiculos.Registro> r = new ArrayList<>();
        tareas(r, 1, TipoVehiculo.CAMIONETA, 20, 20, 60L);
        tareas(r, 10, TipoVehiculo.CAMIONETA, 5, 5, 40L);

        assertEquals(1.5, new BaselineVehiculos(r).evaluar(10L).indiceEficiencia(), DELTA);
    }

    @Test
    @DisplayName("Un mensajero sin tareas con vehiculo queda sin base, sin romper")
    void mensajeroSinTareas() {
        BaselineVehiculos.Evaluacion e = new BaselineVehiculos(List.of()).evaluar(99L);

        assertEquals(0, e.tareasEvaluadas());
        assertNull(e.cumplimientoAjustado());
        assertTrue(e.tareasPorVehiculo().isEmpty());
    }
}
