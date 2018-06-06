package cat.detemporada.models;

public class ResultatClarifai {

    private Producte producte;
    private float estimacio;
    private boolean seleccionat;

    public ResultatClarifai(Producte producte, float estimacio) {
        this.producte = producte;
        this.estimacio = estimacio;
        this.seleccionat = false;
    }

    public Producte getProducte() {
        return producte;
    }

    public void setProducte(Producte producte) {
        this.producte = producte;
    }

    public float getEstimacio() {
        return estimacio;
    }

    public void setEstimacio(float estimacio) {
        this.estimacio = estimacio;
    }

    public boolean isSeleccionat() {
        return seleccionat;
    }

    public void setSeleccionat(boolean seleccionat) {
        this.seleccionat = seleccionat;
    }
}
