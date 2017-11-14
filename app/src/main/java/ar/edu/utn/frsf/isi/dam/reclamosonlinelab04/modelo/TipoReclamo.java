package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mdominguez on 26/10/17.
 */

public class TipoReclamo implements Parcelable {
    private Integer id;
    private String tipo;

    public TipoReclamo() {

    }

    public TipoReclamo(Integer id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    protected TipoReclamo(Parcel in) {
        id = in.readInt();
        tipo = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(tipo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TipoReclamo> CREATOR = new Creator<TipoReclamo>() {
        @Override
        public TipoReclamo createFromParcel(Parcel in) {
            return new TipoReclamo(in);
        }

        @Override
        public TipoReclamo[] newArray(int size) {
            return new TipoReclamo[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return tipo;
    }
}
