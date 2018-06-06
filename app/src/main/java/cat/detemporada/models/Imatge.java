package cat.detemporada.models;

import com.orm.SugarRecord;

import java.io.File;

public class Imatge extends SugarRecord {
    private String original;
    private String local;
    private Producte producte;
    private Recepta recepta;
    private boolean principal;

    public Imatge() {}

    public Imatge(File arxiu) {
        this.local = arxiu.getAbsolutePath();
    }

    public String getPath() {
        if(local == null || local.equals("")) {
            return original;
        } else {
            return "file:".concat(local);
        }
    }

    public String getOriginal() {
        return original;
    }

    public String getLocal() {
        return local;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Producte getProducte() {
        return producte;
    }

    public void setProducte(Producte producte) {
        this.producte = producte;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public Recepta getRecepta() {
        return recepta;
    }

    public void setRecepta(Recepta recepta) {
        this.recepta = recepta;
    }

}
