package cat.detemporada.models;

import com.orm.SugarRecord;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

// Model d'un ingredient

public class Ingredient extends SugarRecord {
    private String nom;
    private float quantitat;
    private Unitat unitat;
    private Producte producte;
    private Recepta recepta;
    private String nomNormalitzat;

    public Ingredient() {}

    public Ingredient(String nom, float quantitat, Unitat unitat) {
        this.nom = nom;
        this.quantitat = quantitat;
        this.unitat = unitat;
        String normalitzat = Normalizer.normalize(nom, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        this.nomNormalitzat = pattern.matcher(normalitzat).replaceAll("");
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
        String normalitzat = Normalizer.normalize(nom, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        this.nomNormalitzat = pattern.matcher(normalitzat).replaceAll("");
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

    public Recepta getRecepta() {
        return recepta;
    }

    public void setRecepta(Recepta recepta) {
        this.recepta = recepta;
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
