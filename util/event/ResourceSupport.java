package notes.util.event;

import javax.swing.event.EventListenerList;

public class ResourceSupport {

    // CONSTANTES STATIQUES

    private static final ResourceListener[] EMPTY_ARRAY =
            new ResourceListener[0];

    // ATTRIBUTS
    
    private EventListenerList listenersList;
    private final Object owner;

    // CONSTRUCTEURS

    public ResourceSupport(Object owner) {
        this.owner = owner;
    }

    // REQUETES

    public ResourceListener[] getListeners() {
        if (listenersList == null) {
            return EMPTY_ARRAY;
        }
        return listenersList.getListeners(ResourceListener.class);
    }

    // COMMANDES

    public void add(ResourceListener listener) {
        if (listener == null) {
            return;
        }
        if (listenersList == null) {
            listenersList = new EventListenerList();
        }
        listenersList.add(ResourceListener.class, listener);
    }

    public void remove(ResourceListener listener) {
        if (listener == null || listenersList == null) {
            return;
        }
        listenersList.remove(ResourceListener.class, listener);
    }

    public void fireLineLoaded(String d) {
        if (listenersList == null) {
            return;
        }
        ResourceListener[] list = getListeners();
        ResourceEvent<String> e = new ResourceEvent<String>(owner, d);
        for (ResourceListener lst : list) {
            lst.lineLoaded(e);
        }
    }

    public void fireDataSaved(String d) {
        if (listenersList == null) {
            return;
        }
        ResourceListener[] list = getListeners();
        ResourceEvent<String> e = new ResourceEvent<String>(owner, d);
        for (ResourceListener lst : list) {
            lst.dataSaved(e);
        }
    }

    public void fireProgressUpdated(Integer n) {
        if (listenersList == null) {
            return;
        }
        ResourceListener[] list = getListeners();
        ResourceEvent<Integer> e = new ResourceEvent<Integer>(owner, n);
        for (ResourceListener lst : list) {
            lst.progressUpdated(e);
        }
    }

    public void fireFailureOccured(Exception exc) {
        if (listenersList == null) {
            return;
        }
        ResourceListener[] list = getListeners();
        ResourceEvent<Throwable> e =
                new ResourceEvent<Throwable>(owner, exc);
        for (ResourceListener lst : list) {
            lst.failureOccurred(e);
        }
    }
}
