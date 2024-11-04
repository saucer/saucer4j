#include <stdio.h>

#include "util/strings.h"
#include "util/files.h"
#include "util/dyn.h"
#include "java.h"

int launcher_main(int argc, char *argv[])
{
    char *cwd = files_get_executable_dir();
    if (cwd == NULL)
    {
        printf("Failed to get executable directory\n");
        return 1;
    }
    files_change_cwd(cwd);

#if defined(_WIN32)
    // Only required on Windows.
    if (!dyn_add_dir(strings_concat(cwd, "\\runtime\\bin\\server")))
        return 1;
    if (!dyn_add_dir(strings_concat(cwd, "\\runtime\\bin")))
        return 1;
#endif

    DYNHandle handle;

#if defined(_WIN32)
    handle = dyn_load(strings_concat(cwd, "\\runtime\\bin\\server\\jvm.dll"));
#elif defined(__APPLE__)
    handle = dyn_load(strings_concat(cwd, "/runtime/lib/libjli.dylib"));
    if (handle == NULL)
        handle = dyn_load(strings_concat(cwd, "/runtime/lib/jli/libjli.dylib"));
#else
    handle = dyn_load(strings_concat(cwd, "/runtime/lib/server/libjvm.so"));
    if (handle == NULL)
        handle = dyn_load(strings_concat(cwd, "/runtime/lib/i386/server/libjvm.so"));
    if (handle == NULL)
        handle = dyn_load(strings_concat(cwd, "/runtime/lib/amd64/server/libjvm.so"));
#endif

    if (handle == NULL)
    {
        printf("Failed to load the JVM\n");
        return 1;
    }

    return java_start_vm(argc, argv, handle);
}

#if defined(_WIN32)
#include <windows.h>

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpCmdLine, int nCmdShow)
{
    // Attach if we have it.
    boolean hasConsole = AttachConsole(ATTACH_PARENT_PROCESS);
    if (hasConsole)
    {
        freopen("CONIN$", "r", stdin);
        freopen("CONOUT$", "w", stdout);
        freopen("CONOUT$", "w", stderr);
    }

    // Parse out the command line arugments.
    int argc;
    LPWSTR *argvW = CommandLineToArgvW(GetCommandLineW(), &argc);
    if (argvW == NULL || argc < 1)
    {
        return launcher_main(0, NULL);
    }

    // Convert the WCHAR command line to CHAR.
    char **argv = malloc(argc * sizeof(char *));
    if (argv == NULL)
    {
        printf("Unable to allocate argv memory\n");
        return 1;
    }

    for (int idx = 0; idx < argc; ++idx)
    {
        argv[idx] = strings_narrow(argvW[idx]);
        if (argv[idx] == NULL)
        {
            printf("Unable to allocate arg[idx] memory\n");
            return 1;
        }
    }

    return launcher_main(argc, argv);
}

#else
int main(int argc, char *argv[])
{
    return launcher_main(argc, argv);
}
#endif