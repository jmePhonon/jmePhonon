#/bin/bash
export JDK_ROOT="$JAVA_HOME"
export READ_LINK="readlink"
if [ "`which greadlink`" != "" ]; 
then
    echo "Use greadlink"
    export READ_LINK="greadlink"  
fi

export JDK_ROOT=$JAVA_HOME
if [ ! -f "$JDK_ROOT/Headers/jni.h"  -a  ! -f "$JDK_ROOT/include/jni.h" ];
then
    export JDK_ROOT="$($READ_LINK -f `which java` | sed "s:/Commands/java::")"
    if [ ! -f "$JDK_ROOT/Headers/jni.h" ];
    then
        export JDK_ROOT="$JAVA_HOME"
        if [ ! -f "$JDK_ROOT/include/jni.h" ];
        then
            export JDK_ROOT="$($READ_LINK -f `which java` | sed "s:/bin/java::")"
            if [ ! -f "$JDK_ROOT/include/jni.h" ];
            then
                export JDK_ROOT="$($READ_LINK -f `which java` | sed "s:/jre/bin/java::")"
                if [ ! -f "$JDK_ROOT/include/jni.h" ];
                then
                    echo "Can't find JDK"
                fi
            fi
        fi
    fi
fi
