package com.example.mislugares.almacenamiento;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static java.lang.Integer.parseInt;

/**
 * Created by jmtt_ on 2/26/2018.
 */

public class Preferencias {
    private static final Preferencias INSTANCIA = new Preferencias();
    private SharedPreferences pref;
    public final static int SELECCION_TODOS = 0;
    public final static int SELECCION_MIOS = 1;
    public final static int SELECCION_TIPO = 2;

    public static Preferencias getInstance() {
        return INSTANCIA;
    }

    private Preferencias() {
    }

    public void inicializa(Context contexto) {
        pref = PreferenceManager.getDefaultSharedPreferences(contexto);
    }

    public boolean usarFirestore() {
        return (pref.getBoolean("firestore", false));
    }

    public boolean usarFirebaseUI() {
        return (pref.getBoolean("firebaseUI", true));
    }

    public int criterioSeleccion() {
        return parseInt(pref.getString("seleccion", "0"));
    }

    public String tipoSeleccion() {
        return (pref.getString("tipo_seleccion", "BAR"));
    }

    public String criterioOrdenacion() {
        return pref.getString("orden", "valoracion");
    }

    public int maximoMostrar() {
        return parseInt(pref.getString("maximo", "50"));
    }

}
