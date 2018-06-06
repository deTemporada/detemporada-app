package cat.detemporada.models;

import com.orm.SugarRecord;
import com.orm.annotation.Ignore;

import java.util.List;


public class Recepta extends SugarRecord {
    public enum Dificultat {
        BAIXA("Baixa", 0),
        MITJANA("Mitjana", 1),
        ALTA("Alta", 2);

        private String stringValue;
        int intValue;
        Dificultat(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        public static Dificultat fromString(String text) {
            for (Dificultat grauDificultat : Dificultat.values()) {
                if (grauDificultat.stringValue.equalsIgnoreCase(text)) {
                    return grauDificultat;
                }
            }
            throw new IllegalArgumentException("No s'ha trobat cap grau: " + text);
        }
        @Override
        public String toString() {
            return stringValue;
        }
    }

    private String nom;
    private String descripcio;
    private int persones;
    private int temps;
    private CategoriaRecepta categoria;
    private int indexTemporalitat;
    private float valoracio;
    private Dificultat dificultat;
    private boolean preferida;
    private boolean usuari;
    private boolean esborrany;

    @Ignore
    private Imatge imatgePrincipal;

    @Ignore
    private List<Imatge> imatges;

    @Ignore
    private List<PasRecepta> instruccions;

    @Ignore
    private List<Ingredient> ingredients;

    public Recepta() {}

    public Recepta(String nom, String descripcio, List<PasRecepta> instruccions, int persones, int temps, CategoriaRecepta categoria, Dificultat dificultat) {
        this.nom = nom;
        this.descripcio = descripcio;
        this.instruccions = instruccions;
        this.persones = persones;
        this.temps = temps;
        this.categoria = categoria;
        this.dificultat = dificultat;
        this.preferida = false;
        this.valoracio = 5f;
    }

    public Recepta(boolean usuari, boolean esborrany) {
        this.nom = "";
        this.descripcio = "";
        this.usuari = usuari;
        this.esborrany = esborrany;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescripcio() {
        return descripcio;
    }

    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }

    public int getPersones() {
        return persones;
    }

    public void setPersones(int persones) {
        this.persones = persones;
    }

    public int getTemps() {
        return temps;
    }

    public void setTemps(int temps) {
        this.temps = temps;
    }

    public CategoriaRecepta getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaRecepta categoria) {
        this.categoria = categoria;
    }

    public int getIndexTemporalitat() {
        return indexTemporalitat;
    }

    public void setIndexTemporalitat(int indexTemporalitat) {
        this.indexTemporalitat = indexTemporalitat;
    }

    public float getValoracio() {
        return valoracio;
    }

    public void setValoracio(float valoracio) {
        this.valoracio = valoracio;
    }

    public Dificultat getDificultat() {
        return dificultat;
    }

    public void setDificultat(Dificultat dificultat) {
        this.dificultat = dificultat;
    }

    public Imatge loadImatgePrincipal() {
        if (imatgePrincipal == null) {
            List<Imatge> results = Imatge.findWithQuery(Imatge.class, "select id, original, local from imatge where recepta = ? and principal = ?", this.getId().toString(), "1");
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

    public List<Imatge> getImatges() {
        return imatges;
    }

    public void setImatges(List<Imatge> imatges) {
        this.imatges = imatges;
    }

    public List<PasRecepta> getInstruccions() {
        return instruccions;
    }

    public void setInstruccions(List<PasRecepta> instruccions) {
        this.instruccions = instruccions;
    }

    public List<Ingredient> loadIngredients() {
        if (ingredients == null) {
            ingredients = Ingredient.find(Ingredient.class, "recepta = ? order by id asc", this.getId().toString());
        }
        return ingredients;
    }

    public List<PasRecepta> loadInstruccions() {
        if (instruccions == null) {
            instruccions = PasRecepta.find(PasRecepta.class, "recepta = ? order by posicio asc", this.getId().toString());
        }
        return instruccions;
    }

    public boolean isPreferida() {
        return preferida;
    }

    public void setPreferida(boolean preferida) {
        this.preferida = preferida;
    }

    public boolean isUsuari() {
        return usuari;
    }

    public void setUsuari(boolean usuari) {
        this.usuari = usuari;
    }

    public boolean isEsborrany() {
        return esborrany;
    }

    public void setEsborrany(boolean esborrany) {
        this.esborrany = esborrany;
    }

}

