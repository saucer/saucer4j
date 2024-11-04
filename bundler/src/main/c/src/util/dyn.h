#pragma once

#include <stdbool.h>

typedef void *DYNHandle;
typedef void *DYNSymbol;

/**
 * This only does something on Windows.
 */
bool dyn_add_dir(char *path);

DYNHandle dyn_load(char *path);

DYNSymbol dyn_symbol(DYNHandle handle, char *name);
