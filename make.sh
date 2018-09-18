#!/bin/bash

set -e
tasks="${@:2}"

DENVS=""
OIFS="$IFS"
IFS=',' 
for i in $1;
do
    export "$i"
    DENVS="$DENVS -e$i"
done
echo "$DENVS"
IFS="$OIFS"
    

if [ "$USE_IMAGE" = "" ];
then
    export USE_IMAGE=""
fi

if [ "$INSTALL_LOCAL_IMAGE" != "" ];
then
    export USE_IMAGE=""
fi

if [ "$INSTALL_LOCAL_IMAGE" = "1" -o "$INSTALL_LOCAL_IMAGE" = "" ];
then
    export INSTALL_LOCAL_IMAGE="build.dep/dockerBuilder"
fi

if [ "$USE_IMAGE" = "" ];
then
    availablelocalimg="`docker image ls localjmephononbuilder -q`"
    if [ "$availablelocalimg" = "" -o "$REBUILD_LOCAL_IMAGE" = "1" ];
    then
        echo "Install local image $INSTALL_LOCAL_IMAGE"
        curdir="$PWD"
        cd "$INSTALL_LOCAL_IMAGE"
        docker rmi localjmephononbuilder || true
        docker build -t localjmephononbuilder .
        cd "$curdir"
    fi
    export USE_IMAGE="localjmephononbuilder:latest"

else
    echo "Use prebuild image $USE_IMAGE"
fi

if [ "$SUDO_USER" != "" ];
then
    userUID=`id -u $SUDO_USER`
    groupUID=`id -g $SUDO_USER`
    export RUN_AS="-u=$userUID:$groupUID"
else
    export RUN_AS=""
fi



echo "Launch $USE_IMAGE as $RUN_AS"


if [ "$1" = "generic" -o "$tasks" == "bash" ];
then
    if [ "$tasks" != "bash" ];
    then
        tasks="gradle $tasks"
    else 
        RUN_AS=""
    fi
    docker run --rm $DENVS -it $RUN_AS -v$PWD:/workspace $USE_IMAGE $tasks
    exit
fi

if [ "$OS_LINUX" != "" ];
then
    docker run --rm $DENVS -e CROSS_TRIPLE=x86_64-linux-gnu -it $RUN_AS  -v$PWD:/workspace $USE_IMAGE crossbuild gradle $tasks
fi

if [ "$OS_WINDOWS" != "" ];
then
    docker run --rm $DENVS -e CROSS_TRIPLE=x86_64-w64-mingw32 -it $RUN_AS  -v$PWD:/workspace $USE_IMAGE crossbuild gradle $tasks
fi

if [ "$OS_OSX" != "" ];
then
    docker run --rm $DENVS -e CROSS_TRIPLE=x86_64-apple-darwin -it $RUN_AS  -v$PWD:/workspace $USE_IMAGE crossbuild gradle ${@:2}
fi
