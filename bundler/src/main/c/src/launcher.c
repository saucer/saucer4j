#include <stdio.h>

#include "platform.h"
#include "util/files.h"
#include "java.h"

int launcher_main(int argc, char *argv[])
{
    char *cwd = files_get_resources_dir();
    if (cwd == NULL)
    {
        printf("Failed to get executable directory\n");
        return 1;
    }
    files_change_cwd(cwd);

    if (!platform_init(cwd))
        return 1;

    DYNHandle handle = platform_load_jvm(cwd);
    if (handle == NULL)
    {
        printf("Failed to load the JVM\n");
        return 1;
    }

    return java_start_vm(argc, argv, handle);
}