package com.example.mislugares.almacenamiento;

import android.content.Context;

import com.example.mislugares.modelo.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class Usuarios {
    public static void guardarUsuario(Context context, FirebaseUser user) {
        Preferencias pref = Preferencias.getInstance();
        pref.inicializa(context);

        Usuario usuario = new Usuario(user.getDisplayName(), user.getEmail());
        if (pref.usarFirestore()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("usuarios").document(user.getUid()).set(usuario);
        }else{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("usuarios/"+user.getUid()).setValue(usuario);
        }
    }
}
