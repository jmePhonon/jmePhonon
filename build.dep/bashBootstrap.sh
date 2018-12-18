#!/bin/bash

set -e

# Initialize various optional settings
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

DENVS="" #environment variables
WRAPPER="" #wrapper script
MULTI_BUILD="" #csv os triplets
RUN_AS="" #user used inside the docker container
TASKS="prepareWorkspace build deploy"  #build tasks (default=build)

#Configure for windows 10
if [ -d "/mnt/c/Users" ]; then
    echo "Running on windows 10"
    export PATH="$HOME/bin:$HOME/.local/bin:$PATH"
    export PATH="$PATH:/mnt/c/Program\ Files/Docker/Docker/resources/bin"
    alias docker=docker.exe
fi

#Unpack and set env variables
OIFS="$IFS"
IFS=',' 
for i in $1;
do
    export "$i"
    if [ "$DENVS" != "" ]; then DENVS="$DENVS " ; fi
    DENVS="${DENVS}-e$i"
done
IFS="$OIFS"

#Read tasks from args
itasks="${@:2}"
if [ "$itasks" != "" ];
then
    TASKS="$itasks"
fi
    
# Build local image if needed
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
    echo "Use prebuilt image $USE_IMAGE"
fi


# Use non sudo user in docker, this will preserve files' permissions
if [ "$SUDO_USER" != "" ];
then
    userUID=`id -u $SUDO_USER`
    groupUID=`id -g $SUDO_USER`
    RUN_AS="-u=$userUID:$groupUID"
else
    RUN_AS=""
fi



echo "Launch $USE_IMAGE as $RUN_AS, running tasks: $TASKS"

#pack targets into a csv list
if [ "$1" = "" -o "$1" = "generic" -o "$OS_GENERIC" != "" ];
then
    if [ "$MULTI_BUILD" != "" ]; then MULTI_BUILD="$MULTI_BUILD,"; fi
    MULTI_BUILD="${MULTI_BUILD}-eGENERIC=1"
fi

if [ "$OS_LINUX" != "" ];
then
    WRAPPER="crossbuild"
    if [ "$MULTI_BUILD" != "" ]; then MULTI_BUILD="$MULTI_BUILD,"; fi
    MULTI_BUILD="${MULTI_BUILD}-eCROSS_TRIPLE=x86_64-linux-gnu"
fi

if [ "$OS_WINDOWS" != "" ];
then
    WRAPPER="crossbuild"
    if [ "$MULTI_BUILD" != "" ]; then MULTI_BUILD="$MULTI_BUILD,"; fi
    MULTI_BUILD="${MULTI_BUILD}-eCROSS_TRIPLE=x86_64-w64-mingw32"
fi

if [ "$OS_OSX" != "" ];
then
    WRAPPER="crossbuild"
    if [ "$MULTI_BUILD" != "" ]; then MULTI_BUILD="$MULTI_BUILD,"; fi
    MULTI_BUILD="${MULTI_BUILD}-eCROSS_TRIPLE=x86_64-apple-darwin"
fi



# We have a special `bash` task, that is run as root inside the docker container
# Mostly useful to debug the container
if [ "$TASKS" != "bash" ]; # If not a `bash` task, run it with gradle
then
    TASKS="gradle $TASKS"
else
    RUN_AS="" # bash task is run as root
fi


# Run build for each target
OIFS="$IFS"
IFS=',' 
for triplet in $MULTI_BUILD;
do
    echo Run for $triplet
    cmd=$(echo docker run -v"$PWD:/workspace" -w /workspace $DENVS $triplet $RUN_AS --rm -it $USE_IMAGE $WRAPPER $TASKS)
    echo "Run: $cmd"
    eval $cmd
done
IFS="$OIFS"


