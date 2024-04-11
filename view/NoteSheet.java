package notes.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import notes.model.DefaultNoteSheetModel;
import notes.model.DefaultNoteTableModel;
import notes.model.NoteSheetModel;
import notes.model.NoteTableModel;

/**
 * Un NoteSheet est une feuille de notes.
 * Une instance de NoteSheet est un JPanel constitué d'une JTable, dotée
 *  d'un NoteTableModel et reposant sur un JScrollPane.
 * Un NoteSheet est aussi doté de JPopupMenus.
 * On peut observer la progression d'un chargement ou d'une sauvegarde en
 *  branchant des PropertyChangeListeners.
 */
public class NoteSheet extends JPanel {
    
    // ATTRIBUTS

    private static final long serialVersionUID = 1L;
	private JTable table;
    // menu surgissant complet attaché à la table quand elle est non vide
    private JPopupMenu fullMenu;
    // tous les éléments du menu surgissant fullMenu (qui correspondent aux
    // constantes de Item)
    private Map<Item, JMenuItem> menuItems;
    
    private NoteSheetModel model;
	private int progress;

    // CONSTRUCTEURS

    public NoteSheet() {
        super(new BorderLayout());
        createModel();
        createView();
        placeComponents();
        createController();
        updateMenuItems();
    }

    // REQUETES

    public NoteTableModel getTableModel() {
        return model.getNoteTableModel();
    }

    public double getMean() {
        return model.getMean();
    }

    public double getPoints() {
        return model.getPoints();
    }
    
    public int getProgress() {
    	return progress;
    }

    // COMMANDES

    /**
     * Charge un fichier de notes.
     * Au cours du chargement, des modifications de valeur pour les propriétés
     *   forcées row, progress et failure auront lieu.
     * @pre
     *     f != null
     * @post
     *     le bean contient les données chargées
     */
    public void loadTableFromFile(File f) {
        model.loadTableFromFile(f);
    }

    /**
     * Enregistre un fichier de notes.
     * Au cours de l'enregistrement, des modifications de valeur pour les
     *  propriétés forcées saved, progress et failure auront lieu.
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
     *     suivi de toutes les lignes de données du bean
     */
    public void saveTableToFile(File f) {
        model.saveTableToFile(f);
    }

    // OUTILS

    private void createModel() {
        model = new DefaultNoteSheetModel();
        progress = 0;
    }

    private void createView() {
        menuItems = getMenuItemsMap();
        fullMenu = createAndGetConfiguredPopupMenu();
        
        table = new JTable(model.getNoteTableModel());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(fullMenu);
        table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));
    }
    
    private Map<Item, JMenuItem> getMenuItemsMap() {
        Map<Item,JMenuItem> map = new EnumMap<Item,JMenuItem>(Item.class);
        Item[] items = Item.values();
        for(Item item: items) {
        	map.put(item, new JMenuItem(item.label));
        }
        return map;
    }

    private JPopupMenu createAndGetConfiguredPopupMenu() {
        JPopupMenu pm = new JPopupMenu() {
            private static final long serialVersionUID = 1L;

			// ce contournement au problème de la non sélection des lignes par
            // clic droit sur une JTable est expliqué ici :
            // https://stackoverflow.com/questions/6007891
            //                /java-swing-popup-menu-and-jlist/20688591#20688591
            @Override
            public void show(Component invoker, int x, int y) {
                int r = table.rowAtPoint(new Point(x, y));
                if (r >= 0) {
                    table.setRowSelectionInterval(r, r);
                }
                super.show(invoker, x, y);
            }
        };
        for (Item i : Item.STRUCT) {
            if (i == null) {
                pm.addSeparator();
            } else {
                pm.add(menuItems.get(i));
            }
        }
        return pm;
    }

    private void placeComponents() {
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void createController() {
        // observation du modèle de données de la table,
        // pour assurer l'affichage de la première des lignes concernées
        // par cette observation
        NoteTableModel tm = model.getNoteTableModel();
        tm.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                /*
                 * l'utilisation de invokeLater n'est pas là pour basculer
                 * sur EDT (on y est déjà), mais pour assurer que
                 * scrollRectToVisible sera effectuée <em>après</em> le
                 * rafraîchissement de la JTable suite à la modification
                 * du modèle.
                 */
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        /*
                         * assure que la première des lignes qui viennent
                         * d'être modifiées ou insérées dans la JTable est bien
                         * affichée à l'écran
                         */
                        int row = e.getFirstRow();
                        Rectangle rect = table.getCellRect(row, 0, true);
                        table.scrollRectToVisible(rect);
                    }
                });
            }
        });
        
        // observation du modèle de sélection de la table,
        // pour mettre à jour l'activabilité des éléments de menu en fonction
        // des lignes sélectionnées dans la table
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateMenuItems();
			}
        });

        // observation de la propriété progress du modèle du bean,
        // pour modifier la propriété de même nom du bean
        model.addPropertyChangeListener(NoteSheetModel.PROP_PROGRESS, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				int oldValue = progress;
				progress = (Integer) evt.getNewValue();
				NoteSheet.this.firePropertyChange(NoteSheetModel.PROP_PROGRESS, oldValue, progress);
			}
        	
        });
        
        // observation de la propriété saved du modèle du bean,
        // pour afficher un message d'information
        model.addPropertyChangeListener(NoteSheetModel.PROP_SAVED, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					
					JOptionPane.showMessageDialog(null,
						"Fichier sauvegardé dans " + evt.getNewValue(),
						"Terminé",
						JOptionPane.INFORMATION_MESSAGE);
				
				}
        	
        	});
        
        // observation de la propriété failure du modèle du bean,
        // pour afficher un message d'erreur
        model.addPropertyChangeListener(NoteSheetModel.PROP_FAILURE, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				JOptionPane.showMessageDialog(null,
						"Il y a eu une erreur",
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				
			}
        	
        });

        // écoute des éléments de menu,
        // pour appliquer leur comportement sur la table
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem m = (JMenuItem) e.getSource();
            	for (Item i : Item.values()) {
            		if (i.label.equals(m.getText())) {
            			i.applyOn(table);
            			break;
            		}
            	}
            }
        };
        for (JMenuItem jmi : menuItems.values()) {
            jmi.addActionListener(al);
        }
    }
    
    /**
     * Activation ou désactivation de chaque JMenuItem selon l'état de la table.
     */
    private void updateMenuItems() {
        for (Map.Entry<Item, JMenuItem> entry : menuItems.entrySet()) {
            Item i = entry.getKey();
            JMenuItem jmi = entry.getValue();
            jmi.setEnabled(i.getEnabledValue(table));
        }
    }

    // TYPES IMBRIQUES

    private enum Item {
        INS_BEFORE("Insérer avant cette ligne") {
            // Insère une nouvelle ligne dans t juste avant celle qui est
            //  sélectionnée.
            // Ne fait rien si aucune ligne n'est sélectionnée.
            @Override void applyOn(JTable t) {
            	int i = t.getSelectedRow();
                if(i == -1) {
                	return;
                } else {
                	DefaultNoteTableModel tm = (DefaultNoteTableModel) t.getModel();
                	tm.insertEmptyRow(i);
                }
            }
            @Override boolean getEnabledValue(JTable t) {
                return t.getSelectedRow() >= 0;
            }
        },
        INS_AFTER("Insérer après cette ligne") {
            // Insère une nouvelle ligne dans t juste après celle qui est
            //  sélectionnée.
            // Ne fait rien si aucune ligne n'est sélectionnée.
            @Override void applyOn(JTable t) {
            	int i = t.getSelectedRow();
                if(i == -1) {
                	return;
                } else {
                	DefaultNoteTableModel tm = (DefaultNoteTableModel) t.getModel();
                	tm.insertEmptyRow(i+1);
                }
            }
            @Override boolean getEnabledValue(JTable t) {
                return t.getSelectedRow() >= 0;
            }
        },
        INS_END("Insérer à la fin") {
            // Insère une nouvelle ligne vide dans t juste après la dernière.
            @Override void applyOn(JTable t) {
                DefaultNoteTableModel tm = (DefaultNoteTableModel) t.getModel();
                tm.addEmptyRow();
            }
        },
        DEL_THIS("Supprimer cette ligne") {
            // Supprime, dans t, la ligne sélectionnée.
            // Ne fait rien si aucune ligne n'est sélectionnée.
            @Override void applyOn(JTable t) {
            	int i = t.getSelectedRow();
                if(i == -1) {
                	return;
                } else {
                	DefaultNoteTableModel tm = (DefaultNoteTableModel) t.getModel();
                	tm.removeRow(i);
                }
            }
            @Override boolean getEnabledValue(JTable t) {
                return t.getSelectedRow() >= 0;
            }
        },
        DEL_ALL("Supprimer toutes les lignes") {
            // Supprime toutes les lignes de t.
            @Override void applyOn(JTable t) {
            	DefaultNoteTableModel tm = (DefaultNoteTableModel) t.getModel();
            	tm.clearRows();
            }
        };
        
        static final Item[] STRUCT = new Item[] {
                INS_BEFORE, INS_AFTER, INS_END, null, DEL_THIS, DEL_ALL
        };
        
        private final String label;
        Item(String s) {
            label = s;
        }

        /**
         * Indique si le JMenuItem correspondant à this doit être activable
         *  ou non, en fonction de l'état de t.
         * Retourne true par défaut.
         */
        boolean getEnabledValue(JTable t) {
            return true;
        }

        /**
         * Applique, sur la table t, le comportement du JMenuItem correspondant
         *  à this.
         */
        abstract void applyOn(JTable t);
    }
}
