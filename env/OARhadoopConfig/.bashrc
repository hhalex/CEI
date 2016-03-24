export PATH=/opt/cuda/bin:$PATH
export PATH=/opt/openmpi/bin/:$PATH
export LD_LIBRARY_PATH=/opt/cuda/lib64:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH=/opt/openmpi/lib/:$LD_LIBRARY_PATH
source ~/.aliases

export JAVA_HOME=/usr/lib/jvm/java

case "$TERM" in
"dumb")
    export PS1="> "
    ;;
xterm*|rxvt*|eterm*|screen*)
    export PS1='\[\033[00;32m\]Alex [\h] > \[\033[00m\]'
    ;;
*)
    export PS1="> "
    ;;
esac

source ~/.hadoop_user_conf/scripts/hadoop_config.sh

hadoop_setup_folders;
hadoop_export_constants;
hadoop_setup_namenode;

source ~/.hadoop_user_conf/scripts/hbase_config.sh

hbase_setup_folders;
hbase_export_constants;
hbase_setup_namenode;
