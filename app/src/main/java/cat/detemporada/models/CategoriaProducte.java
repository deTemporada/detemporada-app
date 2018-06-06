package cat.detemporada.models;

// Enum que representa les categories d'un producte. Finalment no s'han fet servir
// en l'àmbit d'aquest treball però serviran per futures millores.

public enum CategoriaProducte {
    FRUITA("Fruita", 0),
    VERDURA("Verdura", 1),
    LLEGUMS("Llegums", 2),
    CEREALS("Cereals", 3),
    LACTICS("Làctics", 4),
    CARN("Carn", 5),
    PEIX("Peix", 6),
    FRUITSSECS("Fruits secs", 7),
    ALTRES("Altres", 8);

    private String stringValue;
    int intValue;
    CategoriaProducte(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    public static CategoriaProducte fromString(String text) {
        for (CategoriaProducte categoria : CategoriaProducte.values()) {
            if (categoria.stringValue.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("No s'ha trobat cap categoria: " + text);
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
