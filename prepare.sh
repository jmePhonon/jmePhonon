#/bin/sh
mkdir -p tmp/tools
mkdir -p tmp/cache
mkdir -p build/natives

source build.dep/safeRm.sh
source build.dep/findJava.sh
source build.dep/findOs.sh



# Get steam audio
if [ "$STEAM_AUDIO_URL" = "" ];
then
    export STEAM_AUDIO_URL="https://github.com/ValveSoftware/steam-audio/releases/download/v2.0-beta.15/steamaudio_api_2.0-beta.15.zip"
    export STEAM_AUDIO_HASH="5b888a84c6bbe79560346338a3a708787645cc8324091b865187d4138df85b43"
fi


export STEAM_AUDIO_URL_HASH="`echo "$STEAM_AUDIO_URL" | sha256sum | cut -d' ' -f1`"


#compareFileHash FILE HASH
function compareFileHash {
    if [ "`cat $1 | sha256sum | cut -d' ' -f1`" != "$2" ];
    then
        echo "fail"
    else
        echo ""
    fi
}

# TODO: Clean this function
function prepareWorkspace {
    forceUpdate=$1
         
    jni_md_folder="jni_md_Windows"
    if [ "$OS_WINDOWS" != "" -a ! -f "tmp/$jni_md_folder/jni_md.h"  ];
    then
        mkdir -p tmp/$jni_md_folder/
        wget "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/raw-file/fd4e976e01bf/src/windows/javavm/export/jni_md.h" -O tmp/$jni_md_folder/jni_md.h
    fi

    jni_md_folder="jni_md_OSX"
    if [ "$OS_OSX" != "" -a ! -f "tmp/$jni_md_folder/jni_md.h"  ];
    then
        mkdir -p tmp/$jni_md_folder/
        wget "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/raw-file/fd4e976e01bf/src/macosx/javavm/export/jni_md.h" -O tmp/$jni_md_folder/jni_md.h
    fi

    jni_md_folder="jni_md_Linux"  
    if [ "$OS_LINUX" != "" -a ! -f "tmp/$jni_md_folder/jni_md.h" ];
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
            
            if [ "`compareSteamAudioHash`" != "" ];
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

        cd doc
        git clone https://github.com/ValveSoftware/steam-audio.git
        cd steam-audio 
        git branch gh-pages
        git reset --hard ec6acfd41f18664fe9726e090d05217f6e2bfaf2
        cp -Rf doc/capi ../steamaudio_api_html
        cd .. 
        safeRm steam-audio

        cd ../../


    fi
################

}

function deepClean {
    safeRm build
    safeRm tmp
    safeRm bin
    safeRm src/steamaudio
    safeRm .gradle
    rm -Rf .Trash    
}


function downloadResources {
    safeRm tmp/res.zip
    wget "https://ci-deploy.frk.wf/p4jme/res.zip" -O tmp/res.zip
    safeRm src/test/resources
    unzip tmp/res.zip -d src/test/resources/
}


function build {
    compiler="$1"
    platform="$2"
    arch="$3"
        liboutfolder="$4"
    args="$5"
    args2="$6"
    libs="$7"

    echo "Compile for $platofrm $arch with $compiler"

    arch_flag="-m64"
    if [ "$arch" = "x86" ];
    then
        arch_flag="-m32"
    fi

    echo '' > tmp/build_IIlist.txt.tmp
    for line in $(cat  tmp/build_IIlist.txt); do
        echo "-I$line " >>  tmp/build_IIlist.txt.tmp 
    done
    cat tmp/build_IIlist.txt.tmp > tmp/build_IIlist.txt
    
    platform_libprefix="lib"
    platform_libsuffix=".so"
   
    if [ "$platform" = "Windows" ];
    then
        platform_libprefix=""
        platform_libsuffix=".dll"
    elif [ "$platform" = "OSX" ];
    then
        platform_libprefix=""
        platform_libsuffix=".dylib"
    fi

    echo "" > tmp/ext_cpplist.txt
      find src/ext -type f -name '*.c' >> tmp/ext_cpplist.txt


    build_script="
    $compiler -mtune=generic  
    -fmessage-length=0 
    -fpermissive 
    -O0 -fno-rtti -shared
    -fPIC  
    -Wall -Werror=implicit-function-declaration 
    -Lsrc/steamaudio/lib/$platform/$arch
    -Isrc/steamaudio/include
    -Isrc/ext
    -Itmp/jni_md_$platform
    -I$JDK_ROOT/include
    $arch_flag   
    $(cat  tmp/build_IIlist.txt)
    $args 
    $(cat  tmp/build_cpplist.txt) 
    $(cat  tmp/ext_cpplist.txt) 
    $args2  -Wl,-Bdynamic -lphonon $libs $BUILD_ARGS"
    cp "src/steamaudio/lib/$platform/$arch/${platform_libprefix}phonon${platform_libsuffix}" "$liboutfolder/"
    echo "Run $build_script"

    $build_script
    if [ $? -ne 0 ]; then exit 1; fi
}



function buildNativeTests {
    echo '' > tmp/build_cpplist.txt
    echo '' > tmp/build_IIlist.txt

    # rm -Rvf build/tests
    mkdir -p build/tests
    cp -Rf src/tests/resources/* build/tests/
    cp -Rf src/steamaudio/lib/Linux/x64/* build/tests/
    
    cd build/tests/resources/
    safeRm inputaudio.raw
    ffmpeg -i 399354__romariogrande__eastandw_mono.ogg -f f32be -acodec pcm_f32le inputaudio.raw
    cd ../../..
    

    build "g++"  \
    "Linux" "x64" \
    "build/test/" \
    "--std=c++11 -obuild/test/SampleApplication1.64" src/test/native/SampleApplication1.cpp
    
    #build "g++"  "Linux" "x64" "--std=c++11 -obuild/tests/SampleApplication2.64" src/tests/SampleApplication2.cpp

  
}
#ffmpeg -i 399354__romariogrande__eastandw_mono.ogg -f f32be -acodec pcm_f32le inputaudio.raw



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
    safeRm tmp/natives
    mkdir -p tmp/natives

    echo '' > tmp/build_cpplist.txt
    echo '' > tmp/build_IIlist.txt
    
    find src/main/natives -type f -name '*.c' >> tmp/build_cpplist.txt
    
    if [ "$OS_LINUX" != "" ];
    then
        #Linux 64
        dest="tmp/natives/linux-x86-64"
        mkdir -p $dest
        build "gcc" \
        "Linux" "x64" \
        "$dest" \
        "--std=gnu99 
        -Isrc/main/natives/include
        -Isrc/main/natives"\
        "-Wl,-soname,jmephonon.so  -otmp/natives/linux-x86-64/libjmephonon.so" \
        ""

        #Linux 32
        dest="tmp/natives/linux-x86"
        mkdir -p $dest
        build "gcc" \
        "Linux" "x86" \
        "$dest" \
        "--std=gnu99
        -Wint-to-pointer-cast
        -Isrc/main/natives/include
        -Isrc/main/natives" \
        "-Wl,-soname,jmephonon.so  -otmp/natives/linux-x86/libjmephonon.so" \
        ""
    fi


    if [ "$OS_WINDOWS" != "" ];
    then
        #Windows 64
        dest="tmp/natives/windows-x86-64"
        mkdir -p $dest
        build "x86_64-w64-mingw32-gcc" \
        "Windows" "x64" \
        "$dest" \
        "-static --std=gnu99 
        -Isrc/main/natives/include
        -Isrc/main/natives" \
        "-Wl,--exclude-all-symbols,--add-stdcall-alias,--kill-at,-soname,jmephonon.dll
          -otmp/natives/windows-x86-64/jmephonon.dll" \
        ""

        #Windows 32
        dest="tmp/natives/windows-x86"
        mkdir -p $dest
           build "i686-w64-mingw32-gcc" \
        "Windows" "x86" \
        "$dest" \
        "-static  --std=gnu99 
        -Isrc/main/natives/include
        -Isrc/main/natives" \
        "-Wl,--exclude-all-symbols,--add-stdcall-alias,--kill-at,-soname,jmephonon.dll
          -otmp/natives/windows-x86/jmephonon.dll" \
        ""
      
    fi

    #Force update vscode
    if [ -d build/resources ];
    then
        cp -Rf tmp/natives/* build/resources/
    fi
    if [ -d bin ];
    then
        cp -Rf tmp/natives/* bin/
    fi
}


function deploy {
    DEPLOY=""
    VERSION=$TRAVIS_COMMIT
    if [ "$TRAVIS_PULL_REQUEST" == "false" -a "$TRAVIS_TAG" != "" ];
    then
        echo "Deploy for $TRAVIS_TAG."
        VERSION=$TRAVIS_TAG
        DEPLOY="1"        
    fi

    if [ "$DEPLOY" = "" -a "$TRAVIS_PULL_REQUEST" == "false" -a  "$TRAVIS_BRANCH" == "master" ];
    then
        echo "Deploy for $TRAVIS_BRANCH."
        VERSION="-SNAPSHOT"
        DEPLOY="1"        
    fi

    safeRm "deploy"
    mkdir -p deploy
    cp build/libs/*.jar deploy/
    for f in deploy/*.jar;
    do
        filename="`basename $f`"
        filename="${filename%.*}"
        echo "$filename ( $f ) read for deploy"
        
        echo "Deploy on $BINTRAY_USER."
        if [ "$DEPLOY" == "1" -a "$BINTRAY_USER" != "" -a "$BINTRAY_KEY" != "" ];
        then
            echo "Deploy $filename ( $f ) to bintray"
            curl -X PUT  -T $f -u$BINTRAY_USER:$BINTRAY_KEY\
            "https://api.bintray.com/content/jmephonon/jmePhonon/jmePhonon/$VERSION/com/jme3/phonon/jmePhonon/$VERSION/$filename-$VERSION.jar?publish=1&override=1"
        fi

    done

}

$@