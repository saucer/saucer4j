#include "platform.h"

#include <stdio.h>
#include <stdlib.h>
#include <windows.h>

#include "util/strings.h"

bool platform_init(char *cwd)
{
    if (!dyn_add_dir(strings_concat(cwd, "\\runtime\\bin\\server")))
        return false;
    if (!dyn_add_dir(strings_concat(cwd, "\\runtime\\bin")))
        return false;
    return true;
}

DYNHandle platform_load_jvm(char *cwd)
{
    return dyn_load(strings_concat(cwd, "\\runtime\\bin\\server\\jvm.dll"));
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpCmdLine, int nCmdShow)
{
    // Attach to the parent console if available.
    if (AttachConsole(ATTACH_PARENT_PROCESS))
    {
        freopen("CONIN$", "r", stdin);
        freopen("CONOUT$", "w", stdout);
        freopen("CONOUT$", "w", stderr);
    }

    return launcher_main(__argc, __argv);
}
