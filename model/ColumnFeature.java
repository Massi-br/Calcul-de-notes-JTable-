package notes.model;

/**
 * Définition des caractéristiques de chaque colonne d'un NoteTableModel.
 * String header() : l'entête de la colonne
 * Class<?> type() : le type des éléments de la colonne
 * Object value(String) : la valeur du paramètre convertie en élément
 *   de type type()
 * Object defaultValue() : la valeur par défaut (de type type()) des éléments
 *   de cette colonne
 */
public enum ColumnFeature {

    SUBJECT("Matières", String.class) {
        @Override String parseValue(String v) {
            if (v == null) {
                return DEFAULT_TEXT;
            }
            return v;
        }
        @Override String defaultValue() {
            return DEFAULT_TEXT;
        }
    },

    COEF("Coefficients", Double.class) {
        @Override Double parseValue(String v) {
            return parseToNumeric(v);
        }
        @Override Double defaultValue() {
            return DEFAULT_NUMERIC;
        }
    },

    MARK("Notes / 20", Double.class) {
        @Override Double parseValue(String v) {
            return parseToNumeric(v);
        }
        @Override Double defaultValue() {
            return DEFAULT_NUMERIC;
        }
    },

    POINTS("Points", Double.class) {
        @Override Object parseValue(String v) {
            // l'usage de cette méthode n'a pas de sens dans le cas de POINTS
            // car la valeur souhaitée n'est pas accessible à partir de v,
            // elle doit être calculée à partir de deux valeurs, en cellules
            // COEF et MARK
            throw new UnsupportedOperationException();
        }
        @Override Double defaultValue() {
            return DEFAULT_NUMERIC;
        }
    };
    
    // CONSTANTES STATIQUES
    
    private static final String DEFAULT_TEXT = "";
    private static final Double DEFAULT_NUMERIC = new Double(0.0);

    // ATTRIBUTS

    private String headerName;
    private Class<?> cellType;

    // CONSTRUCTEURS

    ColumnFeature(String n, Class<?> c) {
        headerName = n;
        cellType = c;
    }

    // REQUETES

    /**
     * Valeur par défaut pour cette colonne.
     */
    abstract Object defaultValue();

    /**
     * Traduit v en une valeur du bon type pour cette colonne.
     */
    abstract Object parseValue(String v);

    /**
     * Entête de cette colonne.
     */
    String headerName() {
        return headerName;
    }

    /**
     * Type des cellules constituant cette colonne.
     */
    Class<?> cellType() {
        return cellType;
    }
    
    private static Double parseToNumeric(String v) {
        Double d = null;
        try {
            d = Double.valueOf(v.trim());
        } catch (Exception e) {
            d = DEFAULT_NUMERIC;
        }
        return d;
    }
}
