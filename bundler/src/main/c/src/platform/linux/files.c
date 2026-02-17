#include "util/files.h"

#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <libgen.h>

char *files_get_resources_dir()
{
    char path[PATH_MAX + 1];

    int len = readlink("/proc/self/exe", path, PATH_MAX);
    if (len == -1)
        return NULL;

    path[len] = '\0'; // Null-terminate

    // dirname may return a pointer into the stack buffer, so copy.
    return strdup(dirname(path));
}
