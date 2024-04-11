package notes.util.event;

import java.util.EventObject;

public class ResourceEvent<R> extends EventObject {

    // ATTRIBUTS

    private final R resourceData;

    // CONSTRUCTEURS

    public ResourceEvent(Object source, R d) {
        super(source);
        resourceData = d;
    }

    // REQUETES

    /**
     * La ressource véhiculée par cet événement.
     */
    public R getResource() {
        return resourceData;
    }
}
