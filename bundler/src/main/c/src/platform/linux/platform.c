#include "platform.h"

#include "util/strings.h"

DYNHandle platform_load_jvm(char *cwd)
{
    DYNHandle handle = dyn_load(strings_concat(cwd, "/runtime/lib/server/libjvm.so"));
    if (handle == NULL)
        handle = dyn_load(strings_concat(cwd, "/runtime/lib/i386/server/libjvm.so"));
    if (handle == NULL)
        handle = dyn_load(strings_concat(cwd, "/runtime/lib/amd64/server/libjvm.so"));
    return handle;
}
