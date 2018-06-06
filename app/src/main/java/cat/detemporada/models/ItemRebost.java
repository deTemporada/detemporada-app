package cat.detemporada.models;

import com.orm.SugarRecord;

import java.util.Locale;

public class ItemRebost extends SugarRecord {
    private String nom;
    private float quantitat;
    private Unitat unitat;
    private Producte producte;
    private ItemLlistaCompra itemLlistaCompra;

    public ItemRebost() {}

    public ItemRebost(ItemLlistaCompra itemLlistaCompra) {
        this.itemLlistaCompra = itemLlistaCompra;
        this.producte = itemLlistaCompra.getProducte();
        this.nom = itemLlistaCompra.getNom();
        this.quantitat = itemLlistaCompra.getQuantitat();
        this.unitat = itemLlistaCompra.getUnitat();
    }

    public ItemRebost(String nom, Producte producte, float quantitat, Unitat unitat) {
        this.nom = nom;
        this.producte = producte;
        this.quantitat = quantitat;
        this.unitat = unitat;
    }

    public String getNom() {
            return this.nom;
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

    public String getQuantitatFormatted() {
        if(this.quantitat == 0f) {
            return this.unitat.toString();
        } else {
            String valor = this.quantitat % 1 == 0 ? String.format(Locale.getDefault(),"%.0f", this.quantitat) : String.format(Locale.getDefault(),"%.2f", this.quantitat);
            return valor + " " + this.unitat.toString();
        }
    }

    public ItemLlistaCompra getItemLlistaCompra() {
        return itemLlistaCompra;
    }

    public void setItemLlistaCompra(ItemLlistaCompra itemLlistaCompra) {
        this.itemLlistaCompra = itemLlistaCompra;
    }
}
