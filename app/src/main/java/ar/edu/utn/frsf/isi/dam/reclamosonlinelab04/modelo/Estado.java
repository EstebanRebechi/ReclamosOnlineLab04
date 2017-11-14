package ar.edu.utn.frsf.isi.dam.reclamosonlinelab04.modelo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mdominguez on 26/10/17.
 */

public class Estado implements Parcelable {
    private Integer id;
    private String tipo;

    public Estado() {

    }

    public Estado(Integer id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    protected Estado(Parcel in) {
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

    public static final Creator<Estado> CREATOR = new Creator<Estado>() {
        @Override
        public Estado createFromParcel(Parcel in) {
            return new Estado(in);
        }

        @Override
        public Estado[] newArray(int size) {
            return new Estado[size];
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
        return "Estado{" +
                "tipo='" + tipo + '\'' +
                '}';
    }
}
