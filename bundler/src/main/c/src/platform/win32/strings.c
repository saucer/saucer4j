#include "platform/win32/strings.h"

#include <stdlib.h>

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
    size_t nlen = wcstombs(NULL, str, 0);           // get the size of the narrow string
    CHAR *nstr = malloc((nlen + 1) * sizeof(CHAR)); // +1 for the null-terminator
    if (nstr == NULL)
        return NULL;

    wcstombs(nstr, str, nlen);
    nstr[nlen] = '\0'; // Null-terminate.

    return nstr;
}
