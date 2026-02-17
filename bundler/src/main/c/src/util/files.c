#include "files.h"

#include <stdlib.h>
#include <stdio.h>

char *files_contents(char *path)
{
    FILE *file = fopen(path, "rb");
    if (file == NULL)
    {
        printf("Could not read %s, exiting.\n", path);
        return NULL;
    }

    fseek(file, 0L, SEEK_END);
    long int len = ftell(file);

    char *result = malloc(len + 1);

    rewind(file);
    size_t read = fread(result, 1, len, file);

    if (read < len)
    {
        printf("Error reading %s (%zd bytes vs %ld)\n", path, read, len);
        fclose(file);
        free(result);
        return NULL;
    }

    result[read] = '\0'; // Null-terminate the string
    fclose(file);

    return result;
}
