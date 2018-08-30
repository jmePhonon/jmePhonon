#!/bin/bash
cd /workspace
if [ "$NO_CACHE" != "" ];
then
    export GRADLE_USER_HOME="/tmp/gradleHome"
else
    export GRADLE_USER_HOME="/workspace/tmp/dockerBuildCache/gradleHome"
fi
mkdir -p "$GRADLE_USER_HOME"

if [ "$1" = "" ];
then
    bash
else
    $@
fi
