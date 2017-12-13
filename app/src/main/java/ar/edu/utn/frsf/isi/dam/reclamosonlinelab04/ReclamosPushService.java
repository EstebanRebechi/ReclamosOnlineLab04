package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class ReclamosPushService extends FirebaseMessagingService {
    public ReclamosPushService() {
        System.out.println("aca pushh" );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Refreshed token: SERVICIO caca CREADO!!!!"); // TODO
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        System.out.println("aca ON MESSAGE RECEIVED " ); //TODO
        System.out.println(remoteMessage); //TODO
        sendNotification(remoteMessage.getData());
    }

    private void sendNotification(Map<String, String> data) {

        /**
         * El mensaje envia el id del reclamo y el id del nuevo estado.
         * lo que debera hacer es generar una notificacion indicando dicha actualizacion
         * y cuando dicha notificacion se abre, se muestra el formulario detalle del reclamo.
         */

        int notificationId = 001;
        Intent resultIntent = new Intent (this, FormReclamo.class);

        System.out.println("aca sendNotification " ); //TODO
        System.out.println(data);

        Integer reclamoId = Integer.parseInt(data.get("idReclamo"));
        Integer reclamoEstadoId = Integer.parseInt(data.get("idEstadoReclamo"));
        resultIntent.putExtra("idReclamo",reclamoId);
        resultIntent.putExtra("idEstadoReclamo",reclamoEstadoId);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("Reclamo Actualizado")
                .setContentText("El reclamo " + reclamoId + " paso al ESTADO " + reclamoEstadoId)
                .setContentIntent(resultPendingIntent);
        notifyMgr.notify(notificationId, builder.build());
    }
}
