package notes.model;

import static notes.model.ColumnFeature.COEF;
import static notes.model.ColumnFeature.MARK;
import static notes.model.ColumnFeature.SUBJECT;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.event.SwingPropertyChangeSupport;

import notes.util.event.ResourceEvent;
import notes.util.event.ResourceListener;
import util.Contract;

public class DefaultNoteSheetModel implements NoteSheetModel {

    // ATTRIBUTS

    private static final ColumnFeature[] COL_FEATURES = ColumnFeature.values();

    private final NoteFileManager lowLevelModel;
    private final NoteTableModel tableModel;
    private final PropertyChangeSupport support;

    // CONSTRUCTEURS

    public DefaultNoteSheetModel() {
        support = new SwingPropertyChangeSupport(this, true);
        tableModel = new DefaultNoteTableModel();
        lowLevelModel = new NoteFileManager();
        lowLevelModel.addResourceListener(new LowLevelModelObserver());
    }

    // REQUETES

    @Override
    public double getMean() {
        // RAPPEL : 0/0 -> NaN sur les flottants
        double coeffs = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            coeffs += (Double) tableModel.getValueAt(i, COEF.ordinal());
        }
        return getPoints() / coeffs;
    }
    
    @Override
    public NoteTableModel getNoteTableModel() {
        return tableModel;
    }

    @Override
    public double getPoints() {
        double points = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            points += (Double) tableModel.getValueAt(i, COEF.ordinal())
                    * (Double) tableModel.getValueAt(i, MARK.ordinal());
        }
        return points;
    }
    
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String prop) {
        Contract.checkCondition(prop != null);
        
        return support.getPropertyChangeListeners();
    }

    // COMMANDES

    @Override
    public void addPropertyChangeListener(String prop,
            PropertyChangeListener lnr) {
        Contract.checkCondition(prop != null && lnr != null);
        
        support.addPropertyChangeListener(prop, lnr);
    }

    @Override
    public void loadTableFromFile(final File f) {
        // 1. vide le modèle de la table
        // 2. Lance le scan du fichier f par lowLevelModel,
        //    sur un autre thread que EDT (car prend du temps).
        tableModel.clearRows();
        Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				lowLevelModel.scanFile(f);
			}
        	
        });
        t.start();
    }

    @Override
    public void removePropertyChangeListener(String prop,
            PropertyChangeListener lnr) {
        Contract.checkCondition(prop != null && lnr != null);
        
        support.removePropertyChangeListener(prop, lnr);
    }

    @Override
    public void saveTableToFile(final File f) {
        // 1. Construit la séquence des lignes de texte à sauvegarder
        //    dans le fichier f.
        // 2. Lance la sauvegarde de cette séquence dans le fichier f
        //    par lowLevelModel, sur un autre thread que EDT (prend du temps).
        final List<String> l = new ArrayList<String>();
        l.add(createHeader());
        for(int i = 0; i< tableModel.getRowCount(); i+=1) {
        	l.add(toLine(i));
        }
        Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				lowLevelModel.saveListToFile(l, f);
			}
        	
        });
        t.start();
    }

    // OUTILS

    /**
     * Construit le commentaire initial du fichier de sauvegarde.
     */
    private static String createHeader() {
        return COMMENT_CHAR + " " + SUBJECT.headerName() + FIELD_SEP
                + COEF.headerName() + FIELD_SEP + MARK.headerName();
    }

    /**
     * Construit une chaîne représentant la ième ligne de m.
     * @pre 0 <= i < tableModel.getRowCount()
     */
    private String toLine(int i) {
        assert 0 <= i && i < tableModel.getRowCount();
        
        return tableModel.getValueAt(i, SUBJECT.ordinal()) + FIELD_SEP
                + tableModel.getValueAt(i, COEF.ordinal()) + FIELD_SEP
                + tableModel.getValueAt(i, MARK.ordinal());
    }

    /**
     * Découpe une ligne de texte en un triplet de valeurs de types respectifs
     *  SUBJECT.type(), COEF.type() et MARK.type().
     * Si line vaut "", on obtient le triplet de valeurs :
     *  (SUBJECT.defaultValue(), COEF.defaultValue(), MARK.defaultValue()).
     * @pre
     *     line != null
     *     |line.split(FIELD_SEP)| < |COL_FEATURES|
     */
    private static Object[] toRow(String line) {
        assert line != null;
        assert line.split(FIELD_SEP).length < COL_FEATURES.length;
        
        Object[] row = new Object[COL_FEATURES.length - 1];
        String[] tokens = line.split(FIELD_SEP);
        for (int i = 0; i < tokens.length; i++) {
            row[i] = COL_FEATURES[i].parseValue(tokens[i]);
        }
        for (int i = tokens.length; i < row.length; i++) {
            row[i] = COL_FEATURES[i].defaultValue();
        }
        return row;
    }
    
    /**
     * Observation de lowLevelModel pour ajouter les lignes lues dans
     *  tableModel et notifier des changements de valeur des propriétés
     *  forcées save, progress et failure.
     */
    private class LowLevelModelObserver implements ResourceListener  {
        @Override
        public void lineLoaded(ResourceEvent<String> e) {
            /*
             * Soit lowLevelModel a lu une ligne de texte vide
             *    ou un commentaire :
             * ---> ne rien faire
             * Soit lowLevelModel a lu une ligne de texte
             *    correspondant au pattern LINE_PAT :
             * ---> ajouter cette ligne en fin du modèle de données de la table
             * Soit lowLevelModel a lu une ligne de texte incorrecte
             * ---> notifier un changement de valeur pour la propriété failure
             *      en transmettant une IllegalArgumentException
             */
            String s = e.getResource();
            if (s.equals("") ||s.charAt(0) == '#') {
            	return;
            }
            Matcher m = LINE_PAT.matcher(s);
            if (m.matches()) {
            	tableModel.addRow(toRow(s));
            } else {
            	support.firePropertyChange(PROP_FAILURE, null, new IllegalArgumentException());
            }
        }
        
        @Override
        public void dataSaved(ResourceEvent<String> e) {
            /*
             * lowLevelModel a terminé une sauvegarde dans le fichier :
             * ---> notifier d'un changement de valeur pour la propriété saved
             *      en transmettant le nom du fichier de sauvegarde
             */
            support.firePropertyChange(PROP_SAVED, null, e.getResource());
        }
        
        @Override
        public void progressUpdated(ResourceEvent<Integer> e) {
            /*
             * lowLevelModel indique la proportion de lignes lues
             *  ou sauvegardées dans le fichier :
             * ---> notifier d'un changement de valeur pour la propriété
             *      progress en transmettant la proportion de lignes lues
             *      ou sauvegardées
             */
            support.firePropertyChange(PROP_PROGRESS, null, e.getResource());
        }
        
        @Override
        public void failureOccurred(ResourceEvent<Throwable> e) {
            /*
             * lowLevelModel a reporté un échec lors de la lecture/écriture :
             * ---> notifier d'un changement de valeur pour la propriété failure
             *      en transmettant l'erreur rencontrée
             */
            support.firePropertyChange(PROP_FAILURE, null, e.getResource());
        }
    }
}
