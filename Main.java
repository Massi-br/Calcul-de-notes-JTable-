package notes;


import javax.swing.SwingUtilities;

import notes.gui.NotesAppli;

public final class Main {

    private Main() {
        // rien ici
    }

    // POINTS D'ENTREE

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NotesAppli().display();
            }
        });
    }
}
