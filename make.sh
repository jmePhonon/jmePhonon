#!/bin/bash

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
        docker rmi localjmephononbuilder
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

DENVS=""
OIFS="$IFS"
IFS=',' 
for i in $1;
do
    DENVS="$DENVS -e$i"
done
echo "$DENVS"

IFS="$OIFS"
echo "Launch $USE_IMAGE as $RUN_AS"
docker run --rm $DENVS -it $RUN_AS  -v$PWD:/workspace $USE_IMAGE gradle ${@:2}