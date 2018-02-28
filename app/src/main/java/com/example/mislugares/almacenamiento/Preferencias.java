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
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("firestore", pref.getBoolean("firestore", true));
        editor.putBoolean("firebaseUI", pref.getBoolean("firebaseUI", false));

        editor.putBoolean("activar_filtros", pref.getBoolean("activar_filtros", false));

        editor.putString("seleccion", pref.getString("seleccion", "0"));
        editor.putString("tipo_seleccion", pref.getString("tipo_seleccion", "BAR"));
        editor.putString("orden", pref.getString("orden", "valoracion"));
        editor.putString("maximo", pref.getString("maximo", "50"));
        editor.apply();
    }

    public boolean usarFirestore() {
        return (pref.getBoolean("firestore", false));
    }

    public boolean usarFirebaseUI() {
        return (pref.getBoolean("firebaseUI", false));
    }

    public boolean usarFiltros() {
        return (pref.getBoolean("activar_filtros", false));
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
