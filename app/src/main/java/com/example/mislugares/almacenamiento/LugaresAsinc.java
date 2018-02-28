package com.example.mislugares.almacenamiento;

import com.example.mislugares.modelo.Lugar;

/**
 * Created by jmtt_ on 2/26/2018.
 */

public interface LugaresAsinc {
    interface EscuchadorElemento{
        void onRespuesta(Lugar lugar);
    }
    interface EscuchadorTamanyo{
        void onRespuesta(long tamanyo);
    }
    void elemento(String id, EscuchadorElemento escuchador);
    void anyade(Lugar lugar);
    String nuevo();
    void borrar(String id);
    void actualiza(String id, Lugar lugar);
    void tamanyo(EscuchadorTamanyo escuchador);
}
