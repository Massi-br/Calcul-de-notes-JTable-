package notes.model;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.regex.Pattern;

/**
 * @inv
 *     getNoteTableModel() != null
 *     Let tm ::= getNoteTableModel
 *         n  ::= tm.getRowCount()
 *         p  ::= ColumnFeature.POINTS.ordinal()
 *         c  ::= ColumnFeature.COEF.ordinal()
 *     getPoints() == sum(int i, 0 <= i < n : tm.getValueAt(i, p))
 *     n > 0 ==> getMean() == getPoints()
 *                       / sum(int i, 0 <= i < n : tm.getValueAt(i, c))
 *     n == 0 ==> getMean() == Double.NaN
 */
public interface NoteSheetModel {

    // CONSTANTES STATIQUES
    
    String PROP_SAVED = "saved";
    String PROP_PROGRESS = "progress";
    String PROP_FAILURE = "failure";

    char COMMENT_CHAR = '#';
    String FIELD_SEP = "\t";
    Pattern LINE_PAT = Pattern.compile("^"
            + "[^" + FIELD_SEP + "]*"
            + "(" + FIELD_SEP + "\\d+(\\.\\d+)?){2}"
            + "(" + FIELD_SEP + ".*)*"
            + "$");

    // REQUETES

    /**
     * La moyenne des notes du modèle, en tenant compte des coefficients.
     */
    double getMean();
    
    /**
     * Le modèle de table associé à ce modèle.
     */
    NoteTableModel getNoteTableModel();

    /**
     * Le nombre de points correspondants aux notes stockées dans le modèle,
     *  calculé comme la somme des (coef * note).
     */
    double getPoints();
    
    /**
     * Les PropertyChangeListeners enregistrés pour la propriété prop.
     * @pre
     *     prop != null
     */
    PropertyChangeListener[] getPropertyChangeListeners(String prop);

    // COMMANDES

    /**
     * Installe un PropertyChangeListener pour la propriété prop.
     * @pre
     *     prop != null && lnr != null
     * @post
     *     lnr a été ajouté à la séquence des écouteurs sur prop
     */
    void addPropertyChangeListener(String prop, PropertyChangeListener lnr);

    /**
     * Charge un fichier de notes dans le modèle.
     * Au cours du chargement, des modifications de valeur pour les propriétés
     *   forcées progress et failure auront lieu.<br />
     * <em>Les réalisations de cette méthode doivent garantir que :
     * <ul>
     *   <li>elles ne surchargent pas EDT</li>
     *   <li>les mises à jour du modèle se font sur EDT</li>
     * </ul>
     * </em>
     * @pre
     *     f != null
     * @post
     *     le modèle contient les données (lignes de f reconnues par LINE_PAT)
     */
    void loadTableFromFile(File f);

    /**
     * Désinstalle le PropertyChangeListener pour la propriété indiquée.
     * @pre
     *     prop != null && lnr != null
     * @post
     *     lnr a été retiré de la séquence des écouteurs sur prop
     */
    void removePropertyChangeListener(String prop, PropertyChangeListener lnr);

    /**
     * Enregistre les notes dans un fichier de texte.
     * Au cours de l'enregistrement, des modifications de valeur pour les
     *  propriétés forcées saved, progress et failure auront lieu.<br />
     * <em>Les réalisations de cette méthode doivent garantir que :
     * <ul>
     *   <li>elles ne surchargent pas EDT</li>
     *   <li>les mises à jour du modèle se font sur EDT</li>
     * </ul>
     * </em>
     * @pre
     *     f != null
     * @post
     *     f est un fichier texte contenant toutes les données du modèle
     *     f commence par :
     *         COMMENT_CHAR + " "
     *          + ColumnFeature.SUBJECT.header() + FIELD_SEP
     *          + ColumnFeature.COEF.header() + FIELD_SEP
     *          + ColumnFeature.MARK.header()
     *     suivi d'une ligne vierge
     *     suivi de getRowCount() lignes reconnues par LINE_PAT
     */
    void saveTableToFile(File f);
}
