#pragma once

#include <jni.h>
#include <stdbool.h>

struct BundleInfo
{
    char *main;
    JavaVMOption *optv;
    int optc;
};

/**
 * Parse a bundle.json file into a BundleInfo struct.
 * Returns NULL on failure (errors are logged to stdout).
 */
struct BundleInfo *bundle_parse(char *path);
