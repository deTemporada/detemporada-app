package cat.detemporada.models;

import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemLlistaCompra extends SugarRecord {
    private String nom;
    private float quantitat;
    private Unitat unitat;
    private Producte producte;
    private Recepta recepta;
    private boolean afegitManualment;
    private String dataAdquisicio;

    public ItemLlistaCompra() {
    }

    public ItemLlistaCompra(String nom, Producte producte, float quantitat, Unitat unitat, boolean afegitManualment) {
        this.nom = nom;
        this.producte = producte;
        this.quantitat = quantitat;
        this.unitat = unitat;
        this.afegitManualment = afegitManualment;
    }

    public ItemLlistaCompra(Ingredient ingredient) {
        this.nom = ingredient.getNom();
        this.quantitat = ingredient.getQuantitat();
        this.unitat = ingredient.getUnitat();
        this.producte = ingredient.getProducte();
        this.recepta = ingredient.getRecepta();
        afegitManualment = false;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public float getQuantitat() {
        return quantitat;
    }

    public void setQuantitat(float quantitat) {
        this.quantitat = quantitat;
    }

    public Unitat getUnitat() {
        return unitat;
    }

    public void setUnitat(Unitat unitat) {
        this.unitat = unitat;
    }

    public Producte getProducte() {
        return producte;
    }

    public void setProducte(Producte producte) {
        this.producte = producte;
    }

    public String getDataAdquisicio() {
        return dataAdquisicio;
    }

    public void setDataAdquisicio(Date dataAdquisicio) {
        SimpleDateFormat s = new SimpleDateFormat( "yyyyMMdd", Locale.getDefault());
        this.dataAdquisicio = s.format(dataAdquisicio);
    }

    public void deleteDataAdquisicio() {
        this.dataAdquisicio = null;
    }

    public Recepta getRecepta() {
        return recepta;
    }

    public void setRecepta(Recepta recepta) {
        this.recepta = recepta;
    }

    public boolean isAfegitManualment() {
        return afegitManualment;
    }

    public void setAfegitManualment(boolean afegitManualment) {
        this.afegitManualment = afegitManualment;
    }

    public String getQuantitatFormatted() {
        if(this.quantitat == 0f) {
            return this.unitat.toString();
        } else {
            String valor = this.quantitat % 1 == 0 ? String.format(Locale.getDefault(),"%.0f", this.quantitat) : String.format(Locale.getDefault(),"%.2f", this.quantitat);
            return valor + " " + this.unitat.toString();
        }
    }
}
