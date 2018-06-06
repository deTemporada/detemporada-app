package cat.detemporada.models;

public enum Unitat {
    MILILITRES("ml", 0),
    LITRES("l", 1),
    KILOGRAMS("kg", 2),
    GRAMS("g", 3),
    UNITATS("u", 4),
    QS("q.s.", 5);

    private String stringValue;
    int intValue;
    Unitat(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    public static Unitat fromString(String text) {
        for (Unitat unitat : Unitat.values()) {
            if (unitat.stringValue.equalsIgnoreCase(text)) {
                return unitat;
            }
        }
        throw new IllegalArgumentException("No s'ha trobat cap unitat: " + text);
    }
    @Override
    public String toString() {
        return stringValue;
    }

}
