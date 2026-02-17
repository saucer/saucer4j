#!/bin/bash

set -e -o pipefail

if [ "$#" -ne 3 ]; then
    echo "Usage: ./build.sh <arch> <os> <abi>"
    exit 1
fi

COMMON_SOURCES="src/launcher.c src/java.c src/bundle.c src/util/files.c src/util/strings.c src/cJSON/cJSON_Utils.c src/cJSON/cJSON.c"
ZIG_TARGET="$1-$2-$3"
JNI_DIR="_include/jni/versions/8/include"

echo "Building for $ZIG_TARGET..."

if [ $2 = "windows" ]; then
    PLATFORM_SOURCES="src/platform/win32/platform.c src/platform/win32/dyn.c src/platform/win32/files.c src/platform/win32/strings.c"
    EXEC_EXT=".exe"
    INCLUDES="-I$JNI_DIR/win32"
    EXTRA_ARGS="-Wl,--subsystem,windows -s -luser32 -lshell32"
elif [ $2 = "linux" ]; then
    PLATFORM_SOURCES="src/platform/linux/platform.c src/platform/posix/platform.c src/platform/posix/dyn.c src/platform/posix/files.c src/platform/linux/files.c"
    EXEC_EXT=""
    INCLUDES="-I$JNI_DIR/linux -lpthread"
    EXTRA_ARGS=""
elif [ $2 = "macos" ]; then
    PLATFORM_SOURCES="src/platform/darwin/platform.c src/platform/posix/platform.c src/platform/posix/dyn.c src/platform/posix/files.c src/platform/darwin/files.c"
    EXEC_EXT=""
    INCLUDES="-I$JNI_DIR/darwin -lpthread"
    EXTRA_ARGS="-framework CoreFoundation"
fi

if [ ! -d "$JNI_DIR" ]; then
    echo "JNI headers not found, cloning https://github.com/Casterlabs/jni-headers.git..."
    git clone --depth=1 https://github.com/Casterlabs/jni-headers.git _include/jni
fi

if [ $2 = "macos" ] && [ "$(uname -s)" != "Darwin" ]; then
    # Cross-compiling for macOS from another OS.
    SDK_DIR="_include/sdk-macos-12.0"
    INCLUDES="$INCLUDES -F$SDK_DIR/root/System/Library/Frameworks"
    if [ ! -d "$SDK_DIR" ]; then
        echo "macOS SDK not found, cloning https://github.com/hexops-graveyard/sdk-macos-12.0..."
        git clone --depth 1 https://github.com/hexops-graveyard/sdk-macos-12.0.git "$SDK_DIR"
    fi
fi

rm -rf "build/$ZIG_TARGET"
mkdir -p "build/$ZIG_TARGET"
zig cc -target $ZIG_TARGET -Isrc -I$JNI_DIR $INCLUDES $EXTRA_ARGS $COMMON_SOURCES $PLATFORM_SOURCES -o "build/$ZIG_TARGET/launcher$EXEC_EXT"

echo "Finished building $ZIG_TARGET!"
