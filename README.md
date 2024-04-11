Préliminaires
La classe JTable est un composant permettant de représenter des données sous forme tabulaire (comme une feuille de tableur par exemple).

Dans cette série, nous allons développer un bean qui permet d'afficher et de modifier les notes d'un étudiant, mais aussi de charger ces données depuis, ou de les sauvegarder dans, un fichier. Nous supposerons que les données associées aux notes sont sauvegardées dans des fichiers de texte dont le contenu est régi par les règles suivantes :

toute ligne vide ou commençant par le caractère '#' est ignorée ;
toute autre ligne contient un seul enregistrement, éventuellement suivi de texte à ignorer ;
un enregistrement contient, dans cet ordre, trois champs : un nom de matière, un coefficient et une note ;
les champs d'un enregistrement sont séparés par des tabulations ('\t' en java) ;
une ligne contenant un enregistrement est reconnue par l'expression « ^[^\t]*(\t\d+(\.\d+)?){2}(\t.*)*$ ».

Les deux boutons du haut permettent de charger les données ou de les sauvegarder. Le comportement de ces opérations de lecture et d'écriture sera artificiellement ralenti pour simuler des actions longues, qui ne devront donc pas être traitées sur EDT.

Dans le bas de la fenêtre se trouvent :

deux labels qui affichent respectivement, et en permanence, la moyenne et la somme des points de l'étudiant ;
une JProgressBar qui indique à l'utilisateur le déroulement des tâches de chargement ou de sauvegarde au fur et à mesure de leur évolution (ici, ce composant indique 100 % sur fond bleu car on vient de finir de sauvegarder les données dans un fichier).
Description des concepts et travail à faire
diagClasses.svg

Présentation des paquetages :
notes contient la classe racine de l'application : Main.
notes.model contient l'interface NoteTableModel qui spécifie des modèles de JTable adaptés à l'usage que nous en ferons cette semaine. Cette interface est réalisée dans la classe DefaultNoteTableModel. Ce paquetage contient aussi, dans l'interface NoteSheetModel et la classe DefaultNoteSheetModel, la spécification et la réalisation des modèles du bean principal utilisé par l'application. Ce dernier utilise un NoteFileManager pour charger les données du modèle de la table à partir d'un fichier, ou inversement pour stocker ces données dans un fichier. Enfin, le type énuméré ColumnFeature regroupe toutes les informations relatives aux caractéristiques de chaque colonne d'un NoteTableModel.
notes.util.events contient la définition des ResourceEvent (type générique) et des ResourceListener associés. Un écouteur de ce type possède quatre méthodes réflexes paramétrées chacune par une dérivation générique différente de ResourceEvent.
notes.view contient la définition d'un bean NoteSheet constitué d'une table, placée sur un panneau de défilement et dotée d'un menu surgissant activable par clic droit.
notes.gui contient la classe NotesAppli qui code l'application graphique constituée essentiellement d'un NoteSheet et de deux boutons, et finalement d'une barre de progression ajoutée par vos soins.
Adaptation des modèles de JTable
Les modèles de JTable nécessaires à notre application sont spécifiés dans l'interface NoteTableModel. Les modèles de ce type disposent de quatre colonnes, et d'un nombre variable de lignes, chacune représentant les données afférentes à une matière particulière. Chaque ligne sera stockée sous la forme d'un tableau d'objets, et la structure globale sera une liste de tableaux d'objets. Le premier élément d'une ligne est une String qui représente un nom de matière (subject), le second est un Double qui représente une note (mark) et le troisième encore un Double qui représente un coefficient (coeff). La quatrième information relative à une ligne donnée est un Double (point) qui se calcule par la formule mark × coeff, il n'est donc pas utile de la stocker physiquement dans les lignes du modèle. En revanche, la définition du modèle doit intégrer le calcul qui doit être effectué lors de la récupération des valeurs en quatrième colonne.

Pour que ce type de modèles soit observable, il faut qu'il notifie ses observateurs de tout changement d'état. En particulier, notez bien que :

la modification du nom d'une matière doit engendrer l'émission d'un TableModelEvent indiquant que la cellule en colonne subject a changé ;
la modification d'une note ou d'un coefficient doit générer deux TableModelEvent : le premier pour rendre compte de la modification de cette cellule, le second pour provoquer le rafraîchissement de la cellule en colonne point.
Travail à faire
Codez complètement la classe DefaultNoteTableModel qui hérite de la classe javax.swing.table.AbstractTableModel et qui implémente l'interface NoteTableModel.
Description du modèle du bean NoteSheet
Outre le remplissage classique “à la main” des données de la JTable par l'utilisateur, à l'aide de son clavier et de sa souris, l'installation de ces données peut aussi se faire par utilisation du bouton « Load... » de l'application graphique. Dans ce cas, ordre est donné au NoteSheetModel de charger le contenu de la table en scannant un fichier. Cette opération est réalisée par l'intermédiaire d'un NoteFileManager.

Le travail d'un NoteFileManager étant lent (en réalité, il est artificiellement ralenti) le scan d'un fichier devra respecter le schéma suivant dans le cas d'une lecture sans incident :

loadFileSeqDiag.svg

Lorsque le modèle du bean s'apprête à charger le contenu de la table à partir d'un fichier de notes, il transfère cette activité sur un modèle bas-niveau (de type NoteFileManager) avec exécution sur un thread privé. Le scan du fichier ne modifie pas une structure de données particulière : à chaque ligne du fichier parcourue par le modèle bas-niveau, une notification est émise. Ces notifications sont observées par un ResourceListener qui ajoute la ligne transmise au modèle de la table.

Plus généralement, l'observateur du modèle bas-niveau est notifié :

pour chaque ligne lue dans le fichier (notification lineLoaded) ;
pour chaque avancement dans la progression des lignes lues dans le fichier (notification progressUpdated) ;
pour toute erreur rencontrée au cours de la lecture d'une ligne dans le fichier (notification failureOccurred).
Chaque notification reçue par cet observateur est ensuite traduite soit en une action de modification (sur EDT) du modèle de la table, soit en une notification (sur EDT) de changement de valeur d'une propriété.

De la même manière, lorsque le modèle du bean s'apprête à sauvegarder le contenu de la table dans un fichier, il transfère aussi cette activité sur le NoteFileManager, avec exécution sur un thread privé. Mais l'observateur de ce gestionnaire reçoit maintenant les notifications suivantes :

une fois que toutes les lignes de la table ont été écrites dans le fichier (notification fileSaved) ;
pour chaque avancement dans la progression des lignes écrites dans le fichier (notification progressUpdated) ;
pour toute erreur rencontrée au cours de l'écriture d'une ligne dans le fichier (notification failureOccurred).
Il ne vous aura pas échappé qu'il y a une étape de préparation des données de la table, préalablement à la demande de sauvegarde de ces données dans un fichier.

Pour redire autrement ce qui vient d'être vu, le modèle possède trois propriétés liées, à notification forcée et inaccessibles en lecture comme en écriture :

saved : propriété de type String, véhiculant le nom du fichier utilisé lors d'une sauvegarde ;
progress : propriété de type Integer, véhiculant la progression de des opérations en cours de chargement ou de sauvegarde (entre 0 et 100) ;
failure : propriété de type Throwable, véhiculant les exceptions survenues lors de l'exécution d'une opération de chargement ou de sauvegarde.
Et il modifie directement le modèle de table pour chaque ligne lue, sans passer par des notifications de changement de valeur de propriété.

Travail à faire
Dans la classe NoteFileManager fournie, complétez les méthodes scanFile et saveListToFile, en n'oubliant pas d'intégrer les notifications aux ResourceListeners là où c'est nécessaire.
Dans la classe DefaultNoteSheetModel fournie, codez une classe interne LowLevelModelObserver qui implémente l'interface ResourceListener de la manière indiquée dans l'énoncé.
Dans la classe DefaultNoteSheetModel fournie, complétez la méthode loadTableFromFile().
Dans la classe DefaultNoteSheetModel fournie, complétez la méthode saveTableToFile().
Le bean NoteSheet
Un NoteSheet est une feuille de notes. Un tel objet est une sorte de JPanel possédant une instance de JTable et une instance de NoteSheetModel. La feuille de notes associe à la JTable le NoteTableModel qui est défini dans son propre modèle. Cette table est disposée sur une instance de JScrollPane qui assure les opérations de défilement.

Une instance de NoteSheet est aussi dotée de menus surgissants.

La classe des menus surgissant (JPopupMenu) est une classe de composants qui permettent de faire apparaître des menus à l'endroit où l'utilisateur humain clique droit avec la souris. Nous l'avons déjà rencontrée car elle est impliquée dans la définition d'un JMenu : un menu possède un menu surgissant, qui apparaît lorsqu'on clique sur le menu. On peuple un menu surgissant de la même manière que l'on peuple un JMenu : ajouter un élément de menu à un menu l'ajoute en fait à son menu surgissant. Du coup, si l'on sait construire des menus, on sait aussi construire des menus surgissants. Je ne m'étends pas sur les menus surgissants car le code de leur configuration est donné dans les ressources : lisez le code !

Le menu surgissant associé à la JTable permet à l'utilisateur de choisir entre plusieurs commandes qui parlent d'elles-mêmes :

insérer une ligne avant la ligne sélectionnée ;
insérer une ligne après la ligne sélectionnée ;
insérer une ligne à la fin de la table ;
supprimer la ligne sélectionnée ;
supprimer toutes les lignes de la table.
Une dernière remarque sur ces éléments de menu : certains d'entre eux ne doivent pas avoir d'effet dans certaines circonstances. En effet il est impossible, par exemple, d'ajouter une ligne “avant cette ligne” lorsqu'il n'y a pas de ligne dans la table, ou bien lorsqu'aucune d'entre elles n'est sélectionnée. Dans une architecture MVC, on observera le modèle de sélection de la table pour recalculer, à chaque modification de la sélection des lignes de la table, l'état d'activabilité des éléments de menu.

Enfin, l'évolution des propriétés du NoteSheetModel doit aussi être observée par le bean :

propriété progress : afin de modifier la propriété liée progress du NoteSheet ;
propriété saved : afin d'afficher, à l'aide d'un JOptionPane, le message de sauvegarde terminée ;
propriété failure : afin d'afficher, à l'aide d'un JOptionPane, le message d'erreur.
Travail à faire dans la classe NoteSheet fournie
Définissez une propriété liée dans NoteSheet, de nom progress et de type int, accessible en lecture uniquement.
Codez un observateur pour la propriété saved du NoteSheetModel, qui ouvre une boite de dialogue affichant un message adapté.
Codez un observateur pour la propriété failure du NoteSheetModel, qui ouvre une boite de dialogue affichant un message adapté.
Dans le type énuméré interne Item imbriqué dans la classe NoteSheet, définissez le comportement des différents éléments du menu surgissant en complétant les méthodes applyOn() de chaque constante.
Définissez ensuite le comportement de l'écouteur associé aux éléments de menu :
complétez la méthode buildMenuItemsMap() ;
complétez la méthode réflexe de l'écouteur al associé aux éléments de menu.
Pour finir, définissez un observateur du modèle de sélection de la table, qui met à jour l'état d'activabilité des élements de menu.
La classe de l'application graphique NotesAppli
L'application graphique est presque complète.

Au niveau des composants graphiques, il ne manque qu'une barre de progression graphique indiquant l'évolution de la propriété progress du NoteSheet.

Je vous laisse chercher dans la documentation comment créer et configurer une telle barre de progression. N'oubliez pas ensuite de l'insérer dans la méthode placeComponents(). N'oubliez pas non plus de la mettre à jour en observant les changements de valeur de la propriété progress du NoteSheet.

Au niveau du comportement enfin, il ne manque plus que la mise à jour des deux champs de texte mean et points à chaque modification du modèle de la table. Pour affiner un peu ce comportement, optimisez le calcul de ces deux valeurs en le désactivant lorsque la modification du modèle est un changement de nom de matière.

Travail à faire dans la classe NotesAppli fournie
Ajoutez une JProgressBar fonctionnant en accord avec la progression du chargement des lignes de la table.
Ajoutez un observateur capable de rafraîchir les champs de texte mean et points de l'application en fonction de la modification des valeurs stockées dans les cellules de la table.
Quelques remarques sur des détails techniques du code fourni
Aucun travail n'est demandé dans cette section car il s'agit de points techniques qui, bien qu'importants, ne doivent pas vous perturber dans votre apprentissage du composant JTable. Je vous propose donc ici quelques explications, pour les plus curieux...

Sur la création du menu surgissant
On attache un menu surgissant à un composant par appel à la méthode void setComponentPopupMenu(JPopupMenu) définie dans JComponent.

Vous trouverez dans le tutoriel Swing une autre façon de gérer les menus surgissants. Elle n'est pas tout à fait satisfaisante, en fait, elle date d'avant Java 5. Cette technique met en jeu un MouseListener particulier qu'il faut enregistrer auprès de chaque composant souhaitant bénéficier d'un JPopupMenu. Outre la lourdeur de cette manière de faire, elle ne permet pas d'activer le menu surgissant à partir du clavier par exemple, lorsqu'un raccourcis est défini pour cette fonction. C'est pourquoi je vous ai parlé ci-dessus de l'utilisation de setComponentPopupMenu : c'est ainsi le ui-delegate qui gère l'activation du menu surgissant, et ceci selon un protocole dépendant de la plateforme, mais de manière totalement transparente pour le programmeur.

Il subsiste quand même un problème : un clic droit sur une ligne de la table ne sélectionne pas la ligne sous le pointeur de la souris. Et tenter de le résoudre avec un écouteur de clic souris ne fonctionnera pas car le menu surgissant consomme le clic souris à l'origine de son activation. Le contournement de ce problème consiste à déterminer la position de la ligne désignée par le pointeur de la souris, puis à la sélectionner. La seule façon de faire est de redéfinir la méthode show de JPopupMenu, ce qui est fait à la création du menu surgissant (voir le code fourni).

Sur l'observation des ajouts de lignes dans la table
Lorsque vous ajoutez une ligne à la table, il est ensuite conseillé de faire appel à la méthode scrollRectToVisible() (définie dans JComponent). Cela permet d'assurer que la ligne qui vient d'être ajoutée au modèle est bien affichée dans le viewport du JScrollPane. Ceci est important notamment dans le cas où l'on vient d'insérer une ligne en fin de table alors que la fin de la table n'était précédemment pas visible : ne pas rendre visible cette dernière ligne pourrait donner l'impression désagréable qu'elle n'a pas été ajoutée (vous n'avez qu'à essayer, vous verrez ce que je veux dire)...

Cette méthode, appliquée à la table, assure donc que la zone de la table délimitée par le rectangle donné en paramètre est effectivement visible dans le viewport du JScrollPane qui supporte la table. Du coup, vous enregistrez un nouveau TableModelListener sur le modèle de la table pour appeller la méthode scrollRectToVisible afin d'afficher la dernière ligne dans le JScrollPane... et ça ne fonctionne pas : ce n'est pas la dernière ligne de la table qui apparaît, mais l'avant dernière !

Rappelez-vous que rien n'est garanti quant à l'ordre dans lequel les listeners sont notifiés. Il se peut (c'est même justement le cas) que le listener interne de la table, celui qui s'occupe du rafraîchissement de la table en fonction de l'état de son modèle de données, soit notifié après le vôtre (celui qui demande d'afficher la nouvelle ligne). La solution consiste à demander la visualisation de la dernière ligne dans le viewport seulement après que cette dernière ligne aura été prise en compte par la table. Vous coderez donc l'appel à scrollRectToVisible comme une tâche qui devra être traitée plus tard sur EDT.
