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

#if defined(_WIN32)
#include <windows.h>

LPWSTR strings_widen(LPSTR str)
{
    size_t wlen = mbstowcs(NULL, str, 0);             // get the size of the wide string
    WCHAR *wstr = malloc((wlen + 1) * sizeof(WCHAR)); // +1 for the null-terminator
    if (wstr == NULL)
        return NULL;

    mbstowcs(wstr, str, wlen);
    wstr[wlen] = L'\0'; // Null-terminate.

    return wstr;
}

LPSTR strings_narrow(LPWSTR str)
{
    size_t len = wcstombs(NULL, str, 0);           // get the size of the narrow string
    CHAR *nstr = malloc((len + 1) * sizeof(CHAR)); // +1 for the null-terminator
    if (nstr == NULL)
        return NULL;

    wcstombs(nstr, str, len);
    nstr[len] = '\0'; // Null-terminate.

    return nstr;
}

#endif