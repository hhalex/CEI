export JAVA_HOME=/usr/lib/jvm/java

case "$TERM" in
"dumb")
    export PS1="> "
    ;;
xterm*|rxvt*|eterm*|screen*)
    export PS1='\[\033[00;32m\]Mickael [\h] > \[\033[00m\]'
    ;;
*)
    export PS1="> "
    ;;
esac

source ~/.hadoop_user_conf/scripts/hadoop_config.sh

hadoop_setup_folders;
hadoop_export_constants;

source ~/.hadoop_user_conf/scripts/hbase_config.sh

hbase_setup_folders;
hbase_export_constants;
