package notes.model;

import javax.swing.table.TableModel;

/**
 * Le type des modèles de données pour les JTables de l'application.
 * @inv <pre>
 *     getColumnCount() == |ColumnFeature.values()|
 *     forall f in ColumnFeature.values() :
 *         getColumnName(f.ordinal()).equals(f.headerName())
 *         getColumnClass(f.ordinal()).equals(f.cellType())
 *     getRowCount() >= 0
 *     forall int i, int j :
 *         isCellEditable(i, j) ==
 *                 (0 <= i < getRowCount() && 0 <= j < getColumnCount() - 1)
 *         (i < 0 || i >= getRowCount() || j < 0 || j >= getColumnCount())
 *             ==> getValueAt(i, j) == null
 *         (0 <= i < getRowCount() && 0 <= j < getColumnCount() - 1)
 *             ==> getValueAt(i, j) != null
 *         0 <= i < getRowCount()
 *             ==> Let p ::= ColumnFeature.POINTS.ordinal()
 *                     c ::= ColumnFeature.COEF.ordinal()
 *                     m ::= ColumnFeature.MARK.ordinal()
 *                 getValueAt(i, p).equals(
 *                     (Double) getValueAt(i, c) * (Double) getValueAt(i, m))
 * </pre>
 */
public interface NoteTableModel extends TableModel {

    // REQUETES

    /**
     * La classe des éléments situés dans les cellules de la colonne column.
     */
    @Override
    Class<?> getColumnClass(int column);

    /**
     * Le nombre de colonnes.
     */
    @Override
    int getColumnCount();

    /**
     * L'entête de la colonne column.
     */
    @Override
    String getColumnName(int column);

    /**
     * Le nombre de lignes.
     */
    @Override
    int getRowCount();

    /**
     * La valeur de la cellule en (row, column).
     */
    @Override
    Object getValueAt(int row, int column);

    /**
     * Indique si cette cellule est éditable.
     */
    @Override
    boolean isCellEditable(int row, int column);

    // COMMANDES

    /**
     * Ajoute une nouvelle ligne vide à la fin du modèle.
     * @post
     *     getRowCount() == old getRowCount() + 1
     *     les lignes du modèle entre 0 et getRowCount() - 2 n'ont pas changé
     *     Let r ::= getRowCount() - 1
     *         forall int c, 0 <= c < getColumnCount() :
     *             getValueAt(r, c) == ColumnFeature.values()[c].defaultValue()
     */
    void addEmptyRow();

    /**
     * Ajoute la ligne row à la fin du modèle.
     * @pre
     *     row != null
     *     |row| == |ColumnFeature.values()| - 1
     *     forall int i, 0 <= i < |ColumnFeature.values()| - 1 :
     *         row[i] != null
     *         row[i] est de type ColumnFeature.values[i].type()
     * @post
     *     getRowCount() == old getRowCount() + 1
     *     les lignes du modèle entre 0 et getRowCount() - 2 n'ont pas changé
     *     Let r ::= getRowCount() - 1
     *         forall int c, 0 <= c < getColumnCount() :
     *             getValueAt(r, c) == row[c]
     */
    void addRow(Object[] row);

    /**
     * Supprime toutes les lignes dans le modèle.
     * @post
     *     getRowCount() == 0
     */
    void clearRows();

    /**
     * Insère une nouvelle ligne vide dans le modèle.
     * @pre
     *     0 <= index <= getRowCount()
     * @post
     *     getRowCount() == old getRowCount() + 1
     *     les lignes du modèle entre 0 et index - 1 n'ont pas changé
     *     forall int c, 0 <= c < getColumnCount() :
     *         getValueAt(index, c) == ColumnFeature.values()[c].defaultValue()
     *     forall int i, index < i < getRowCount() :
     *         la ligne du modèle en position i correspond à l'ancienne
     *           ligne du modèle en position i - 1
     */
    void insertEmptyRow(int index);

    /**
     * Insère une nouvelle ligne vide dans le modèle.
     * @pre
     *     0 <= index <= getRowCount()
     *     row != null
     *     |row| == |ColumnFeature.values()| - 1
     *     forall int i, 0 <= i < |ColumnFeature.values()| - 1 :
     *         row[i] != null
     *         row[i] est de type ColumnFeature.values[i].type()
     * @post
     *     getRowCount() == old getRowCount() + 1
     *     les lignes du modèle entre 0 et index - 1 n'ont pas changé
     *     forall int c, 0 <= c < getColumnCount() :
     *         getValueAt(index, c) == row[c]
     *     forall int i, index < i < getRowCount() :
     *         la ligne du modèle en position i correspond à l'ancienne
     *           ligne du modèle en position i - 1
     */
    void insertRow(int index, Object[] row);
    
    /**
     * Retire la ligne en position index dans le modèle.
     * @pre
     *     0 <= index < getRowCount()
     * @post
     *     getRowCount() == old getRowCount() - 1
     *     les lignes du modèle entre 0 et index - 1 n'ont pas changé
     *     forall int i, index <= i < getRowCount() :
     *         la ligne du modèle en position i correspond à l'ancienne
     *           ligne du modèle en position i + 1
     */
    void removeRow(int index);

    /**
     * @post
     *     isCellEditable(row, column)
     *             && value != null
     *             && value.getClass() == getColumnClass(column)
     *         ==> getValueAt(row, column).equals(value)
     *     isCellEditable(row, column)
     *             && (value == null
     *                 || value.getClass() != getColumnClass(column))
     *         ==> getValueAt(row, column).equals(
     *                 ColumnFeature.values()[column].defaultValue())
     */
    @Override
    void setValueAt(Object value, int row, int column);
}
