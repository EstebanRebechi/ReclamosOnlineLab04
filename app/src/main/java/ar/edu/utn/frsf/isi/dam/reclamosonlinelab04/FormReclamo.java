package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.TipoReclamo;

public class FormReclamo extends AppCompatActivity {

    public static final int RESULT_DELETED = 2;

    public static final int MAP_REQ = 1;

    private Intent intentOrigen;

    private EditText editTextTitulo;
    private EditText editTextDetalle;
    private EditText editTextLugar;
    private Button btnElegirLugar;
    private Spinner spinnerTipoReclamo;
    private Button btnGuardar;
    private Button btnEliminar;
    private Button btnCancelar;

    private ReclamoDao reclamoDao;

    private Reclamo reclamo;
    private LatLng lugar;
    private List<TipoReclamo> listaTiposReclamo;

    private boolean flagNuevoReclamo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_reclamo);

        intentOrigen = getIntent();

        // obtengo el reclamo pasado en el bundle
        reclamo = (Reclamo) intentOrigen.getSerializableExtra("reclamo");
        // si es null (no se pasó ningun reclamo) entonces se desea crear uno nuevo
        flagNuevoReclamo = reclamo == null;

        reclamoDao = new ReclamoDaoHTTP();

        obtenerViews();
        setearListeners();
        inicializarSpinner();
        if(!flagNuevoReclamo) {
            mostrarDatosReclamo();
        }
        btnEliminar.setEnabled(!flagNuevoReclamo);
    }

    private void obtenerViews() {
        editTextTitulo = (EditText) findViewById(R.id.frmReclamoTextReclamo);
        editTextDetalle = (EditText) findViewById(R.id.frmReclamoTextDetReclamo);
        editTextLugar = (EditText) findViewById(R.id.frmReclamoTextLugar);
        btnElegirLugar = (Button) findViewById(R.id.elegirLugar);
        spinnerTipoReclamo = (Spinner) findViewById(R.id.frmReclamoCmbTipo);
        btnGuardar = (Button) findViewById(R.id.frmReclamoGuardar);
        btnEliminar = (Button) findViewById(R.id.frmReclamoEliminar);
        btnCancelar = (Button) findViewById(R.id.frmReclamoCancelar);
    }

    private void setearListeners() {
        btnElegirLugar.setOnClickListener(new ElegirLugarListener());
        btnGuardar.setOnClickListener(new GuardarListener());
        btnEliminar.setOnClickListener(new EliminarListener());
        btnCancelar.setOnClickListener(new CancelarListener());
    }

    private void inicializarSpinner() {
        listaTiposReclamo = new ArrayList<>();
        ArrayAdapter<TipoReclamo> adapterTiposReclamo = new ArrayAdapter<TipoReclamo>(this, android.R.layout.simple_spinner_item, listaTiposReclamo);
        spinnerTipoReclamo.setAdapter(adapterTiposReclamo);
        obtenerTiposReclamo();
    }

    private void obtenerTiposReclamo() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<TipoReclamo> rec = reclamoDao.tiposReclamo();
                listaTiposReclamo.clear();
                listaTiposReclamo.addAll(rec);
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void mostrarDatosReclamo() {
        editTextTitulo.setText(reclamo.getTitulo());
        editTextDetalle.setText(reclamo.getDetalle());
        lugar = reclamo.getLugar();
        if(lugar != null) {
            editTextLugar.setText(lugar.toString());
        }
        spinnerTipoReclamo.setSelection(((ArrayAdapter) spinnerTipoReclamo.getAdapter()).getPosition(reclamo.getTipo()));
    }

    private class ElegirLugarListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(FormReclamo.this,MapsActivity.class);
            if(lugar != null) {
                intent.putExtra(MapsActivity.LUGAR_KEY, lugar);
            }
            startActivityForResult(intent, MAP_REQ);
        }
    }

    private class GuardarListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            // obtengo datos ingresados por el usuario
            String titulo = editTextTitulo.getText().toString();
            String detalle = editTextDetalle.getText().toString();
            TipoReclamo tipoReclamo = (TipoReclamo) spinnerTipoReclamo.getSelectedItem();

            if(flagNuevoReclamo) {
                int id = obtenerNuevoID();
                Date fecha = new Date();
                Estado estado = reclamoDao.getEstadoById(1); //El estado para un nuevo reclamo es "enviado" y tiene id 1

                // creo el reclamo y lo paso a la capa dao para guardar
                Reclamo nuevoReclamo = new Reclamo(id, titulo, detalle, fecha, tipoReclamo, estado, lugar);
                reclamoDao.crear(nuevoReclamo);
            } else {
                // seteo los atributos del reclamo existente y lo paso a la capa dao para actualizar
                reclamo.setTitulo(titulo);
                reclamo.setDetalle(detalle);
                reclamo.setTipo(tipoReclamo);
                reclamo.setLugar(lugar);

                reclamoDao.actualizar(reclamo);
            }

            setResult(RESULT_OK, intentOrigen);
            finish();
        }
    }

    private int obtenerNuevoID() {
        List<Reclamo> reclamos = reclamoDao.reclamos();
        int id = -1;
        for(Reclamo r : reclamos) {
            if(r.getId() > id) {
                id = r.getId();
            }
        }
        return id + 1;
    }

    private class EliminarListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            reclamoDao.borrar(reclamo);
            setResult(RESULT_DELETED, intentOrigen);
            finish();
        }
    }

    private class CancelarListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            setResult(RESULT_CANCELED, intentOrigen);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MAP_REQ) {
            if(resultCode == RESULT_OK) {
                lugar = data.getParcelableExtra(MapsActivity.LUGAR_KEY);
                editTextLugar.setText(lugar.toString());
            }
        }
    }
}
