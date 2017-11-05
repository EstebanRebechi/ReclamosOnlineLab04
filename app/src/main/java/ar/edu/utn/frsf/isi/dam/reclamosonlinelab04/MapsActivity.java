package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String LUGAR_KEY = "lugar";
    private GoogleMap mMap;
    private Button btn_listo;

    private Intent intentOrigen;

    private LatLng lugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_listo = (Button) findViewById(R.id.buttonListo);
        btn_listo.setOnClickListener(new Listolistener());

        intentOrigen = getIntent();

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
        Integer zoom;
        if(lugar == null) { // Si no se pasó ningún lugar al crear la actividad
            // agrego un marker aproximadamente al centro de Santa Fe
            // y seteo el zoom para centrarlo en toda la ciudad
            lugar = new LatLng(-31.621, -60.704);
            zoom = 13;
        } else { // si se pasó uno, seteo el zoom más cerca de ese lugar
            zoom = 16;
        }
        mMap.addMarker(new MarkerOptions().position(lugar).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lugar, zoom));

        mMap.setOnMarkerDragListener(new onMarkerDrag());
        mMap.getUiSettings().setZoomControlsEnabled(true);
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
