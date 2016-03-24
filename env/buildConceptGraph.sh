#!/usr/bin/env bash                                                                                                                                                                
# Permet d'initialiser Hadoop et HBase                                                                                                                                             
source ~/.bashrc

# Permet d'utiliser des alias en non shell interactif                                                                                                                              
shopt -s expand_aliases

# Raccourcis                             
#Nouvelle version de conceptopedia !!!                                                                                                                                         
CONCEPTOPEDIA=~/tests_java/Conceptopedia.jar
WIKIPEDIA_FOLDER=~/wikipedia
alias Conceptopedia0="java -jar $CONCEPTOPEDIA --mode 0"
alias Conceptopedia1="java -jar $CONCEPTOPEDIA --mode 1"
alias Conceptopedia2="java -jar $CONCEPTOPEDIA --mode 2"
alias Conceptopedia3="java -jar $CONCEPTOPEDIA --mode 3"
alias Conceptopedia4bis="java -jar $CONCEPTOPEDIA --mode 4bis --start-row \"C-af-woeste hoeve\x00\""


Conceptopedia4bis >> temoinConceptGraphLundiNuit



