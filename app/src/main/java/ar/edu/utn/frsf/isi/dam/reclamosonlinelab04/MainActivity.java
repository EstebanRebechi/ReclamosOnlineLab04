package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;

public class MainActivity extends AppCompatActivity {

    public static final int NUEVO_RECLAMO_REQUEST = 1;
    public static final int VER_RECLAMO_REQUEST = 2;

    private ReclamoDao daoReclamo;
    private ListView listViewReclamos;
    private List<Reclamo> listaReclamos;
    private ReclamoAdapter adapter;
    private Button btnNuevoReclamo;
    private Button btnVerTodosEnMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkGooglePlayServices();
        daoReclamo = new ReclamoDaoHTTP();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listViewReclamos = (ListView) findViewById(R.id.mainListaReclamos);
        listaReclamos = new ArrayList<>();
        adapter = new ReclamoAdapter(this, listaReclamos);
        listViewReclamos.setAdapter(adapter);
        listViewReclamos.setOnItemLongClickListener(new reclamosListener());

        obtenerReclamos();

        btnNuevoReclamo = (Button) findViewById(R.id.btnNuevoReclamo);
        btnNuevoReclamo.setOnClickListener(new NuevoReclamoListener());
        btnVerTodosEnMapa = (Button) findViewById(R.id.btnVerTodos);
        btnVerTodosEnMapa.setOnClickListener(new VerTodosEnMapaListener());
    }

    @Override
    public void onResume(){
        super.onResume();
        checkGooglePlayServices();
    }

    private void checkGooglePlayServices() {
        int gServicesCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        System.out.println("Google Play services status: " + GoogleApiAvailability.getInstance().getErrorString(gServicesCode));
        if(gServicesCode != 0)
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
    }

    private void obtenerReclamos() {
        listaReclamos.clear();
        adapter.notifyDataSetChanged();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<Reclamo> rec = daoReclamo.reclamos();
                System.out.println("Cantidad de reclamos: " + rec.size());
                for(Reclamo r: rec){
                    System.out.println("Reclamo Titulo: " + r.getTitulo());
                }
                listaReclamos.addAll(rec);
                runOnUiThread(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private class NuevoReclamoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, FormReclamo.class);
            startActivityForResult(intent, NUEVO_RECLAMO_REQUEST);
        }
    }

    private class reclamosListener implements android.widget.AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            Reclamo reclamoSeleccionado = (Reclamo) adapterView.getItemAtPosition(i);
            Intent intent = new Intent(MainActivity.this, FormReclamo.class);
            intent.putExtra("idReclamo", reclamoSeleccionado.getId());
            startActivityForResult(intent, VER_RECLAMO_REQUEST);
            return true;
        }
    }

    private class VerTodosEnMapaListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(listaReclamos.isEmpty()) {
                Toast.makeText(MainActivity.this, R.string.No_hay_reclamos, Toast.LENGTH_LONG).show();
            } else {
                ArrayList<Reclamo> listaReclamosConLugares = new ArrayList<>();
                for(Reclamo reclamo : listaReclamos) {
                    if(reclamo.getLugar() != null) {
                        listaReclamosConLugares.add(reclamo);
                    }
                }
                if(listaReclamosConLugares.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.No_hay_lugares, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra(MapsActivity.LISTA_RECLAMOS_KEY, listaReclamosConLugares);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case NUEVO_RECLAMO_REQUEST: {
                switch(resultCode) {
                    case RESULT_OK: {
                        obtenerReclamos();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.Nuevo_Reclamo_Guardado), Toast.LENGTH_LONG).show();
                        break;
                    }
                    case RESULT_CANCELED: {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.Nuevo_Reclamo_Cancelado), Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                break;
            }
            case VER_RECLAMO_REQUEST: {
                switch(resultCode) {
                    case RESULT_OK: {
                        obtenerReclamos();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.Reclamo_Actualizado), Toast.LENGTH_LONG).show();
                        break;
                    }
                    case RESULT_CANCELED: {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.Actualizacion_Reclamo_Cancelada), Toast.LENGTH_LONG).show();
                        break;
                    }
                    case FormReclamo.RESULT_DELETED: {
                        obtenerReclamos();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.Reclamo_Eliminado), Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                break;
            }
        }
    }
}
