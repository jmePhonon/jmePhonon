#/bin/bash
if [ "$TRASH" == "" ];
then
    export TRASH="$PWD/.Trash"
fi
function timeNow {
    date +"%Y-%m-%d-%H-%M-%S"
}
export SRMDATE="`timeNow`"
function safeRm {
    if [ "$2" != "" ];
    then
        echo "safeRm: Error, can't remove $@"
    else
        if [ "$TRASH" == "notrash" ];
        then
            echo "safeRm: Remove $1"   
            rm -Rf "$1"
        else
            trashpath="$TRASH/$SRMDATE/"
            if [ -f "$trashpath/$1" -o -d "$trashpath/$1" ];
            then
                trashpath="$trashpath-`date +%s`"
                sleep 2
            fi
            mkdir -p "$trashpath"
            echo "safeRm: Move $1 to Trash $trashpath"   
            mv "$1" "$trashpath" || true
        fi
    fi
}
export -f safeRm
