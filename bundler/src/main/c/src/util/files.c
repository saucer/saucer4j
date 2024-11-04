#include "files.h"

#include <stdlib.h>
#include <stdio.h>

#if defined(_WIN32)
#include <windows.h>
#include <libloaderapi.h>

char *files_get_executable_dir()
{
    char *cwd = malloc((MAX_PATH + 1) * sizeof(char));
    if (cwd == NULL)
        return NULL;

    DWORD length = GetModuleFileName(NULL, cwd, MAX_PATH);
    if (length == 0)
    {
        printf("Error getting the cwd of the executable.\n");
        return NULL;
    }

    // Extract directory part of the cwd
    char *last_backslash = strrchr(cwd, '\\');
    if (last_backslash == NULL)
        return NULL;

    *last_backslash = '\0'; // Null-terminate

    return cwd;
}

bool files_change_cwd(char *new_path)
{
    return SetCurrentDirectory(new_path);
}

#elif defined(__APPLE__)
#include <unistd.h>
#include <limits.h>
#include <libgen.h>
#include <string.h>
#include <mach-o/dyld.h>

#include "strings.h"

char *files_get_executable_dir()
{
    uint32_t bufsize = 0;
    _NSGetExecutablePath(NULL, &bufsize); // Get the required buffer size

    char *path = malloc(bufsize); // Allocate buffer
    if (path == NULL)
    {
        printf("Error getting the cwd of the executable.\n");
        return NULL;
    }

    if (_NSGetExecutablePath(path, &bufsize) != 0)
    {
        free(path);
        return NULL;
    }

    char *dir = dirname(path);
    free(path);

    if (strstr(path, ".app/Contents/MacOS/") == NULL)
    {
        // Fallback to the executable's directory. This is probably fine.
        char *abs_dir = realpath(dir, NULL);
        free(dir);
        return abs_dir;
    }
    else
    {
        // We're a bundle, so resolve the Resources directory.
        char *r_resources = strings_concat(dir, "../Resources");
        free(dir);
        if (r_resources == NULL)
            return NULL;

        char *abs_resources = realpath(r_resources, NULL);
        free(r_resources);
        return abs_resources;
    }
}

bool files_change_cwd(char *new_path)
{
    return chdir(new_path) == 0;
}

#else
#include <unistd.h>
#include <limits.h>
#include <libgen.h>

char *files_get_executable_dir()
{
    char path[PATH_MAX + 1];

    int len = readlink("/proc/self/exe", path, PATH_MAX);
    if (len == -1)
        return NULL;

    path[len] = '\0'; // Null-terminate

    return dirname(path);
}

bool files_change_cwd(char *new_path)
{
    return chdir(new_path) == 0;
}

#endif

char *files_contents(char *path)
{
    FILE *file = fopen(path, "rb");
    if (file == NULL)
    {
        printf("Could not read %s, exiting.\n", path);
        return NULL;
    }

    fseek(file, 0L, SEEK_END);
    long int len = ftell(file);

    char *result = malloc(len + 1);

    rewind(file);
    size_t read = fread(result, 1, len, file);

    if (read < len)
    {
        printf("Error reading %s (%zd bytes vs %ld)\n", path, read, len);
        fclose(file);
        free(result);
        return NULL;
    }

    result[read] = '\0'; // Null-terminate the string
    fclose(file);

    return result;
}
