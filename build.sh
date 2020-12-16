#/bin/sh
mkdir -p tmp/tools
mkdir -p tmp/cache
mkdir -p build/natives

source build.dep/safeRm.sh
source build.dep/findJava.sh
source build.dep/findOs.sh
source build.dep/uploadToMaven.sh

    set -e


# Get steam audio
if [ "$STEAM_AUDIO_URL" = "" ];
then
    export STEAM_AUDIO_URL="https://github.com/ValveSoftware/steam-audio/releases/download/v2.0-beta.17/steamaudio_api_2.0-beta.17.zip"
    export STEAM_AUDIO_HASH="cef95d302fb2439da9c1661e8544ccc0f30871a01be7d9864e081a91f4cd229e"

    #Downgrade
    # export STEAM_AUDIO_URL="https://github.com/ValveSoftware/steam-audio/releases/download/v2.0-beta.15/steamaudio_api_2.0-beta.15.zip"
    # export STEAM_AUDIO_HASH="5b888a84c6bbe79560346338a3a708787645cc8324091b865187d4138df85b43"
fi


export STEAM_AUDIO_URL_HASH="`echo "$STEAM_AUDIO_URL" | sha256sum | cut -d' ' -f1`"


#compareFileHash FILE HASH
function compareFileHash {
    if [ ! -f "$1" ];
    then
        echo "fail"
    else
        if [ "`cat $1 | sha256sum | cut -d' ' -f1`" != "$2" ];
        then
            echo "fail"
        else
            echo ""
        fi
    fi
}

# TODO: Clean this function
function prepareWorkspace {
    forceUpdate=$1
         
    jni_md_folder="jni_md_Windows"
    # "$OS_WINDOWS" != "" -a
    if [  ! -f "tmp/$jni_md_folder/jni_md.h"  ];
    then
        mkdir -p tmp/$jni_md_folder/
        wget "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/raw-file/fd4e976e01bf/src/windows/javavm/export/jni_md.h" -O tmp/$jni_md_folder/jni_md.h
    fi

    jni_md_folder="jni_md_OSX"
    # "$OS_OSX" != "" -a
    if [  ! -f "tmp/$jni_md_folder/jni_md.h"  ];
    then
        mkdir -p tmp/$jni_md_folder/
        wget "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/raw-file/fd4e976e01bf/src/macosx/javavm/export/jni_md.h" -O tmp/$jni_md_folder/jni_md.h
    fi

    jni_md_folder="jni_md_Linux"  
    # "$OS_LINUX" != "" -a 
    if [ ! -f "tmp/$jni_md_folder/jni_md.h" ];
    then
            mkdir -p tmp/$jni_md_folder/

         wget "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/raw-file/fd4e976e01bf/src/solaris/javavm/export/jni_md.h" -O tmp/$jni_md_folder/jni_md.h
    fi


  



    #Steam audio
    if [ "$forceupdate" != "" ];
    then
        export UPDATE_STEAMAUDIO=1
    fi

    if [ "$UPDATE_STEAMAUDIO" = "" ];
    then
        export UPDATE_STEAMAUDIO="`compareFileHash tmp/cache/$STEAM_AUDIO_URL_HASH.zip $STEAM_AUDIO_HASH`"
    fi
    if [  ! -f src/steamaudio/include/phonon.h -o  "$UPDATE_STEAMAUDIO" != "" ];
    then
        safeRm tmp/ext_sta
        mkdir -p tmp/ext_sta
        if [ ! -f tmp/cache/$STEAM_AUDIO_URL_HASH.zip -o "$UPDATE_STEAMAUDIO" != "" ];
        then
            echo "Download steam audio"
            wget "$STEAM_AUDIO_URL" -O tmp/cache/$STEAM_AUDIO_URL_HASH.zip
            
            if [ "`compareFileHash tmp/cache/$STEAM_AUDIO_URL_HASH.zip $STEAM_AUDIO_HASH`" != "" ];
            then
                echo "Error. Steamaudio hash is wrong or the download is corrupted"
                safeRm  tmp/cache/$STEAM_AUDIO_URL_HASH.zip
                exit 1
            fi
        cp tmp/cache/$STEAM_AUDIO_URL_HASH.zip tmp/ext_sta/steamaudio.zip
        else
            echo "Use steam audio from cache tmp/cache/$STEAM_AUDIO_URL_HASH.zip"
            cp tmp/cache/$STEAM_AUDIO_URL_HASH.zip tmp/ext_sta/steamaudio.zip
        fi
        cd tmp/ext_sta
        unzip steamaudio.zip
        safeRm steamaudio.zip
        
        safeRm ../../src/steamaudio
        mkdir -p ../../src/steamaudio
        cp -Rf steamaudio_api/* ../../src/steamaudio/
        
        cd ..
        safeRm ext_sta

        cd ../src/steamaudio/

        cp -Rf bin/* lib/
        safeRm bin

        safeRm steam-audio

        cd ../../


    fi
################

}

function deepClean {
    safeRm build
    safeRm tmp
    safeRm bin
    rm hs_err_pid*.log
    safeRm src/steamaudio
    safeRm .gradle
    rm -Rf .Trash    
}


function downloadResources {
    safeRm tmp/res.zip
    wget "https://ci-deploy.frk.wf/jmePhonon/res.zip" -O tmp/res.zip
    safeRm src/test/resources
    unzip tmp/res.zip -d src/test/resources/
}



# buildTests
function removeEmptyJNIh {
    file=$1
    if ! grep -q "JNIEXPORT" "$file" ;
    then 
        safeRm "$file"
    fi 
}
export -f removeEmptyJNIh

## build natives
function genJNI {
    classpath="$1"
    rootDir="$2"
    output="$3"
    file="$4"
    file="${file#$rootDir/}"
    file="${file//\//.}"
    file="${file%.class}"
    echo "Gen JNI header for $file in $output" 
    mkdir -p "$output"
    javah -jni -d "$output" -classpath "$classpath" "$file"
    # jnih="${file//./_}"
    # jnih="$output/${jnih//\$/_}.h"
    # echo "Check $jnih"
    find "$output" -name "*.h" -exec   bash -c "removeEmptyJNIh {}" \;

    # if ! grep -q "JNIEXPORT" "$jnih" ;
    # then
    #     echo "rm Empty binding $jnih"
    #     rm "$jnih"
    # fi  
}
export -f genJNI

function updateJNIHeaders {
    echo "Update JNI headers..."
    safeRm src/main/natives/include
    classpath=$1
    rootDir=$2
    output=$3
    echo "Search $rootDir for class files"
    find "$rootDir" -name "*.class" -exec bash -c "genJNI \"$classpath\" \"$rootDir\" \"$output\" {} " \;
}


function buildNatives {
    mkdir -p tmp/natives



    
    # echo '' > tmp/build_IIlist.txt
    # echo '' > tmp/build_IIlist.txt.tmp
    # for line in $(cat  tmp/build_IIlist.txt); do
    #     echo "-I$line " >>  tmp/build_IIlist.txt.tmp 
    # done
    # cat tmp/build_IIlist.txt.tmp > tmp/build_IIlist.txt


    echo '' > tmp/build_cpplist.txt    
    find -L src/main/natives -type f -name '*.c' >> tmp/build_cpplist.txt


#     echo "" > tmp/ext_cpplist.txt
#     find -L src/ext -type f -name '*.c' >> tmp/ext_cpplist.txt || true

    platform="Linux"
    platform2="none"
    arch="x86_64"
    platform_libprefix="lib"
    platform_libsuffix=".so"
    liboutfolder="tmp/natives/native/linux/x86_64"
    args="-O0 -g"
    args2=""
    compiler="clang"
    largs=""
    largs2=""
    if [ "$DEBUG" == "" ];
    then
        args="-O3"
    fi

    if [ "$CROSS_TRIPLE" == "" ];
    then
        CROSS_TRIPLE="x86_64"
        if [ "$OS_LINUX" != "" ];
        then
            CROSS_TRIPLE="$CROSS_TRIPLE-linux-gnu"
        elif [ "$OS_WINDOWS" != "" ];
        then
            CROSS_TRIPLE="$CROSS_TRIPLE-w64-mingw32"
        elif [ "$OS_OSX" != "" ];
        then
            CROSS_TRIPLE="$CROSS_TRIPLE-apple-darwin"
        fi
    fi
    
    if [ "$CROSS_TRIPLE" != "" ];
    then
    
        #x86_64-linux-gnu
        #x86_64-w64-mingw32
        #x86_64-apple-darwin
        IFS=- read arch platform platform2 <<< "$CROSS_TRIPLE"
        if [ "$platform" = "w64" ];
        then
            compiler="x86_64-w64-mingw32-gcc"
            platform="Windows"
            platform_libprefix=""
            platform_libsuffix=".dll"
            args="$args "
            args2="-static"
            largs2="-Wl,-Bdynamic"
            largs="-Wl,--exclude-all-symbols,--add-stdcall-alias,--kill-at,-soname,jmephonon${platform_libsuffix}"
        elif [ "$platform" = "apple" ];
        then
            compiler="cc"
            platform="OSX"
            args="$args -dynamiclib  -flat_namespace -undefined suppress "
            args2="-static"
            platform_libprefix="lib"
            platform_libsuffix=".dylib"
            largs=""
        else
            compiler="cc"
            platform="Linux"
            args="$args "
            platform_libprefix="lib"
            platform_libsuffix=".so"
            largs2="-Wl,-Bdynamic"
            largs="-Wl,-soname,jmephonon${platform_libsuffix}"
        fi
        liboutfolder="tmp/natives/native/$platform/$arch"
        safeRm "$liboutfolder"

        mkdir -p $liboutfolder
    fi
    if [ "$arch" = "x86_64" ];
    then
        arch="x64"
    else
        arch="x86"
    fi
    if [ "$platform" = "OSX" ];
    then
        arch=""
    fi
# 
    build_script="
    $compiler -mtune=generic  
    -fmessage-length=0 
    $args
    -shared
    -fPIC  
    -Wall -Werror=implicit-function-declaration 
    -Lsrc/steamaudio/lib/$platform/$arch
    -Isrc/steamaudio/include
    -Isrc/ext
    -Itmp/jni_md_$platform
    -I$JDK_ROOT/include
    $args2 -m64 --std=gnu99 
    -Isrc/main/natives/include
    -Isrc/main/natives
    $(cat  tmp/build_cpplist.txt) 
    ${largs}  -o$liboutfolder/${platform_libprefix}jmephonon${platform_libsuffix}
    $largs2 -lphonon ${BUILD_ARGS}"
    cp "src/steamaudio/lib/$platform/$arch/${platform_libprefix}phonon${platform_libsuffix}" "$liboutfolder/"
    echo "Run $build_script"
    `$build_script`
  
}


function deploy {
    DEPLOY=""
    VERSION=$TRAVIS_COMMIT
    if [ "$TRAVIS_PULL_REQUEST" == "false" -a "$TRAVIS_TAG" != "" ];
    then
        echo "Deploy for $TRAVIS_TAG from $PWD."
        VERSION=$TRAVIS_TAG
        DEPLOY="1"        
    fi



    if [ "$DEPLOY" == "1" -a "$BINTRAY_USER" != "" -a "$BINTRAY_KEY" != "" ];
    then
        uploadAllToMaven deploy \
        https://api.bintray.com/maven/jmephonon/jmePhonon/ \
        $BINTRAY_USER $BINTRAY_KEY "https://github.com/jmePhonon/jmePhonon" "BSD 3-Clause"
    fi


}

function licenseGen {
    shopt -s globstar
    for f in src/**/*.{java,c,h};
    do
        if ! grep -q "Copyright" "$f" ;
        then     
            license="`cat build.dep/HEADER.template`"
            echo "$license" > /tmp/___licenseGen.tmp
            cat "$f" >> /tmp/___licenseGen.tmp
            mv /tmp/___licenseGen.tmp $f        
        fi
    done
}



$@
