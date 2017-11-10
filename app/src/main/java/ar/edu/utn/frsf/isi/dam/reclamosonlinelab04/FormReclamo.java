package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.admin.ConnectEvent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.TipoReclamo;

public class FormReclamo extends AppCompatActivity {

    public static final int MAP_REQ = 1;
    public static final int RESULT_DELETED = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private static final int PERMISSION_REQUEST_VOICE = 4;
    private static final int REQUEST_IMAGE_CAPTURE = 5;
    private static final int RESULT_OK = 6;

    private Intent intentOrigen;

    private EditText editTextTitulo;
    private EditText editTextDetalle;
    private EditText editTextLugar;
    private Button btnElegirLugar;
    private Spinner spinnerTipoReclamo;
    private Button btnGuardar;
    private Button btnEliminar;
    private Button btnCancelar;
    private Button btnGrabarAudio;
    private Button btnPlayAudio;
    private Button btnCargarFoto;
    private ImageView imgFotoReclamo;

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
        btnCargarFoto = (Button) findViewById(R.id.frmCargarFoto);
        btnGrabarAudio = (Button) findViewById(R.id.frmReclamoRecAudio);
        btnPlayAudio = (Button) findViewById(R.id.frmReclamoPlayAudio);
        imgFotoReclamo = (ImageView) findViewById(R.id.frmReclamoImgFoto);
    }

    private void setearListeners() {
        btnElegirLugar.setOnClickListener(new ElegirLugarListener());
        btnGuardar.setOnClickListener(new GuardarListener());
        btnEliminar.setOnClickListener(new EliminarListener());
        btnCancelar.setOnClickListener(new CancelarListener());
        btnCargarFoto.setOnClickListener(new CargarFotoListener());
        btnGrabarAudio.setOnClickListener(new GrabarAudioListener());
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
            Boolean esNuevo = false;

            if(flagNuevoReclamo) {
                esNuevo = true;
                Estado estado = null; //El estado para un nuevo reclamo es "enviado" y tiene id 1
                int id = 0;
                try {
                    id = obtenerNuevoID();
                    estado = getEstadoById(1);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Date fecha = new Date();

                // creo el reclamo y lo paso a la capa dao para guardar
                Reclamo nuevoReclamo = new Reclamo(id, titulo, detalle, fecha, tipoReclamo, estado, lugar);
                new HttpAsyncTask().execute(nuevoReclamo, 1, 0);
            } else {
                // seteo los atributos del reclamo existente y lo paso a la capa dao para actualizar
                reclamo.setTitulo(titulo);
                reclamo.setDetalle(detalle);
                reclamo.setTipo(tipoReclamo);
                reclamo.setLugar(lugar);
                // TODO: SI TIENE FOTO GUARDAR. SI TIENE AUDIO HABILITAR REPRODUCIR. GUARDAR AUDIO.

                new HttpAsyncTask().execute(reclamo, 2, 0);
            }

            setResult(RESULT_OK, intentOrigen);
            finish();
        }
    }

    private class CargarFotoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            askForPermission(Manifest.permission.CAMERA, FormReclamo.PERMISSION_REQUEST_CAMERA, getString(R.string.solicitud_permiso_foto));
        }
    }

    private class GrabarAudioListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            askForPermission(Manifest.permission.RECORD_AUDIO, FormReclamo.PERMISSION_REQUEST_VOICE, getString(R.string.solicitud_permiso_audio));
        }
    }

    public void askForPermission(final String permisoManifest, final int codigoPermiso, String rationaleMsgStr){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(FormReclamo.this,
                    permisoManifest)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(FormReclamo.this,
                        permisoManifest)) {
                    // Por lo que entiendo, esto lo pide solamente si ya intento varias veces y
                    // hay que hacerle una explicacion mas detallada de por que necesitamos el permiso
                    AlertDialog.Builder builder = new AlertDialog.Builder(FormReclamo.this);
                    builder.setTitle(R.string.titulo_dialog);
                    builder.setPositiveButton(android.R.string.ok,null);
                    builder.setMessage(rationaleMsgStr);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {permisoManifest}
                                    , codigoPermiso);
                        }
                    });
                    builder.show();
                } else {
                    // Abre el dialogo para pedir el permiso del a camara.
                    ActivityCompat.requestPermissions(FormReclamo.this,
                            new String[]
                                    {permisoManifest},
                            codigoPermiso);
                }
            } else {
                // El permiso ya esta dado
                dispatchTakePictureIntent();
            }
        } else {
            // la versión alcanza con tenerlo declarado
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults) {
        switch (requestCode) {
            case FormReclamo.PERMISSION_REQUEST_CAMERA: {
                // si el request es cancelado el arreglo es vacio.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // tengo el permiso, saco la foto!!!
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(FormReclamo.this, "Pidio camara y rechazo", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case FormReclamo.PERMISSION_REQUEST_VOICE: {
                // si el request es cancelado el arreglo es vacio.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(FormReclamo.this, "Pidio audio y acepto", Toast.LENGTH_SHORT).show();
                    // tengo el permiso!!!.
                } else {
                    Toast.makeText(FormReclamo.this, "Pidio audio y rechazo", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new
                Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) !=
                null) {
            startActivityForResult(takePictureIntent,
                    REQUEST_IMAGE_CAPTURE);
        }
    }

    private int obtenerNuevoID() throws ExecutionException, InterruptedException {
        List<Reclamo> reclamos;
        HttpAsyncTask as = new HttpAsyncTask();

        reclamos = (List<Reclamo>) as.execute(null, 4, 0).get();

        int id = -1;
        for(Reclamo r : reclamos) {
            if(r.getId() > id) {
                id = r.getId();
            }
        }
        return id + 1;
    }

    private Estado getEstadoById(int id) throws ExecutionException, InterruptedException {
        Estado estado;
        HttpAsyncTask as = new HttpAsyncTask();

        estado = (Estado) as.execute(null, 5, id).get();

        return estado;
    }


    private class EliminarListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            new HttpAsyncTask().execute(reclamo, 3, 0);
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
        switch(requestCode) {
            case MAP_REQ:
                if (resultCode == RESULT_OK) {
                    lugar = data.getParcelableExtra(MapsActivity.LUGAR_KEY);
                    editTextLugar.setText(lugar.toString());
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imgFotoReclamo.setImageBitmap(imageBitmap);
                    saveImageInStorage(imageBitmap);
                }
                break;
        }
    }

    private void saveImageInStorage(Bitmap imageReclamo) {
        File directory = getApplicationContext().getDir("imagenes", Context.MODE_PRIVATE);
        if(!directory.exists())
            directory.mkdir();
        File mypath = new File(directory, "reclamo_" + reclamo.getId() + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            imageReclamo.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImageFromStorage() {
        File directory = getApplicationContext().getDir("imagenes", Context.MODE_PRIVATE);
        try {
            File f=new File(directory, "reclamo_" + reclamo.getId() + ".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            imgFotoReclamo.setImageBitmap(b);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
