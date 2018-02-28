package com.example.mislugares.vistas.detalleLugar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.R;
import com.example.mislugares.modelo.TipoLugar;
import com.example.mislugares.vistas.MainActivity;
import com.example.mislugares.vistas.SelectorFragment;
import com.google.firebase.iid.FirebaseInstanceId;

public class EdicionLugarActivity extends AppCompatActivity {
    private long id;
    private String _id;
    private Lugar lugar;
    private EditText nombre;
    private Spinner tipo;
    private EditText direccion;
    private EditText telefono;
    private EditText url;
    private EditText comentario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion_lugar);
        Bundle extras = getIntent().getExtras();
        id = extras.getLong("id", -1);
        _id = extras.getString("_id", null);
        if (_id != null) {
            lugar = new Lugar();
        } else {
            lugar = SelectorFragment.getAdaptador().getItem((int) id);
            _id = SelectorFragment.getAdaptador().getKey((int) id);
        }
        nombre = findViewById(R.id.nombre);
        nombre.setText(lugar.getNombre());
        direccion = findViewById(R.id.direccion);
        direccion.setText(lugar.getDireccion());
        telefono = findViewById(R.id.telefono);
        telefono.setText(Integer.toString(lugar.getTelefono()));
        url = findViewById(R.id.url);
        url.setText(lugar.getUrl());
        comentario = findViewById(R.id.comentario);
        comentario.setText(lugar.getComentario());
        tipo = findViewById(R.id.tipo);
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TipoLugar.getNombres());
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipo.setAdapter(adaptador);
        tipo.setSelection(lugar.getTipoEnum().ordinal());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edicion_lugar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accion_cancelar:
                if(getIntent().getExtras().getBoolean("nuevo", false)) {
                    MainActivity.lugares.borrar(_id);
                }
                finish();
                return true;
            case R.id.accion_guardar:
                lugar.setNombre(nombre.getText().toString());
                lugar.setTipoEnum(TipoLugar.values()[tipo.getSelectedItemPosition()]);
                lugar.setDireccion(direccion.getText().toString());
                lugar.setTelefono(Integer.parseInt(telefono.getText().toString()));
                lugar.setUrl(url.getText().toString());
                lugar.setComentario(comentario.getText().toString());
                lugar.setCreador(FirebaseInstanceId.getInstance().getId());
                MainActivity.lugares.actualiza(_id, lugar);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
