#include "platform.h"

bool platform_init(char *cwd)
{
    return true; // No special initialization needed on POSIX platforms.
}

int main(int argc, char *argv[])
{
    return launcher_main(argc, argv);
}
