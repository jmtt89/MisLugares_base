package com.example.mislugares;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class ValoracionesFirestore {
    public static void guardarValoracion(String lugar, String usuario, Double valor) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> datos = new HashMap<>();
        datos.put("valoracion", valor);
        db.collection("lugares")
                .document(lugar)
                .collection("valoraciones")
                .document(usuario).set(datos);
    }

    public interface EscuchadorValoracion {
        void onRespuesta(Double valor);

        void onNoExiste();

        void onError(Exception e);
    }

    public static void guardarValoracionYRecalcular(final String lugar, final String usuario, final float nuevaVal) {
        leerValoracion(lugar, usuario, new EscuchadorValoracion() {
            @Override public void onRespuesta(Double viejaVal) {
                actualizarValoracionMedia(lugar, usuario, viejaVal, nuevaVal);
            }
            @Override public void onNoExiste() {
                actualizarValoracionMedia(lugar, usuario, Double.NaN, nuevaVal);
            }
            @Override public void onError(Exception e) {}
        });
    }

    public static void leerValoracion(String lugar, String usuario, final EscuchadorValoracion escuchador) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("lugares").document(lugar)
                .collection("valoraciones").document(usuario).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {
                                escuchador.onNoExiste();
                            } else {
                                double val = task.getResult().getDouble("valoracion");
                                escuchador.onRespuesta(val);
                            }
                        } else {
                            Log.e("Mis Lugares", "No se puede leer valoraciones", task.getException());
                            escuchador.onError(task.getException());
                        }
                    }
                });
    }

    private static void actualizarValoracionMedia(final String lugar, final String usuario, final double viejaVal, final double nuevaVal) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference refLugar = db.collection("lugares").document(lugar);
        db.runTransaction(new Transaction.Function<Double>() {
            @Override
            public Double apply(Transaction transaction)
                    throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(refLugar);
                double media = snapshot.getDouble("valoracion");
                long nValoraciones = snapshot.getLong("n_valoraciones");
                double nuevaMedia = nuevaMedia(media, nValoraciones, viejaVal, nuevaVal);
                if (Double.isNaN(viejaVal)) nValoraciones++;
                transaction.update(refLugar, "valoracion", nuevaMedia, "n_valoraciones", nValoraciones);
                //Escribimos valoración
                Map<String, Object> datos = new HashMap<>();
                datos.put("valoracion", nuevaVal);
                transaction.set(db.collection("lugares").document(lugar).collection("valoraciones").document(usuario), datos);
                return nuevaMedia;
            }
        }).addOnSuccessListener(new OnSuccessListener<Double>() {
            @Override public void onSuccess(Double result) {
                Log.d("Mis Lugares", "Transaccion OK. Nueva media : " + result);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Mis Lugares", "Transaccion errónea.", e);
            }
        });
    }

    private static Double nuevaMedia(double media, long nValoraciones, double viejaVal, double nuevaVal){
        if (nValoraciones==0){ //No existe ninguna valoración
            return nuevaVal;
        } else if (Double.isNaN(viejaVal )){ //No existe valoración anterior
            return (nValoraciones*media + nuevaVal) / (nValoraciones+1);
        } else { //El usuario cambia su valoración
            return (nValoraciones*media - viejaVal + nuevaVal)/ nValoraciones;
        }
    }
}