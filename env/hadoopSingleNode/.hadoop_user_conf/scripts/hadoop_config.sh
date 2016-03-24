#
#
# HADOOP
#
#

# Constantes importantes, déterminant la localisation des fichiers de conf, et de stockage de hadoop/hdfs 
export HADOOP_USER_CONF=${HOME}"/.hadoop_user_conf"
export HADOOP_USER_STORAGE="/opt/hadoop-data"

# Crée la structure de dossiers nécessaire à l'utilisation d'hadoop avec OAR
function hadoop_setup_folders(){
    


    dossiers=(${HADOOP_USER_STORAGE} \
	${HADOOP_USER_STORAGE}"/hdfs" \
	${HADOOP_USER_STORAGE}"/hdfs/datanode" \
	${HADOOP_USER_CONF} \
	${HADOOP_USER_CONF}"/hadoop_logs" \
	${HADOOP_USER_CONF}"/yarn_logs" \
	${HADOOP_USER_CONF}"/hadoop_conf_original" \
	${HADOOP_USER_CONF}"/hadoop_conf") 
    
    for f in ${dossiers[@]}; do
	if [ ! -d $f ]; then
	    mkdir $f 2> /dev/null;
	fi
    done;
    return 0;
}

function hadoop_export_constants(){
    export JAVA_HOME=/usr/lib/jvm/java

    export HADOOP_HEAPSIZE="500"
    export HADOOP_NAMENODE_INIT_HEAPSIZE="500"

# Hadoop 2.2.0
    HADOOP_PREFIX=/opt/hadoop
    export HADOOP_HOME=$HADOOP_PREFIX
    export HADOOP_COMMON_HOME=$HADOOP_PREFIX
    export HADOOP_HDFS_HOME=$HADOOP_PREFIX
    export HADOOP_MAPRED_HOME=$HADOOP_PREFIX
    export HADOOP_YARN_HOME=$HADOOP_PREFIX
    export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
    
    export HADOOP_OPTS=-Djava.net.preferIPv4Stack=true
    export HADOOP_COMMON_LIB_NATIVE_DIR=${HADOOP_PREFIX}"/lib/native/"
    export HADOOP_OPTS="${HADOOP_OPTS} -Djava.library.path=$HADOOP_PREFIX/lib/"
    
    export HADOOP_CONF_DIR=${HADOOP_USER_CONF}/hadoop_conf
    export HADOOP_LOG_DIR=${HADOOP_USER_CONF}/hadoop_logs
    export YARN_LOG_DIR=${HADOOP_USER_CONF}/yarn_logs
    export YARN_CONF_DIR=${HADOOP_USER_CONF}/hadoop_conf
}



## Sécurité du logout

function confirm()
{
    echo -n "$@ "
    read -e answer
    for response in y Y yes YES Yes Sure sure SURE OK ok Ok
    do
        if [ "_$answer" == "_$response" ]
        then
            return 0
        fi
    done
 
    # Any answer other than the list above is considerred a "no" answer
    return 1
}
function testExit()
{
    if [[ "$(jps|wc -l)" -gt "1" ]]; then
	confirm Avez-vous bien arrêté tous les processus Java ? \(commande jps\) yes/no && logout;
    else
	logout;
    fi
}

alias exit="testExit";
