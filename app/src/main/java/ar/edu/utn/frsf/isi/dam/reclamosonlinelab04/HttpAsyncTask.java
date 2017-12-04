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
    public static final int RECLAMOS = 4;
    public static final int ESTADO_BY_ID = 5;

    @Override
    protected Object doInBackground(Object[] objects) {

        ReclamoDao reclamoDao = new ReclamoDaoHTTP();
        Reclamo nuevoReclamo = (Reclamo) objects[0];
        int opcion = (int)objects[1];
        int id = (int)objects[2];

        List<Reclamo> reclamos;
        Estado estado;
        try {

            switch (opcion){
                case CREAR:
                    reclamoDao.crear(nuevoReclamo);
                    break;
                case ACTUALIZAR:
                    reclamoDao.actualizar(nuevoReclamo);
                    break;
                case BORRAR:
                    reclamoDao.borrar(nuevoReclamo);
                    break;
                case RECLAMOS:
                    reclamos = reclamoDao.reclamos();
                    return reclamos;
                case ESTADO_BY_ID:
                    estado = reclamoDao.getEstadoById(id);

                    return estado;
                default:
                    break;

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
