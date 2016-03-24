#
#
# HBASE
#
#

# Crée la structure de dossiers nécessaire à l'utilisation d'hbase avec OAR
function hbase_setup_folders(){
    
    dossiers=(${HADOOP_USER_STORAGE} \
	${HADOOP_USER_STORAGE}"/hdfs" \
	${HADOOP_USER_STORAGE}"/hbase" \
	${HADOOP_USER_STORAGE}"/zookeeper" \
	${HADOOP_USER_CONF} \
	${HADOOP_USER_CONF}"/hbase_conf" \
	${HADOOP_USER_CONF}"/hbase_logs") 
    
    for f in ${dossiers[@]}; do
	if [ ! -d $f ]; then
	    mkdir $f 2> /dev/null;
	fi
    done;
    return 0;
}

function hbase_export_constants(){
    export JAVA_HOME=/usr/lib/jvm/java

    export HBASE_HEAPSIZE=1000
    export HBASE_OPTS="-XX:+UseConcMarkSweepGC"


    export HADOOP_HEAPSIZE="500"
    export HADOOP_NAMENODE_INIT_HEAPSIZE="500"

# Hbase 0.98
    HBASE_PREFIX=${HOME}/hbase
    export HBASE_HOME=$HBASE_PREFIX
    export HBASE_COMMON_HOME=$HBASE_PREFIX
    export PATH=$HBASE_HOME/bin:$HBASE_HOME/sbin:$PATH
    
    
    export HBASE_CONF_DIR=${HADOOP_USER_CONF}/hbase_conf
    export HBASE_LOG_DIR=${HADOOP_USER_CONF}/hbase_logs

    export CLASSPATH=$CLASSPATH:./
}
