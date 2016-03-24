#!/usr/bin/env bash
# Permet d'initialiser Hadoop et HBase
source ~/.bashrc

# Permet d'utiliser des alias en non shell interactif
shopt -s expand_aliases

# Raccourcis
CONCEPTOPEDIA=~/tests_java/Conceptopedia.jar
WIKIPEDIA_FOLDER=~/wikipedia
alias Conceptopedia0="java -jar $CONCEPTOPEDIA --mode 0"
alias Conceptopedia1="java -jar $CONCEPTOPEDIA --mode 1"
alias Conceptopedia2="java -jar $CONCEPTOPEDIA --mode 2"
alias Conceptopedia3="java -jar $CONCEPTOPEDIA --mode 3"
alias Conceptopedia4="hadoop jar $CONCEPTOPEDIA --mode 4"

# Variable Lang
WIKIPEDIA_LANGS=(af az ar fr en it es de) 

#Réinitialisation du fichier qui contient des informations sur l'avancée du script
echo "" > ~/temoin_buildhbase

# On supprime les snapshots précédents s'ils existent
echo  "delete_snapshot 'intralang-xml'" | hbase shell 
echo  "delete_snapshot 'intralang_matching_ids-xml'" | hbase shell
echo  "delete_snapshot 'intralang-sql'" | hbase shell 
echo  "delete_snapshot 'intralang-creator'" | hbase shell
echo  "delete_snapshot 'conceptcreator-creator'" | hbase shell
# Construction du Data Model (tables et familles de colonnes dans HBase)
echo "Construction du data model" >> temoin_buildhbase
echo $(date) >> temoin_buildhbase
Conceptopedia0 >> temoin_buildhbase

# Chargement des données XML (Pages avec liens vers les voisins dans la même langue)
for l in "${WIKIPEDIA_LANGS[@]}"
do
    echo $l " xml start : "  $(date) >> temoin_buildhbase
    Conceptopedia1 --file ${WIKIPEDIA_FOLDER}/${l}wiki-latest-pages-articles.xml
done

echo "fin xml : " $(date) >> temoin_buildhbase     

# Sauvegarde des tables à cette étape
echo  "Snapshot de la base: seulement les fichiers xml" >> temoin_buildhbase
echo  "snapshot 'intralang', 'intralang-xml'" | hbase shell
echo  "snapshot 'intralang_matching_ids', 'intralang_matching_ids-xml'" | hbase shell

# Ajout des crosslinks
for l in "${WIKIPEDIA_LANGS[@]}"
do
    echo $l " sql start : "  $(date) >> temoin_buildhbase
    Conceptopedia2 --file ${WIKIPEDIA_FOLDER}/${l}wiki-latest-langlinks.sql
done

echo "fin sql : " $(date) >> temoin_buildhbase     

# Sauvegarde des tables
echo "Snapshot de la base: seulement les fichiers sql" >> temoin_buildhbase
echo "snapshot 'intralang', 'intralang-sql'" | hbase shell
echo "snapshot 'conceptcreator', 'conceptcreator-vide'" | hbase shell
echo "list_snapshots" | hbase shell >> temoin_buildhbase

# Création des concepts
echo "Création des concepts (conceptcreator)" >> temoin_buildhbase
Conceptopedia3 >> temoin_buildhbase

# Sauvegarde
echo "Snapshot de la base: après création des concepts" >> temoin_buildhbase

echo "snapshot 'intralang', 'intralang-creator'" | hbase shell
echo "snapshot 'conceptcreator', 'conceptcreator-creator'" | hbase shell

echo "list_snapshots" | hbase shell >> temoin_buildhbase

# Création des concepts
echo "Création du graphe des concepts (conceptgraph)" >> temoin_buildhbase
Conceptopedia4 >> temoin_buildhbase
