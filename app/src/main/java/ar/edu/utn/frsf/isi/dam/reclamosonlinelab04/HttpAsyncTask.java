package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04;

import android.os.AsyncTask;
import android.text.BoringLayout;

import java.util.List;

import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDao;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.dao.ReclamoDaoHTTP;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Estado;
import ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo.Reclamo;

/**
 * Created by augusto on 07/11/2017.
 */

public class HttpAsyncTask extends AsyncTask {

    public static final int CREAR = 1;
    public static final int ACTUALIZAR = 2;
    public static final int BORRAR = 3;
    public static final int GET_ALL_RECLAMOS = 4;
    public static final int GET_ESTADO_BY_ID = 5;
    public static final int GET_RECLAMO_BY_ID = 6;
    private OnTaskCompleted listener = null;

    public HttpAsyncTask(OnTaskCompleted listener){
        this.listener=listener;
    }

    public HttpAsyncTask(){
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ReclamoDao reclamoDao = new ReclamoDaoHTTP();
        Reclamo nuevoReclamo = (Reclamo) objects[0];
        int opcion = (int)objects[1];
        int id = (int)objects[2];

        List<Reclamo> reclamos;
        Reclamo reclamo;
        Estado estado;
        try {
            switch (opcion){
                case HttpAsyncTask.CREAR:
                    reclamoDao.crear(nuevoReclamo);
                    break;
                case HttpAsyncTask.ACTUALIZAR:
                    reclamoDao.actualizar(nuevoReclamo);
                    break;
                case HttpAsyncTask.BORRAR:
                    reclamoDao.borrar(nuevoReclamo);
                    break;
                case HttpAsyncTask.GET_ALL_RECLAMOS:
                    reclamos = reclamoDao.reclamos();
                    return reclamos;
                case HttpAsyncTask.GET_ESTADO_BY_ID:
                    estado = reclamoDao.getEstadoById(id);
                    return estado;
                case HttpAsyncTask.GET_RECLAMO_BY_ID:
                    reclamo = reclamoDao.getReclamoById(id);
                    return reclamo;
                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Object o){
        if(listener!=null)
            listener.onTaskCompleted();
    }

}
