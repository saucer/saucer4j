#include "java.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#include <jni.h>

#include "bundle.h"
#include "util/dyn.h"

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

    jobjectArray args;
    if (argc > 1)
    {
        // Start at IDX=1 so that Java doesn't receive the executable command.
        int argcMinusExec = argc - 1;
        args = (*env)->NewObjectArray(env, argcMinusExec, (*env)->FindClass(env, "java/lang/String"), NULL);
        for (jint i = 0; i < argcMinusExec; i++)
        {
            jstring arg = (*env)->NewStringUTF(env, argv[i + 1]);
            (*env)->SetObjectArrayElement(env, args, i, arg);
        }
    }
    else
        args = (*env)->NewObjectArray(env, 0, (*env)->FindClass(env, "java/lang/String"), NULL);

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

    call_java_main(tenv);

    jint destroy_code = (*jvm)->DestroyJavaVM(jvm);
    if (destroy_code != JNI_OK)
    {
        printf("Got %d while destroying VM\n", (int)destroy_code);
        return destroy_code;
    }

    return 0;
}
