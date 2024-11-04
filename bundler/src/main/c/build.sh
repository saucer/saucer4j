#!/bin/bash

set -e -o pipefail

if [ "$#" -ne 3 ]; then
    echo "Usage: ./build.sh <arch> <os> <abi>"
    exit 1
fi

SOURCES="src/launcher.c src/java.c src/util/dyn.c src/util/files.c src/util/strings.c src/cJSON/cJSON_Utils.c src/cJSON/cJSON.c" # src/util/thread.c 
ZIG_TARGET="$1-$2-$3"

echo "Building for $ZIG_TARGET..."

if [ $2 = "windows" ]; then
    EXEC_EXT=".exe"
    INCLUDES="-I_include/JNI -I_include/JNI/win32"
    EXTRA_ARGS="/SUBSYSTEM:WINDOWS -s -luser32 -lshell32"
elif [ $2 = "linux" ]; then
    EXEC_EXT=""
    INCLUDES="-I_include/JNI -I_include/JNI/linux -lpthread"
    EXTRA_ARGS=""
elif [ $2 = "macos" ]; then
    EXEC_EXT=""
    # You can use https://github.com/hexops-graveyard/sdk-macos-12.0 to get framework headers on Linux.
    INCLUDES="-I_include/JNI -I_include/JNI/darwin -lpthread" #  -F/home/ubuntu/include/sdk-macos-12.0-main/root/System/Library/Frameworks -framework CoreFoundation
    EXTRA_ARGS=""
fi

rm -rf "build/$ZIG_TARGET"
mkdir -p "build/$ZIG_TARGET"
zig cc -target $ZIG_TARGET $INCLUDES $EXTRA_ARGS $SOURCES -o "build/$ZIG_TARGET/launcher$EXEC_EXT"

echo "Finished building $ZIG_TARGET!"
