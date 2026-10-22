package com.soprint.seguimiento_mensajeros.service;

import com.soprint.seguimiento_mensajeros.model.TipoVehiculo;
import com.soprint.seguimiento_mensajeros.service.BaselinePares.Area;
import com.soprint.seguimiento_mensajeros.service.BaselinePares.Nivel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaselineParesTest {

    private static final double DELTA = 0.01;

    private record Tipo(long id, String nombre, boolean tecnica) {
    }

    private static final Tipo ENTREGA = new Tipo(1, "Entrega", false);
    private static final Tipo RETIRO = new Tipo(2, "Retiro", false);
    private static final Tipo GARANTIA = new Tipo(4, "Garantía", true);
    private static final Tipo RENTING = new Tipo(6, "Renting", true);

    /** Agrega `total` tareas del mensajero, `aTiempo` de ellas a tiempo. */
    private static void tareas(List<BaselinePares.Registro> lista, long idMensajero, Tipo tipo,
                               TipoVehiculo vehiculo, int total, int aTiempo, Long minutos) {
        for (int i = 0; i < total; i++) {
            lista.add(new BaselinePares.Registro(idMensajero, tipo.id(), tipo.nombre(), tipo.tecnica(), vehiculo,
                    i < aTiempo, minutos));
        }
    }

    private static BaselinePares.ReferenciaTipo referencia(BaselinePares b, Tipo tipo) {
        return b.referenciasPorTipo().stream()
                .filter(t -> t.idTipoOperacion() == tipo.id()).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("El tecnico no queda por debajo del mensajero solo porque su trabajo es mas dificil")
    void tecnicoMejorQueSusParesSuperaAMensajeroPeorQueLosSuyos() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        // Resto de la flota: garantias a tiempo el 60%, entregas el 95%
        tareas(r, 1, GARANTIA, TipoVehiculo.AUTO, 25, 15, 180L);
        tareas(r, 2, ENTREGA, TipoVehiculo.MOTO, 20, 19, 30L);
        tareas(r, 3, ENTREGA, TipoVehiculo.MOTO, 20, 19, 30L);
        // Evaluados: tecnico 70% (sobre sus pares), mensajero 90% (bajo los suyos)
        tareas(r, 10, GARANTIA, TipoVehiculo.AUTO, 20, 14, 180L);
        tareas(r, 20, ENTREGA, TipoVehiculo.MOTO, 20, 18, 30L);

        BaselinePares b = new BaselinePares(r);
        BaselinePares.Evaluacion tecnico = b.evaluar(10L);
        BaselinePares.Evaluacion mensajero = b.evaluar(20L);

        assertEquals(Area.TECNICA, tecnico.area());
        assertEquals(60.0, tecnico.cumplimientoEsperado(), DELTA);
        assertEquals(10.0, tecnico.diferenciaVsPares(), DELTA);

        assertEquals(Area.LOGISTICA, mensajero.area());
        assertEquals(95.0, mensajero.cumplimientoEsperado(), DELTA);
        assertEquals(-5.0, mensajero.diferenciaVsPares(), DELTA);

        assertTrue(tecnico.cumplimientoAjustado() > mensajero.cumplimientoAjustado(),
                "con la comparacion por tipo, el tecnico debe quedar por encima");
    }

    @Test
    @DisplayName("Dentro de un mismo tipo se sigue ajustando por vehiculo")
    void mismoTipoDistintoVehiculo() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, ENTREGA, TipoVehiculo.CAMION, 25, 15, null);  // entregas en camion 60%
        tareas(r, 2, ENTREGA, TipoVehiculo.MOTO, 25, 24, null);    // entregas en moto 96%
        tareas(r, 10, ENTREGA, TipoVehiculo.CAMION, 20, 14, null);

        BaselinePares.Evaluacion e = new BaselinePares(r).evaluar(10L);

        assertEquals(60.0, e.cumplimientoEsperado(), DELTA);
        assertEquals(20, e.tareasPorNivel().get(Nivel.TIPO_Y_VEHICULO));
    }

    @Test
    @DisplayName("El ajustado es el % general de TODA la flota mas la diferencia, no el de su area")
    void ajustadoEnEscalaDeLaFlota() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, ENTREGA, TipoVehiculo.MOTO, 30, 27, null);    // 90%
        tareas(r, 2, GARANTIA, TipoVehiculo.AUTO, 20, 12, null);   // 60%
        tareas(r, 10, GARANTIA, TipoVehiculo.AUTO, 20, 12, null);  // igual que sus pares

        BaselinePares b = new BaselinePares(r);
        // General: 51 de 70 = 72.86%; diferencia 0 -> ajustado 72.86, no 60
        assertEquals(72.86, b.getPorcentajeGeneral(), DELTA);
        assertEquals(0.0, b.evaluar(10L).diferenciaVsPares(), DELTA);
        assertEquals(72.86, b.evaluar(10L).cumplimientoAjustado(), DELTA);
    }

    @Test
    @DisplayName("Quien hace casi todas las tareas de un vehiculo se compara contra el tipo completo")
    void caeAlTipoCuandoElVehiculoNoTieneOtros() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, ENTREGA, TipoVehiculo.MOTO, 30, 24, null);    // entregas: 80% de otros
        tareas(r, 2, ENTREGA, TipoVehiculo.AUTO, 3, 3, null);
        tareas(r, 10, ENTREGA, TipoVehiculo.AUTO, 25, 20, null);   // solo 3 de otros en auto

        BaselinePares.Evaluacion e = new BaselinePares(r).evaluar(10L);

        assertEquals(25, e.tareasEvaluadas());
        assertEquals(25, e.tareasPorNivel().get(Nivel.TIPO));
        // Otros en entregas, sin contarlo a el: 27 de 33
        assertEquals(27 * 100.0 / 33, e.cumplimientoEsperado(), DELTA);
    }

    @Test
    @DisplayName("Tipo con pocas tareas de otros se compara contra su area")
    void caeAlAreaCuandoElTipoNoTieneOtros() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, GARANTIA, TipoVehiculo.AUTO, 20, 14, null);   // tecnica de otros: 70%
        tareas(r, 10, RENTING, TipoVehiculo.CAMIONETA, 16, 12, null); // nadie mas hace renting
        tareas(r, 20, ENTREGA, TipoVehiculo.MOTO, 40, 40, null);   // logistica no se mezcla

        BaselinePares b = new BaselinePares(r);
        BaselinePares.Evaluacion e = b.evaluar(10L);

        assertEquals(16, e.tareasPorNivel().get(Nivel.AREA));
        assertEquals(70.0, e.cumplimientoEsperado(), DELTA);
        assertFalse(referencia(b, RENTING).suficiente());
    }

    @Test
    @DisplayName("Suficiente en la tabla coincide con lo que se usa al evaluar")
    void suficienteCoherenteConLaEvaluacion() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        // 42 tareas de entrega en auto, 25 de ellas de un solo mensajero:
        // la celda tiene mas de 20, pero a el le quedan 17 de otros
        tareas(r, 1, ENTREGA, TipoVehiculo.AUTO, 17, 12, null);
        tareas(r, 10, ENTREGA, TipoVehiculo.AUTO, 25, 20, null);
        tareas(r, 2, ENTREGA, TipoVehiculo.MOTO, 30, 27, null);

        BaselinePares b = new BaselinePares(r);
        BaselinePares.ReferenciaVehiculo auto = referencia(b, ENTREGA).vehiculos().stream()
                .filter(v -> v.vehiculo() == TipoVehiculo.AUTO).findFirst().orElseThrow();

        assertEquals(42, auto.tareas());
        assertFalse(auto.suficiente());
        assertEquals(25, b.evaluar(10L).tareasPorNivel().get(Nivel.TIPO));
        assertTrue(referencia(b, ENTREGA).suficiente());
    }

    @Test
    @DisplayName("El area del mensajero es donde hizo la mayoria de sus tareas")
    void areaPorMayoria() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 10, GARANTIA, TipoVehiculo.AUTO, 6, 6, null);
        tareas(r, 10, ENTREGA, TipoVehiculo.AUTO, 4, 4, null);
        tareas(r, 20, GARANTIA, TipoVehiculo.AUTO, 3, 3, null);
        tareas(r, 20, RETIRO, TipoVehiculo.MOTO, 3, 3, null);

        BaselinePares b = new BaselinePares(r);
        BaselinePares.Evaluacion tecnico = b.evaluar(10L);

        assertEquals(Area.TECNICA, tecnico.area());
        assertEquals(6, tecnico.tareasPorTipo().get("Garantía"));
        assertEquals(4, tecnico.tareasPorTipo().get("Entrega"));
        assertEquals(Area.LOGISTICA, b.evaluar(20L).area(), "en empate va a logistica");
    }

    @Test
    @DisplayName("Las tareas logisticas de un tecnico se comparan contra las de los mensajeros")
    void tecnicoHaciendoEntregas() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, ENTREGA, TipoVehiculo.MOTO, 20, 18, null);    // 90%
        tareas(r, 2, GARANTIA, TipoVehiculo.AUTO, 20, 10, null);   // 50%
        tareas(r, 10, GARANTIA, TipoVehiculo.AUTO, 12, 6, null);
        tareas(r, 10, ENTREGA, TipoVehiculo.MOTO, 8, 8, null);

        BaselinePares.Evaluacion e = new BaselinePares(r).evaluar(10L);

        // (12 * 50 + 8 * 90) / 20 = 66
        assertEquals(66.0, e.cumplimientoEsperado(), DELTA);
        assertEquals(70.0, e.cumplimientoReal(), DELTA);
    }

    @Test
    @DisplayName("El unico de su area no se compara contra si mismo")
    void sinParesNoHayBase() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, GARANTIA, TipoVehiculo.AUTO, 40, 20, 90L);
        tareas(r, 2, ENTREGA, TipoVehiculo.MOTO, 40, 38, 30L);

        BaselinePares.Evaluacion unico = new BaselinePares(r).evaluar(1L);

        assertNull(unico.cumplimientoEsperado());
        assertNull(unico.cumplimientoAjustado());
        assertNull(unico.indiceEficiencia());
        assertEquals(0, unico.tareasEvaluadas());
        assertEquals(40, unico.tareasPorVehiculo().get(TipoVehiculo.AUTO));
    }

    @Test
    @DisplayName("Con menos de 15 tareas propias no se da indice")
    void pocasTareasDelMensajero() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, ENTREGA, TipoVehiculo.MOTO, 30, 27, 30L);
        tareas(r, 10, ENTREGA, TipoVehiculo.MOTO, 14, 2, 30L);
        tareas(r, 20, ENTREGA, TipoVehiculo.MOTO, 15, 2, 30L);

        BaselinePares b = new BaselinePares(r);
        BaselinePares.Evaluacion e = b.evaluar(10L);

        assertEquals(14, e.tareasEvaluadas());
        assertNull(e.cumplimientoAjustado());
        assertNull(e.indiceEficiencia());
        assertTrue(b.evaluar(20L).cumplimientoAjustado() != null, "con 15 ya se califica");
    }

    @Test
    @DisplayName("Tareas sin fecha limite no suman ni restan al cumplimiento")
    void tareasSinPlazo() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, ENTREGA, TipoVehiculo.AUTO, 20, 10, null);
        tareas(r, 10, ENTREGA, TipoVehiculo.AUTO, 15, 15, null);
        for (int i = 0; i < 10; i++) {
            r.add(new BaselinePares.Registro(10L, ENTREGA.id(), ENTREGA.nombre(), false, TipoVehiculo.AUTO,
                    null, null));
        }

        BaselinePares.Evaluacion e = new BaselinePares(r).evaluar(10L);

        assertEquals(15, e.tareasEvaluadas());
        assertEquals(100.0, e.cumplimientoReal(), DELTA);
        assertEquals(25, e.tareasPorVehiculo().get(TipoVehiculo.AUTO));
    }

    @Test
    @DisplayName("Eficiencia: tiempo de sus pares en ese mismo trabajo sobre el suyo")
    void indiceDeEficiencia() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, GARANTIA, TipoVehiculo.CAMIONETA, 20, 20, 180L);
        tareas(r, 2, ENTREGA, TipoVehiculo.CAMIONETA, 40, 40, 20L);   // no entra: otro tipo
        tareas(r, 10, GARANTIA, TipoVehiculo.CAMIONETA, 15, 15, 120L);

        assertEquals(1.5, new BaselinePares(r).evaluar(10L).indiceEficiencia(), DELTA);
    }

    @Test
    @DisplayName("Referencia por area: tecnica y logistica por separado")
    void referenciaPorArea() {
        List<BaselinePares.Registro> r = new ArrayList<>();
        tareas(r, 1, ENTREGA, TipoVehiculo.MOTO, 10, 9, null);
        tareas(r, 2, GARANTIA, TipoVehiculo.AUTO, 10, 5, null);

        List<BaselinePares.ReferenciaArea> areas = new BaselinePares(r).referenciasPorArea();

        assertEquals(2, areas.size());
        assertEquals(Area.LOGISTICA, areas.get(0).area());
        assertEquals(90.0, areas.get(0).porcentajeATiempo(), DELTA);
        assertEquals(50.0, areas.get(1).porcentajeATiempo(), DELTA);
    }

    @Test
    @DisplayName("Un mensajero sin tareas queda sin base, sin romper")
    void mensajeroSinTareas() {
        BaselinePares.Evaluacion e = new BaselinePares(List.of()).evaluar(99L);

        assertEquals(0, e.tareasEvaluadas());
        assertNull(e.cumplimientoAjustado());
        assertTrue(e.tareasPorVehiculo().isEmpty());
        assertEquals(Area.LOGISTICA, e.area());
    }
}
