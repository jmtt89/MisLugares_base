package com.example.mislugares.vistas.adaptador;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class AdaptadorLugaresFirebaseUI
        extends FirebaseRecyclerAdapter<Lugar, AdaptadorLugares.ViewHolder>
        implements AdaptadorLugaresInterface{

    protected View.OnClickListener onClickListener;

    public AdaptadorLugaresFirebaseUI(@NonNull FirebaseRecyclerOptions<Lugar> opciones) {
        super(opciones);
    }

    @Override
    public AdaptadorLugares.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_lista, parent, false);
        return new AdaptadorLugares.ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull AdaptadorLugares.ViewHolder holder, int position, @NonNull Lugar lugar) {
        AdaptadorLugares.personalizaVista(holder, lugar);
        holder.itemView.setOnClickListener(onClickListener);
    }

    public void setOnItemClickListener(View.OnClickListener onClick) {
        onClickListener = onClick;
    }

    public String getKey(int pos) {
        return super.getSnapshots().getSnapshot(pos).getKey();
    }
}
