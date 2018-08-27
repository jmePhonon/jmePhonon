#/bin/sh
echo "Premake $PWD"
mkdir -p tmp/tools
mkdir -p tmp/cache
# if [ ! -f jnaerator/target/jnaerator-*-shaded.jar ];
# then
#     cd tmp/tools
#     git clone http://github.com/nativelibs4java/JNAerator.git
#     cd JNAerator
#     mvn clean install
#     cd ../../..
# fi

# if [ "$CONVERT_DOC" = "" ];
# then
#     export CONVERT_DOC="true"
# fi

if [ "$STEAM_AUDIO_URL" = "" ];
then
    export STEAM_AUDIO_URL="https://github.com/ValveSoftware/steam-audio/releases/download/v2.0-beta.14/steamaudio_api_2.0-beta.14.zip"
fi

export STEAM_AUDIO_URL_HASH="`echo "$STEAM_AUDIO_URL" | sha256sum | cut -d' ' -f1`"

if [  ! -f src/steamaudio/include/phonon.h -o  "$REGEN_BINDINGS" != "" ];
then
    rm -Rf tmp/ext_sta
    mkdir -p tmp/ext_sta
    if [ ! -f tmp/cache/$STEAM_AUDIO_URL_HASH.zip ];
    then
        echo "Download steam audio"
        wget "$STEAM_AUDIO_URL" -O tmp/cache/$STEAM_AUDIO_URL_HASH.zip
       cp tmp/cache/$STEAM_AUDIO_URL_HASH.zip tmp/ext_sta/steamaudio.zip
    else
        echo "Use steam audio from cache tmp/cache/$STEAM_AUDIO_URL_HASH.zip"
        cp tmp/cache/$STEAM_AUDIO_URL_HASH.zip tmp/ext_sta/steamaudio.zip
    fi
    cd tmp/ext_sta
    unzip steamaudio.zip
    rm steamaudio.zip
    
    rm -Rf ../../src/steamaudio
    mkdir -p ../../src/steamaudio
    cp -Rf steamaudio_api/* ../../src/steamaudio/
    
    cd ..
    rm -Rf ext_sta

    cd ../src/steamaudio/

    cp -Rf bin/* lib/
    rm -Rf bin

    # mkdir -p lib/linux-x86
    # cp lib/Linux/x86/*.so lib/linux-x86/

    # mkdir -p lib/linux-x86-64
    # cp lib/Linux/x64/*.so lib/linux-x86-64

    # mkdir -p lib/win32-x86
    # cp bin/Windows/x86/*.dll lib/win32-x86

    # mkdir -p lib/win32-x86-64
    # cp bin/Windows/x64/*.dll lib/win32-x86-64

    # mkdir -p lib/darwin
    # cp lib/OSX/*.dylib lib/darwin

    # rm -Rf lib/Android
    # rm -Rf lib/Linux
    # rm -Rf lib/OSX
    # rm -Rf lib/Windows

    # rm -Rf bin

    #apt-get install htmldoc archmage
    if [ "$CONVERT_DOC" == "true" ];
    then
        cd doc
        archmage -c pdf steamaudio_api.chm steamaudio_api.pdf
        # archmage -c html steamaudio_api.chm steamaudio_api.html
        cd ..
    fi
    cd ../../
fi

  #  -shared -Wl,-soname,phonon.so -o $OUT_PATH/libphonon.so 

function build {
    compiler=$1
    platform=$2
    arch=$3
    args=$4
    file=$5

    echo "Compile for $platofrm $arch with $compiler"

    arch_flag="-m64"
    if [ "$arch" = "x86" ];
    then
        arch_flag="-m32"
    fi

    for line in $(cat  tmp/build_IIlist.txt); do
        echo "-I$line " >>  tmp/build_IIlist.txt
    done
    
    build_script="
    $compiler -mtune=generic  
    -fmessage-length=0 
    -fpermissive 
    -O2 -fno-rtti
    -fPIC 
    -Wall 
    $args $arch_flag 
    -Lsrc/steamaudio/lib/$platform/$arch
    -Isrc/steamaudio/include
   
    $(cat  tmp/build_IIlist.txt)
    $(cat  tmp/build_cpplist.txt) $file  -lphonon
    "
    echo "Run $build_script"

    $build_script
    if [ $? -ne 0 ]; then exit 1; fi
}

echo '' > tmp/build_cpplist.txt
echo '' > tmp/build_IIlist.txt


function clean {
    gradle clean
    rm -Rf build
    rm -Rf tmp
    rm -Rf bin
    rm -Rf src/steamaudio
}

function buildNativeTests {
    # rm -Rvf build/tests
    mkdir -p build/tests
    cp -Rf src/tests/resources/* build/tests/
    cp -Rf src/steamaudio/lib/Linux/x64/* build/tests/
    
    cd build/tests/resources/
    rm inputaudio.raw
    ffmpeg -i 399354__romariogrande__eastandw_mono.ogg -f f32be -acodec pcm_f32le inputaudio.raw
    cd ../../..
    
    build "g++"  "Linux" "x64" "--std=c++11 -obuild/test/SampleApplication1.64" src/test/native/SampleApplication1.cpp
    
    #build "g++"  "Linux" "x64" "--std=c++11 -obuild/tests/SampleApplication2.64" src/tests/SampleApplication2.cpp

  
}
#ffmpeg -i 399354__romariogrande__eastandw_mono.ogg -f f32be -acodec pcm_f32le inputaudio.raw


# buildTests

## build natives
function genJNI {
    classpath=$1
    rootDir=$2
    output=$3
    file=$4
    file="${file#$rootDir/}"
    file="${file//\//.}"
    file="${file%.class}"
    echo "Gen JNI header for $file in $output" 
    mkdir -p "$output"
    javah -jni -d "$output" -classpath "$classpath" "$file"
    jnih="$output/${file//./_}.h"
    if ! grep -q "JNIEXPORT" "$jnih" ;
    then
        echo "rm Empty binding $jnih"
        rm "$jnih"
    fi  
}
export -f genJNI

function updateJNIHeaders {
    echo "Update JNI headers..."
    classpath=$1
    rootDir=$2
    output=$3
    echo "Search $rootDir for class files"
    find "$rootDir" -name "*.class" -exec bash -c "genJNI \"$classpath\" \"$rootDir\" \"$output\" {} " \;
}

$@