package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.TipoReclamo;

public class FormReclamo extends AppCompatActivity implements OnTaskCompleted{

    public static final int MAP_REQ = 1;
    public static final int RESULT_DELETED = 2;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private static final int PERMISSION_REQUEST_VOICE = 4;
    private static final int REQUEST_IMAGE_CAPTURE = 5;

    private Intent intentOrigen;

    private EditText editTextTitulo;
    private EditText editTextDetalle;
    private EditText editTextLugar;
    private Spinner spinnerTipoReclamo;
    private Button btnElegirLugar;
    private Button btnGuardar;
    private Button btnEliminar;
    private Button btnCancelar;
    private Button btnGrabarAudio;
    private Button btnPlayAudio;
    private Button btnCargarFoto;
    private ImageView imgFotoReclamo;
    private TextView frmReclamoLblFoto;

    private ReclamoDao reclamoDao;
    private Reclamo reclamo;
    private boolean flagNuevoReclamo;
    private LatLng lugar;
    private List<TipoReclamo> listaTiposReclamo;
    private ArrayAdapter<TipoReclamo> adapterTiposReclamo;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private Boolean grabando = false;
    private Boolean reproduciendo = false;
    private File audio = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_reclamo);

        intentOrigen = getIntent();
        Bundle extras = intentOrigen.getExtras();
        reclamoDao = new ReclamoDaoHTTP();
        // obtengo el ID del reclamo pasado en el intent
        final Integer idReclamo = (extras != null) ? extras.getInt("idReclamo") : null;
        final Integer nuevoIdEstadoReclamo = (extras != null) ? extras.getInt("idEstadoReclamo") : null;
        obtenerViews();
        setearListeners();
        inicializarSpinner(-1); //-1 para que no seleccione nada
        btnEliminar.setEnabled(false);
        flagNuevoReclamo = idReclamo==null;
        if(!flagNuevoReclamo){
            Runnable r = new Runnable(){
                @Override
                public void run() {
                    reclamo = reclamoDao.getReclamoById(idReclamo);
                    if(nuevoIdEstadoReclamo != null) {
                        // llego notif con cambio de estado, entonces lo cambio
                        Estado nuevoEstado = null;
                        try {
                            nuevoEstado = getEstadoById(nuevoIdEstadoReclamo);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        reclamo.setEstado(nuevoEstado);
                        new HttpAsyncTask(FormReclamo.this).execute(reclamo, HttpAsyncTask.ACTUALIZAR, 0);
                    }
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            mostrarDatosReclamo();
                        }
                    });
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
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
        frmReclamoLblFoto = (TextView) findViewById(R.id.frmReclamoLblFoto);
        btnPlayAudio = (Button) findViewById(R.id.frmReclamoPlayAudio);
    }

    private void setearListeners() {
        btnElegirLugar.setOnClickListener(new ElegirLugarListener());
        btnGuardar.setOnClickListener(new GuardarListener());
        btnEliminar.setOnClickListener(new EliminarListener());
        btnCancelar.setOnClickListener(new CancelarListener());
        btnCargarFoto.setOnClickListener(new CargarFotoListener());
        btnGrabarAudio.setOnClickListener(new GrabarAudioListener());
        btnPlayAudio.setOnClickListener(new PlayAudioListener());
    }

    private void inicializarSpinner(final int selectedPosition) {
        listaTiposReclamo = new ArrayList<>();
        adapterTiposReclamo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listaTiposReclamo);
        spinnerTipoReclamo.setAdapter(adapterTiposReclamo);

        Runnable r = new Runnable(){
            @Override
            public void run() {
                List<TipoReclamo> rec = reclamoDao.tiposReclamo();
                listaTiposReclamo.clear();
                listaTiposReclamo.addAll(rec);
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        adapterTiposReclamo.notifyDataSetChanged();
                        if(selectedPosition >= 0)
                            spinnerTipoReclamo.setSelection(selectedPosition);
                    }
                });
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
        int indexTipoReclamo = listaTiposReclamo.indexOf(reclamo.getTipo());
        inicializarSpinner(adapterTiposReclamo.getPosition(listaTiposReclamo.get(indexTipoReclamo)));
        // Mostrar o no la imagen
        try {
            loadImageFromStorage(reclamo.getId());
            imgFotoReclamo.setVisibility(View.VISIBLE);
            frmReclamoLblFoto.setVisibility(View.VISIBLE);
        } catch(FileNotFoundException e) {
            imgFotoReclamo.setVisibility(View.INVISIBLE);
            frmReclamoLblFoto.setVisibility(View.INVISIBLE);
        }
        // Habilitar o no audio
        File directory = getApplicationContext().getDir("audios", Context.MODE_PRIVATE);
        if(!directory.exists())
            directory.mkdir();
        audio = new File(directory, "reclamo_" + reclamo.getId() + ".3gp");
        btnPlayAudio.setEnabled(false);
        if(audio.exists())
            btnPlayAudio.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    private class ElegirLugarListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(FormReclamo.this, MapsActivity.class);
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
                Estado estado = null; //El estado para un nuevo reclamo es "enviado" y tiene id 1
                int id = 0;
                try {
                    id = obtenerNuevoID();
                    estado = getEstadoById(1);
                } catch(ExecutionException e) {
                    e.printStackTrace();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                Date fecha = new Date();

                // creo el reclamo y lo paso a la capa dao para guardar
                Reclamo nuevoReclamo = new Reclamo(id, titulo, detalle, fecha, tipoReclamo, estado, lugar);
                new HttpAsyncTask(FormReclamo.this).execute(nuevoReclamo, HttpAsyncTask.CREAR, 0);
            } else {
                // seteo los atributos del reclamo existente y lo paso a la capa dao para actualizar
                reclamo.setTitulo(titulo);
                reclamo.setDetalle(detalle);
                reclamo.setTipo(tipoReclamo);
                reclamo.setLugar(lugar);

                new HttpAsyncTask(FormReclamo.this).execute(reclamo, HttpAsyncTask.ACTUALIZAR, 0);
            }

            setResult(RESULT_OK, intentOrigen);
        }
    }

    private class EliminarListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            new HttpAsyncTask().execute(reclamo, HttpAsyncTask.BORRAR, 0);
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
    public void onTaskCompleted() {
        finish();
    }

    private class CargarFotoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(hasPermission(Manifest.permission.CAMERA)){
                dispatchTakePictureIntent();
            } else {
                askForPermission(Manifest.permission.CAMERA, FormReclamo.PERMISSION_REQUEST_CAMERA,
                        getString(R.string.solicitud_permiso_foto));
            }
        }
    }

    private class GrabarAudioListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (grabando) {
                grabando = false;
                stopRecordingAudio();
            } else {
                if(hasPermission(Manifest.permission.RECORD_AUDIO)){
                    grabando = true;
                    startRecordingAudio();
                } else {
                    askForPermission(Manifest.permission.RECORD_AUDIO,
                            FormReclamo.PERMISSION_REQUEST_VOICE, getString(R.string.solicitud_permiso_audio));
                }
            }
        }
    }

    private class PlayAudioListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(reproduciendo) {
                btnPlayAudio.setText("PLAY AUDIO");
                reproduciendo=false;
                stopPlayingAudio();
            } else {
                btnPlayAudio.setText("PAUSAR");
                reproduciendo=true;
                startPlayingAudio();
            }
        }
    }

    public boolean hasPermission(final String permisoManifest){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // la versión alcanza con tenerlo declarado
            return true;
        }
        if (ContextCompat.checkSelfPermission(FormReclamo.this, permisoManifest)
                == PackageManager.PERMISSION_GRANTED) {
            // El permiso ya esta dado
            return true;
        }
        return false;
    }

    public void askForPermission(final String permisoManifest, final int codigoPermiso, String rationaleMsgStr){
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
                    startRecordingAudio();
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

    private void loadImageFromStorage(int idReclamo) throws FileNotFoundException {
        File directory = getApplicationContext().getDir("imagenes", Context.MODE_PRIVATE);
        File f = new File(directory, "reclamo_" + idReclamo + ".jpg");
        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
        imgFotoReclamo.setImageBitmap(b);
    }

    private void startRecordingAudio() {
        btnGrabarAudio.setText("PARAR");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audio.getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start();
    }

    private void stopRecordingAudio() {
        btnGrabarAudio.setText("GRABAR AUDIO");
        recorder.stop();
        recorder.release();
        recorder = null;
        btnPlayAudio.setEnabled(true);
    }

    private void startPlayingAudio() {
        player = new MediaPlayer();
        try {
            player.setDataSource(audio.getAbsolutePath());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlayingAudio() {
        player.release();
        player = null;
    }

    private int obtenerNuevoID() throws ExecutionException, InterruptedException {
        List<Reclamo> reclamos;
        HttpAsyncTask as = new HttpAsyncTask();

        reclamos = (List<Reclamo>) as.execute(null, HttpAsyncTask.GET_ALL_RECLAMOS, 0).get();

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
        estado = (Estado) as.execute(null, HttpAsyncTask.GET_ESTADO_BY_ID, id).get();
        return estado;
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
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgFotoReclamo.setImageBitmap(imageBitmap);
                imgFotoReclamo.setVisibility(View.VISIBLE);
                frmReclamoLblFoto.setVisibility(View.VISIBLE);
                saveImageInStorage(imageBitmap);
                break;
        }
    }
}
