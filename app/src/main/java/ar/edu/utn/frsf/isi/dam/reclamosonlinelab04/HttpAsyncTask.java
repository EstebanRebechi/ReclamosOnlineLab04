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
                case 1:
                    reclamoDao.crear(nuevoReclamo);
                    break;
                case 2:
                    reclamoDao.actualizar(nuevoReclamo);
                    break;
                case 3:
                    reclamoDao.borrar(nuevoReclamo);
                    break;
                case 4:
                    reclamos = reclamoDao.reclamos();
                    return reclamos;
                case 5:
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
