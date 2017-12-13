package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class ReclamosInstanceId extends FirebaseInstanceIdService {

    public ReclamosInstanceId(){
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Refreshed token: SERVICIO CREADO!!!!"); // TODO
    }

    @Override
    public void onTokenRefresh() {
        // obtiene el token que lo identifica
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("LAB04-3::", "Refreshed token: " + refreshedToken);
        System.out.println("aca onTokenRefresh" ); //TODO
        System.out.println(refreshedToken); //TODO
        guardarToken(refreshedToken);
    }
    private void guardarToken(String tkn){
        // guardarlo en un archivo
        // o en el servidor con un POST asociando un
        // nombre de usuario ficticio y hardcoded
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("registration_id", tkn);
        editor.apply();
    }

    private String getTokenFromPrefs(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("registration_id", null);
    }
}