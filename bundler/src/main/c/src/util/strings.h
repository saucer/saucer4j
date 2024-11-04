#pragma once

char *strings_concat(const char *s1, const char *s2);

#if defined(_WIN32)
#include <windows.h>

LPWSTR strings_widen(LPSTR str);

LPSTR strings_narrow(LPWSTR str);

#endif
