#include "util/dyn.h"

#include <stdio.h>
#include <windows.h>
#include <libloaderapi.h>
#include <fileapi.h>

#include "util/strings.h"
#include "platform/win32/strings.h"

bool dyn_add_dir(char *path)
{
    LPWSTR widened_path = strings_widen(path);
    if (AddDllDirectory(widened_path) == 0)
    {
        printf("Failed to add DLL directory '%s': %lu.\n", path, GetLastError());
        return false;
    }

    // We have to preload all of the DLLs to avoid issues with AWT not being able to find them later.
    char *pattern = strings_concat(path, "\\*.dll");

    WIN32_FIND_DATAA finder_data;
    HANDLE finder = FindFirstFileA(pattern, &finder_data);
    if (finder == INVALID_HANDLE_VALUE)
    {
        printf("Failed to start search for: %s\n", pattern);
        return false;
    }

    do
    {
        char *partial_path = finder_data.cFileName;
        char *full_path = strings_concat(path, strings_concat("\\", partial_path));

        if (LoadLibraryExA(full_path, NULL, LOAD_LIBRARY_SEARCH_DEFAULT_DIRS) == NULL &&
            LoadLibraryExA(partial_path, NULL, LOAD_LIBRARY_SEARCH_DEFAULT_DIRS) == NULL)
        {
            printf("Failed to load library '%s': %lu, ignoring.\n", full_path, GetLastError());
        }
    } while (FindNextFileA(finder, &finder_data));

    FindClose(finder);
    return true;
}

DYNHandle dyn_load(char *path)
{
    return LoadLibraryExA(path, NULL, LOAD_LIBRARY_SEARCH_DEFAULT_DIRS);
}

DYNSymbol dyn_symbol(DYNHandle handle, char *name)
{
    return GetProcAddress((HMODULE)handle, name);
}
