package com.example.mislugares.almacenamiento;

import android.util.Log;

import com.example.mislugares.modelo.Lugar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LugaresFirebase implements LugaresAsinc {
    private final static String NODO_LUGARES = "lugares";
    private DatabaseReference nodo;

    public LugaresFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        nodo = database.getReference().child(NODO_LUGARES);
    }

    public void elemento(String id, final EscuchadorElemento escuchador) {
        nodo.child(id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Lugar lugar = dataSnapshot.getValue(Lugar.class);
                        escuchador.onRespuesta(lugar);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("Firebase", "Error al leer.", error.toException());
                        escuchador.onRespuesta(null);
                    }
                });
    }

    public void anyade(Lugar lugar) {
        nodo.push().setValue(lugar);
    }

    public String nuevo() {
        return nodo.push().getKey();
    }

    public void borrar(String id) {
        nodo.child(id).setValue(null);
    }

    public void actualiza(String id, Lugar lugar) {
        nodo.child(id).setValue(lugar);
    }

    public void tamanyo(final EscuchadorTamanyo escuchador) {
        nodo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Lugar lugar = dataSnapshot.getValue(Lugar.class);
                escuchador.onRespuesta(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Error en tamanyo.", error.toException());
                escuchador.onRespuesta(-1);
            }
        });
    }
}
