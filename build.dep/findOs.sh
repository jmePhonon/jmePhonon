#!/bin/bash
os="$(uname -s)"
if [[ "$os" == Linux* ]];
then
    echo "Running on linux"
    export OS_LINUX=1
fi
if [[ "$os" == Darwin* ]];
then
    echo "Running on mac"
    export OS_OSX=1
fi
if [[ "$os" == CYGWIN* ]];
then
    echo "Running on windows"
    export OS_WINDOWS=1
fi
if [[ "$os" == MINGW* ]];
then
    echo "Running on windows"
    export OS_WINDOWS=1
fi