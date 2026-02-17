#include "util/dyn.h"

#include <dlfcn.h>

bool dyn_add_dir(char *path)
{
    return true; // Not needed on POSIX platforms.
}

DYNHandle dyn_load(char *path)
{
    return dlopen(path, RTLD_LAZY);
}

DYNSymbol dyn_symbol(DYNHandle handle, char *name)
{
    return dlsym((void *)handle, name);
}
