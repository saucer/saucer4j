#pragma once

#include <stdbool.h>

/**
 * @attention This returns the Resources dir on macOS if in a bundle, otherwise it returns a normal value.
 */
char *files_get_executable_dir();

bool files_change_cwd(char *new_path);

char *files_contents(char *path);
