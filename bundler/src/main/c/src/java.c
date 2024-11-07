#include "java.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#include <jni.h>
#include "cJSON/cJSON.h"

#include "util/dyn.h"
#include "util/files.h"
// #include "util/thread.h"

#if defined(__APPLE__)
// #include <CoreFoundation/CoreFoundation.h>
// We need to give the first thread to AWT... unless the user uses -XstartOnFirstThread.
#define DEFAULT_FIRST_THREAD_POLICY false

// void dummy_callback(void *info)
// {
// }
#else
#define DEFAULT_FIRST_THREAD_POLICY true
#endif

struct BundleInfo
{
    char *main;
    JavaVMOption *optv;
    int optc;
    bool start_on_first_thread;
};

struct BundleInfo *bundle_parse(char *path)
{
    struct BundleInfo *result = malloc(sizeof(struct BundleInfo));
    result->start_on_first_thread = DEFAULT_FIRST_THREAD_POLICY;

    char *bundle_raw = files_contents(path);
    if (bundle_raw == NULL)
    {
        free(result);
        return NULL; // We've already logged errors.
    }

    cJSON *bundle_json = cJSON_Parse(bundle_raw);

    {
        cJSON *main_class_path_json = cJSON_GetObjectItemCaseSensitive(bundle_json, "main");
        if (main_class_path_json == NULL || !cJSON_IsString(main_class_path_json))
        {
            printf("bundle.json is missing 'main' field with type string\n");
            return NULL;
        }

        char *main_class_path = main_class_path_json->valuestring;

        // Convert '.' to '/'
        size_t len = strlen(main_class_path);
        for (size_t i = 0; i < len; i++)
            if (main_class_path[i] == '.')
                main_class_path[i] = '/';

        result->main = main_class_path;
    }

    {
        cJSON *vm_args_json = cJSON_GetObjectItemCaseSensitive(bundle_json, "args");
        if (vm_args_json == NULL || !cJSON_IsArray(vm_args_json))
        {
            printf("bundle.json is missing 'args' field with type array\n");
            return NULL;
        }

        int vm_args_length = cJSON_GetArraySize(vm_args_json);
        result->optc = (jint)vm_args_length;

        JavaVMOption *options = (JavaVMOption *)malloc(sizeof(JavaVMOption) * vm_args_length);
        if (options == NULL)
        {
            printf("Failed to allocate memory for JVM options.\n");
            return NULL;
        }

        for (int i = 0; i < vm_args_length; i++)
        {
            cJSON *vm_arg_json = cJSON_GetArrayItem(vm_args_json, i);
            if (!cJSON_IsString(vm_arg_json))
            {
                printf("bundle.json 'args' field contains non-string elements.\n");
                return NULL;
            }

#if defined(__APPLE__)
            // Don't run this on other platforms, it does nothing.
            if (strcmp(vm_arg_json->valuestring, "-XstartOnFirstThread") == 0)
            {
                result->start_on_first_thread = true;
            }
#endif

            options[i].optionString = vm_arg_json->valuestring;
        }

        result->optv = options;
    }

    return result;
}

struct MainInfo
{
    JavaVM *jvm;
    struct BundleInfo *bundle_info;
    int argc;
    char **argv;
};

void *call_java_main(void *tenv)
{
    struct MainInfo *info = (struct MainInfo *)tenv;

    JavaVM *jvm = info->jvm;
    struct BundleInfo *bundle_info = info->bundle_info;
    int argc = info->argc;
    char **argv = info->argv;

    JNIEnv *env;

    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED)
    {
        (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
    }

    jclass main_class = (*env)->FindClass(env, bundle_info->main);
    if (main_class == NULL)
    {
        printf("Failed to find main class.\n");
        return NULL;
    }

    jmethodID main_method = (*env)->GetStaticMethodID(env, main_class, "main", "([Ljava/lang/String;)V");
    if (main_method == NULL)
    {
        printf("Failed to find main method.\n");
        return NULL;
    }

    jobjectArray args = (*env)->NewObjectArray(env, argc, (*env)->FindClass(env, "java/lang/String"), NULL);
    for (jint i = 0; i < argc; i++)
    {
        jstring arg = (*env)->NewStringUTF(env, argv[i]);
        (*env)->SetObjectArrayElement(env, args, i, arg);
    }

    (*env)->CallStaticVoidMethod(env, main_class, main_method, args);

    if ((*env)->ExceptionOccurred(env))
        (*env)->ExceptionDescribe(env);

    (*jvm)->DetachCurrentThread(jvm);

    return NULL;
}

int java_start_vm(int argc, char *argv[], DYNHandle handle)
{
    GetDefaultJavaVMInitArgs DYN_GetDefaultJavaVMInitArgs = dyn_symbol(handle, "JNI_GetDefaultJavaVMInitArgs");
    CreateJavaVM DYN_CreateJavaVM = dyn_symbol(handle, "JNI_CreateJavaVM");

    struct BundleInfo *bundle_info = bundle_parse("bundle.json");
    if (bundle_info == NULL)
        return 1; // We've already logged errors.

    JavaVMInitArgs vm_args;
    vm_args.version = JNI_VERSION_1_6;
    vm_args.options = bundle_info->optv;
    vm_args.nOptions = bundle_info->optc;
    vm_args.ignoreUnrecognized = JNI_TRUE;

    DYN_GetDefaultJavaVMInitArgs(&vm_args);

    JavaVM *jvm;
    JNIEnv *env;

    if (DYN_CreateJavaVM(&jvm, &env, &vm_args) != JNI_OK)
    {
        printf("Failed to create Java VM. Is one of your args bad?\n");
        return 1;
    }

    struct MainInfo *tenv = malloc(sizeof(struct MainInfo));
    tenv->jvm = jvm;
    tenv->bundle_info = bundle_info;
    tenv->argc = argc;
    tenv->argv = argv;

    // if (bundle_info->start_on_first_thread)
    // {
    call_java_main(tenv);
    // thread_start(call_java_main, tenv);
    //     }
    //     else
    //     {
    //         thread_start(call_java_main, tenv);
    // #if defined(__APPLE__)
    //         // We have this dummy to make sure that Java has adequate time to capture the first thread.
    //         CFRunLoopSourceContext ctx;
    //         ctx.version = 0;
    //         ctx.info = NULL;
    //         ctx.retain = NULL;
    //         ctx.release = NULL;
    //         ctx.copyDescription = NULL;
    //         ctx.equal = NULL;
    //         ctx.hash = NULL;
    //         ctx.schedule = NULL;
    //         ctx.cancel = NULL;
    //         ctx.perform = &dummy_callback;

    //         CFRunLoopSourceRef source = CFRunLoopSourceCreate(NULL, 0, &ctx);
    //         CFRunLoopAddSource(CFRunLoopGetCurrent(), source, kCFRunLoopCommonModes);
    //         CFRunLoopRun();
    // #endif
    //     }

    jint destroy_code = (*jvm)->DestroyJavaVM(jvm);
    if (destroy_code != JNI_OK)
    {
        printf("Got %d while destroying VM\n", (int)destroy_code);
        return destroy_code;
    }

    return 0;
}
