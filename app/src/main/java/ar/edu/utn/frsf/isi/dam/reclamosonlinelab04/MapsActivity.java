package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String LUGAR_KEY = "lugar";
    public static final String LISTA_RECLAMOS_KEY = "lista_lugares";

    private GoogleMap mMap;
    private Button btn_listo;
    private LinearLayout linearLayoutElegirLugar;

    private Intent intentOrigen;

    private LatLng lugar;
    private List<Reclamo> listaReclamosConLugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        linearLayoutElegirLugar = (LinearLayout) findViewById(R.id.linearLayoutElegirLugar);
        btn_listo = (Button) findViewById(R.id.buttonListo);
        btn_listo.setOnClickListener(new Listolistener());

        intentOrigen = getIntent();


        /*
        * si se desea ver todos los reclamos, se pasan los lugares como extra
        * si se desea editar el lugar de un reclamo ya existente se pasa ese lugar como extra
        * por último, si se desea asignar un lugar a un reclamo que no tenía lugar, no se pasa ningún extra
        */
        listaReclamosConLugar = intentOrigen.getParcelableArrayListExtra(LISTA_RECLAMOS_KEY);
        lugar = intentOrigen.getParcelableExtra(LUGAR_KEY);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(listaReclamosConLugar != null) {
            linearLayoutElegirLugar.setVisibility(View.GONE);
            verListaLugares();
        } else {
            linearLayoutElegirLugar.setVisibility(View.VISIBLE);
            elegirUnLugar();
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void elegirUnLugar() {
        Integer zoom;
        if(lugar == null) { // Si no se pasó ningún lugar al crear la actividad
            // creo el lugar con coordenadas aproximadamente al centro de Santa Fe
            // y seteo el zoom para centrarlo sobre toda la ciudad
            lugar = new LatLng(-31.621, -60.704);
            zoom = 13;
        } else { // si se pasó uno
            // seteo el zoom más cerca de ese lugar
            zoom = 16;
        }
        mMap.addMarker(new MarkerOptions().position(lugar).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lugar, zoom));

        mMap.setOnMarkerDragListener(new onMarkerDrag());
    }

    private void verListaLugares() {
        // valores para determinar las coordenadas de las esquinas de la visualización
        LatLng primero = listaReclamosConLugar.get(0).getLugar();
        Double sur = primero.latitude;
        Double este = primero.longitude;
        Double oeste = primero.longitude;
        Double norte = primero.latitude;

        for(Reclamo r : listaReclamosConLugar) {
            LatLng l = r.getLugar();
            if(l.latitude < sur) {
                sur = l.latitude;
            }
            if(l.latitude > norte) {
                norte = l.latitude;
            }
            if(l.longitude < oeste) {
                oeste = l.longitude;
            }
            if(l.longitude > este) {
                este = l.longitude;
            }

            mMap.addMarker(new MarkerOptions().position(l).title(r.getTitulo()));
        }
        if(listaReclamosConLugar.size() > 1) {
            // si hay más de un marcador agrego una linea desde el primer al último elemento
            mMap.addPolyline(
                    new PolylineOptions()
                            .add(listaReclamosConLugar.get(0).getLugar())
                            .add(listaReclamosConLugar.get(listaReclamosConLugar.size() - 1).getLugar())
            );
        }

        LatLng suroeste = new LatLng(sur, oeste);
        LatLng noreste = new LatLng(norte, este);
        int padding = 10;
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(suroeste, noreste), padding));
    }

    private class onMarkerDrag implements GoogleMap.OnMarkerDragListener {

        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            lugar = marker.getPosition();
        }
    }

    private class Listolistener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            intentOrigen.putExtra(LUGAR_KEY, lugar);
            setResult(RESULT_OK, intentOrigen);
            finish();
        }
    }
}
