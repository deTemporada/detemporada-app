package cat.detemporada.models;

// Enum que representa les categories de recepta

public enum CategoriaRecepta {
    ENTRANT("Entrant", 0),
    PRIMER("Primer", 1),
    SEGON("Segon", 2),
    POSTRE("Postre", 3);

    private String stringValue;
    int intValue;
    CategoriaRecepta(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    public static CategoriaRecepta fromString(String text) {
        for (CategoriaRecepta categoria : CategoriaRecepta.values()) {
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
