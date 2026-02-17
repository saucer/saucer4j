#include "bundle.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "cJSON/cJSON.h"
#include "util/files.h"

struct BundleInfo *bundle_parse(char *path)
{
    struct BundleInfo *result = malloc(sizeof(struct BundleInfo));

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

            options[i].optionString = vm_arg_json->valuestring;
        }

        result->optv = options;
    }

    return result;
}
