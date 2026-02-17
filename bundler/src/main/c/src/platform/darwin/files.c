#include "util/files.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <limits.h>
#include <libgen.h>
#include <mach-o/dyld.h>
#include <CoreFoundation/CoreFoundation.h>

char *files_get_resources_dir()
{
    CFBundleRef bundle = CFBundleGetMainBundle();
    if (bundle != NULL)
    {
        CFURLRef resources_url = CFBundleCopyResourcesDirectoryURL(bundle);
        if (resources_url != NULL)
        {
            char path[PATH_MAX];
            if (CFURLGetFileSystemRepresentation(resources_url, true, (UInt8 *)path, PATH_MAX))
            {
                CFRelease(resources_url);
                return strdup(path);
            }
            CFRelease(resources_url);
        }
    }

    // Not in a bundle — fall back to executable's directory.
    char path[PATH_MAX];

    if (_NSGetExecutablePath(path, &PATH_MAX) != 0)
    {
        printf("Error getting the path of the executable.\n");
        return NULL;
    }

    char *dir = strdup(dirname(path));

    if (dir == NULL)
        return NULL;

    char *abs_dir = realpath(dir, NULL);
    free(dir);
    return abs_dir;
}
