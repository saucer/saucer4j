#!/bin/bash

set -e -o pipefail

# --------------------
#       Windows
# --------------------
./build.sh x86_64 windows msvc

# --------------------
#        Linux
# --------------------
./build.sh aarch64 linux gnu.2.36
./build.sh arm linux gnueabihf.2.36
./build.sh powerpc64le linux gnu.2.36
./build.sh x86_64 linux gnu.2.36

# --------------------
#        macOS
# --------------------
./build.sh aarch64 macos none
./build.sh x86_64 macos none
