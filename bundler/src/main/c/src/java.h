#pragma once

#include <jni.h>

#include "util/dyn.h"

typedef jint(JNICALL *GetDefaultJavaVMInitArgs)(JavaVMInitArgs *args);
typedef jint(JNICALL *CreateJavaVM)(JavaVM **pvm, JNIEnv **penv, JavaVMInitArgs *args);

int java_start_vm(int argc, char *argv[], DYNHandle handle);
