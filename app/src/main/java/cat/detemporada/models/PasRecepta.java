package cat.detemporada.models;

import com.orm.SugarRecord;

public class PasRecepta extends SugarRecord {
    private int posicio;
    private String pas;
    private Imatge imatge;
    private Recepta recepta;

    public PasRecepta() {}

    public PasRecepta(String pas) {
        this.pas = pas;
    }

    public String getPas() {
        return pas;
    }

    public void setPas(String pas) {
        this.pas = pas;
    }

    public Imatge getImatge() {
        return imatge;
    }

    public void setImatge(Imatge imatge) {
        this.imatge = imatge;
    }

    public int getPosicio() {
        return posicio;
    }

    public void setPosicio(int posicio) {
        this.posicio = posicio;
    }

    public Recepta getRecepta() {
        return recepta;
    }

    public void setRecepta(Recepta recepta) {
        this.recepta = recepta;
    }
}
