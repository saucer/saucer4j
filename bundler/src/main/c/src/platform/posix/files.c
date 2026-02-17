#include "util/files.h"

#include <unistd.h>

bool files_change_cwd(char *new_path)
{
    return chdir(new_path) == 0;
}
