#include "platform.h"

#include "util/strings.h"

DYNHandle platform_load_jvm(char *cwd)
{
    DYNHandle handle = dyn_load(strings_concat(cwd, "/runtime/lib/libjli.dylib"));
    if (handle == NULL)
        handle = dyn_load(strings_concat(cwd, "/runtime/lib/jli/libjli.dylib"));
    return handle;
}
