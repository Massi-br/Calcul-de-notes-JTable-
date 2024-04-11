package notes.util.event;

import java.util.EventListener;

public interface ResourceListener extends EventListener {

    /**
     * Invoquée lorsque une ligne de fichier vient d'être lue, l'événement
     *  véhiculant cette ligne au format String.
     */
    void lineLoaded(ResourceEvent<String> e);

    /**
     * Invoquée lorsque la sauvegarde des données est terminée, l'événement
     *  véhiculant le nom du fichier utilisé.
     */
    void dataSaved(ResourceEvent<String> e);

    /**
     * Invoquée lorsque une ligne de fichier vient d'être lue ou écrite,
     *  l'événement véhiculant le pourcentage de chargement ou de sauvegarde
     *  effectué.
     */
    void progressUpdated(ResourceEvent<Integer> e);

    /**
     * Invoquée lorsqu'un échec survient pendant la lecture ou l'écriture
     *  d'une ligne, l'événement véhiculant l'exception survenue.
     */
    void failureOccurred(ResourceEvent<Throwable> e);
}
