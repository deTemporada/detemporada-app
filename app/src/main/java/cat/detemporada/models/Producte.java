package cat.detemporada.models;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;

import java.util.List;

import com.orm.annotation.Ignore;

public class Producte extends SugarRecord implements Comparable<Producte> {

    @Ignore
    String TAG = "Classe Producte";

    private String nom;
    private String descripcio;
    private CategoriaProducte categoria;
    private Unitat unitatDefecte;
    private String nomClarifai;

    @Ignore
    private Imatge imatgePrincipal;

    @Ignore
    private List<Temporada> temporades;

    public Producte() {}

    public Producte( String nom ) {
        this.nom = nom;
    }

    public Producte(String nom, CategoriaProducte categoria, Unitat unitatDefecte, boolean atemporal, Imatge imatgePrincipal, List<Temporada> temporades) {
        this.nom = nom;
        this.categoria = categoria;
        this.unitatDefecte = unitatDefecte;
        this.imatgePrincipal = imatgePrincipal;
        this.temporades = temporades;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCategoria(CategoriaProducte categoria) {
        this.categoria = categoria;
    }

    public Unitat getUnitatDefecte() {
        return unitatDefecte;
    }

    public void setUnitatDefecte(Unitat unitatDefecte) {
        this.unitatDefecte = unitatDefecte;
    }

    public Imatge getImatgePrincipal() {
        return imatgePrincipal;
    }

    public Imatge loadImatgePrincipal() {
        if (imatgePrincipal == null) {
            List<Imatge> results = Imatge.findWithQuery(Imatge.class, "SELECT ID, ORIGINAL, LOCAL FROM IMATGE WHERE PRODUCTE = ? AND PRINCIPAL = ?", this.getId().toString(), "1");
            if (!results.isEmpty()) {
                imatgePrincipal = results.get(0);
            } else {
                return null;
            }
        }
        return imatgePrincipal;
    }

    public void setImatgePrincipal(Imatge imatgePrincipal) {
        this.imatgePrincipal = imatgePrincipal;
    }

    public List<Temporada> loadTemporades() {
        if (temporades == null) {
            temporades = Temporada.find(Temporada.class, "producte = ? order by inici asc", this.getId().toString());
        }
        return temporades;
    }

    public String getDescripcio() {
        return descripcio;
    }

    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }

    public String getNomClarifai() {
        return nomClarifai;
    }

    public void setNomClarifai(String nomClarifai) {
        this.nomClarifai = nomClarifai;
    }

    @Override
    public String toString() { return this.getId().toString(); }


    public int compareTo(@NonNull Producte producte) {
        if(this.nom != null && producte.nom != null){
            return this.nom.compareToIgnoreCase(producte.nom);
        }
        return 0;
    }
}
