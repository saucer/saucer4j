#include "util/files.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>
#include <libloaderapi.h>

char *files_get_resources_dir()
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
