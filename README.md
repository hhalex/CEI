# CEI
Projet de CEI en 3ème année à Supélec.

##
Réalisé par *Mickael Benais*, *Clément Buisson* et *Alexandre Careil*.
Encadré par *Nacéra Bennacer*, *Francesca Bugiotti* et *Gianluca Quercini*.

## Buts fixés 
Le but de ce projet est assez large. Il s'agit dans un premier temps d'analyser un algorithme et de voir comment l'améliorer. L'algorithme en question se charge de déterminer la traduction d'un article donné dans une autre langue. La traduction doit déjà exister dans le graphe de Wikipedia.
Dans un second temps il s'agira d'explorer les technologies disponibles pour stocker les données autrement qu'avec les bases de données relationnelles (état de l'art du NoSQL).
Finalement, il s'agira d'implémenter l'algorithme proposé et de comparer sa préision avec celui proposé au départ.

## Contenu de ce repository
Vous trouverez sur dépôt:
- le compte-rendu en anglais, au format Org (mode d'Emacs)
- les illustrations au format .eps (encapsulated PostScript)
- le code
- les scripts d'installation et d'initalisation de l'environnement d'exécution

## Outils utilisés
- OAR (gestionnaire de ressources) pour acquérir un ensemble de noeuds.
- Hadoop 2.2.0 avec HBase 0.98


## Compilation du rapport
La syntaxe utilisée pour la rédaction du rapport est [Org](http://orgmode.org/features.html). C'est une syntaxe assez simple et moins bruyante que latex/tex, ressemblant beaucoup au [`markdown`](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet).

Latex + Pygments + Minted (pour colorer les extraits de code).

On compile d'abord Org -> Latex avec Emacs (`C-c C-e l l`), et puis on exécute le makefile qui se charge de tout compiler comme il se doit vers le format .pdf (et supprime tous les fichiers temporaires utiles pendant la compilation).

Finalement on va utiliser latex.