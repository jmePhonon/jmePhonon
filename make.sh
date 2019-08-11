#!/bin/bash
cd "`dirname $0`"

set -e

# Initialize various optional settings
if [ "$USE_IMAGE" = "" ];then export USE_IMAGE="riccardoblb/buildenv-14all"; fi

DENVS="" #environment variables
RUN_AS="" #user used inside the docker container
TASKS="prepareWorkspace build deploy"  #build tasks (default=build)
MULTI_BUILD="" #csv os triplets

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

if [ "$OS_LINUX" != "" ];
then
    if [ "$MULTI_BUILD" != "" ]; then MULTI_BUILD="$MULTI_BUILD,"; fi
    MULTI_BUILD="${MULTI_BUILD}-eCROSS_TRIPLE=x86_64-linux-gnu"
fi

if [ "$OS_WINDOWS" != "" ];
then
    if [ "$MULTI_BUILD" != "" ]; then MULTI_BUILD="$MULTI_BUILD,"; fi
    MULTI_BUILD="${MULTI_BUILD}-eCROSS_TRIPLE=x86_64-w64-mingw32"
fi

if [ "$OS_OSX" != "" ];
then
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
docker pull $USE_IMAGE

# Run build for each target
OIFS="$IFS"
IFS=',' 
for triplet in $MULTI_BUILD;
do
    echo "Run for $triplet"
    cmd="docker run -v\"$PWD:/workdir\" -eGRADLE_USER_HOME=/workdir/build.cache -w /workdir $triplet $DENVS $RUN_AS --rm -it $USE_IMAGE $TASKS"
    echo $cmd
    eval $cmd
done
IFS="$OIFS"

