#include "strings.h"

#include <stdlib.h>
#include <string.h>

char *strings_concat(const char *s1, const char *s2)
{
    char *res = malloc((strlen(s1) + strlen(s2) + 1) * sizeof(char)); // +1 for the null-terminator
    if (res == NULL)
        return NULL;

    strcpy(res, s1);
    strcat(res, s2);
    return res;
}