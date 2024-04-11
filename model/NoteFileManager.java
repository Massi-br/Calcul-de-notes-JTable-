package notes.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import notes.util.event.ResourceListener;
import notes.util.event.ResourceSupport;
import util.Contract;

class NoteFileManager {

    // CONSTANTES STATIQUES

    private static final String NL = System.getProperty("line.separator");

    // ATTRIBUTS
    
    private final ResourceSupport support;

    // CONSTRUCTEURS

    NoteFileManager() {
        support = new ResourceSupport(this);
    }

    // REQUETES
    
    ResourceListener[] getResourceListeners() {
        return support.getListeners();
    }

    // COMMANDES

    /**
     * @pre
     *     lnr != null
     * @post
     *     lnr a été ajouté à la séquence des écouteurs
     */
    void addResourceListener(ResourceListener lnr) {
        Contract.checkCondition(lnr != null);
        
        support.add(lnr);
    }

    void scanFile(final File f) {
    	BufferedReader rd = null;
		int x = 0;
		int fSize = (int) f.length();
		try {
			rd = new BufferedReader(new FileReader(f));
			String s = rd.readLine();
			while(s!=null) {
				delayAction();
				support.fireLineLoaded(s);
				x += s.getBytes().length;
				support.fireProgressUpdated((int) ((x * 100)/fSize ));
				s = rd.readLine();
			}
			support.fireProgressUpdated(100);
		} catch (Exception e) {
			support.fireFailureOccured(e);
		} finally {
			try {
				rd.close();
			} catch (IOException e) {
				support.fireFailureOccured(e);
			}
		}
    }

    /**
     * @pre
     *     lnr != null
     * @post
     *     lnr a été retiré de la séquence des écouteurs
     */
    void removeResourceListener(ResourceListener lnr) {
        Contract.checkCondition(lnr != null);
        
        support.remove(lnr);
    }

    void saveListToFile(List<String> lines, File f) {
        Writer wr = null;
        int sum = 0;
        try {
			wr = new BufferedWriter(new FileWriter(f));
			for (String line: lines) {
				wr.write(line + NL);
				sum += 1;
				support.fireProgressUpdated((sum * 100) / lines.size());
				delayAction();
			}
		} catch (Exception e) {
			support.fireFailureOccured(e);
		} finally {
			try {
				wr.close();
			} catch (IOException e) {
				support.fireFailureOccured(e);
			}
		}
        support.fireDataSaved(f.getAbsolutePath());
    }

    // OUTILS

    /**
     * Pour ralentir les actions de lecture ou d'écriture.
     */
    private void delayAction() {
        final int delay = 100;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // rien, on quitte
        }
    }
}
