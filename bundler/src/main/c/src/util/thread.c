// #include "thread.h"
// #include <stdio.h>

// #if defined(_WIN32)
// #include <windows.h>

// // Wrapper function to match the signature required by CreateThread
// DWORD WINAPI thread_wrapper(LPVOID param)
// {
//     void *(*start_routine)(void *) = ((void *(**)(void *))param)[0];
//     ThreadEnv *arg = ((ThreadEnv **)param)[1];
//     start_routine(arg);
//     free(param);
//     return 0;
// }

// void thread_start(void *(*tfn)(void *), ThreadEnv *tenv)
// {
//     void **params = malloc(2 * sizeof(void *));
//     if (params == NULL)
//         return; // Allocation failed

//     params[0] = (void *)tfn;
//     params[1] = tenv;

//     CreateThread(NULL, 0, thread_wrapper, params, 0, NULL);
// }

// #else
// #include <pthread.h> // For pthread_create

// // POSIX
// void thread_start(void *(*tfn)(ThreadEnv *), ThreadEnv *tenv)
// {
//     pthread_t thread;
//     pthread_create(&thread, NULL, tfn, &tenv);
// }

// #endif