#pragma once

#include <stdbool.h>

#include "util/dyn.h"

/**
 * Platform-specific initialization (e.g. DLL directory setup on Windows).
 * Returns false on failure.
 */
bool platform_init(char *cwd);

/**
 * Load the JVM shared library for the current platform.
 * Returns NULL on failure.
 */
DYNHandle platform_load_jvm(char *cwd);

/**
 * Common launcher logic. Called by the platform-specific entry point.
 */
int launcher_main(int argc, char *argv[]);
