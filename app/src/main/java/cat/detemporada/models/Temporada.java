package cat.detemporada.models;

import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Temporada extends SugarRecord {
    private int inici;
    private int fi;
    private Producte producte;
    private boolean atemporal;

    private SimpleDateFormat s = new SimpleDateFormat("MMdd", Locale.getDefault());
    /*
     *  Degut a que SQLite no suporta el tipus Date i SugarORM converteix els atributs
     *  d'aquest tipus en Strings, és millor convertir-ho manualment en el constructor
     *  i emmagatzemar-ho com a String a la classe i BBDD. Com que no ens interessa l'any
     *  sino només el mes i el dia, emmagatzemem aquests valors. D'aquesta manera podem fer
     *  una cerca ràpida a la base de dades.
     */

    public Temporada() {}

    public Temporada(Date inici, Date fi) {
        this.inici = Integer.parseInt(s.format(inici));
        this.fi = Integer.parseInt(s.format(fi));
        this.atemporal = (this.inici == 101 && this.fi == 1231);
    }

    public Temporada(String inici, String fi) {
        this.inici = Integer.parseInt(inici);
        this.fi = Integer.parseInt(fi);
        this.atemporal = (this.inici == 101 && this.fi == 1231);
    }

    public void setInici(Date inici) {

        this.inici = Integer.parseInt(s.format(inici));
    }

    public void setFi(Date fi) {
        this.fi = Integer.parseInt(s.format(fi));
    }

    public void setIniciString(String inici) {
        this.inici = Integer.parseInt(inici);
    }

    public void setFiString(String fi) {
        this.fi = Integer.parseInt(fi);
    }

    public boolean isAtemporal() {
        return atemporal;
    }

    public void setAtemporal(boolean atemporal) {
        this.atemporal = atemporal;
    }

    public boolean checkAtemporal() {
        if (this.inici != 0 && this.fi != 0) {
            this.atemporal = (this.inici == 101 && this.fi == 1231);
            return atemporal;
        } else {
            throw new IllegalArgumentException("Manquen inici o fi");
        }
    }
    public int getInici() {
        return inici;
    }

    public int getFi() {
        return fi;
    }

    public Producte getProducte() {
        return producte;
    }

    public void setProducte(Producte producte) {
        this.producte = producte;
    }

    public boolean esTemporada(Date date) {
        int data = Integer.parseInt(s.format(date));
        return atemporal || (this.inici <= data && this.fi >= data);
    }
}
