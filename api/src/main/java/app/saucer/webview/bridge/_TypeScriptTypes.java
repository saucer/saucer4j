package app.saucer.webview.bridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptValue;
import co.casterlabs.rakurai.json.DefaultJsonSerializer;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonBoolean;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonNumber;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;

class _TypeScriptTypes {

    static String getType(Field field) {
        Class<?> typeToDocument = field.getType();
        Type genericType = field.getGenericType();

        if (field.isAnnotationPresent(JavascriptValue.class)) {
            JavascriptValue annotation = field.getAnnotation(JavascriptValue.class);
            if (annotation.typeToDocument() != void.class) {
                typeToDocument = annotation.typeToDocument();
                genericType = null;
            }
        }

        return _TypeScriptTypes.getTypeName(
            field.getDeclaringClass().getClassLoader(),
            typeToDocument,
            genericType
        );
    }

    static String getReturnType(Method method) {
        return _TypeScriptTypes.getTypeName(
            method.getDeclaringClass().getClassLoader(),
            method.getReturnType(),
            method.getGenericReturnType()
        );
    }

    static String getParameterType(Method method, int parameterIndex) {
        Class<?> clazz = method.getParameters()[parameterIndex].getType();
        Type genericType = method.getParameters()[parameterIndex].getParameterizedType();

        return _TypeScriptTypes.getTypeName(
            method.getDeclaringClass().getClassLoader(),
            clazz,
            genericType
        );
    }

    /**
     * @return The TypeScript type name corresponding to the given Java class.
     */
    private static String getTypeName(ClassLoader loader, Class<?> clazz, @Nullable Type generic) {
        // @formatter:off
        if (clazz == boolean.class) return "boolean";
        if (clazz == int.class)     return "number";
        if (clazz == byte.class)    return "number";
        if (clazz == char.class)    return "number";
        if (clazz == short.class)   return "number";
        if (clazz == long.class)    return "number";
        if (clazz == float.class)   return "number";
        if (clazz == double.class)  return "number";
        if (clazz == void.class)    return "void";

        if (clazz == Boolean.class) return "boolean";
        if (clazz == Void.class)    return "void";
        if (clazz == Object.class)  return "any";

        if (clazz == JsonNull.class)    return "null";
        if (clazz == JsonArray.class)   return "any[]";
        if (clazz == JsonObject.class)  return "Record<string, any>";
        if (clazz == JsonString.class)  return "string";
        if (clazz == JsonNumber.class)  return "number";
        if (clazz == JsonBoolean.class) return "boolean";
        if (clazz == JsonElement.class) return "any";
        // @formatter:on

        if (CharSequence.class.isAssignableFrom(clazz)) {
            return "string";
        }

        if (Number.class.isAssignableFrom(clazz)) {
            return "number";
        }

        if (clazz.isArray()) {
            return String.format(
                "%s[]",
                getTypeName(loader, clazz.getComponentType(), null)
            );
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            if (generic != null && generic instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) generic;
                Type[] typeArguments = pt.getActualTypeArguments();

                return String.format(
                    "%s[]",
                    typeToName(typeArguments[0], loader)
                );
            } else {
                return "any[]";
            }
        }

        if (Map.class.isAssignableFrom(clazz)) {
            if (generic != null && generic instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) generic;
                Type[] typeArguments = pt.getActualTypeArguments();

                return String.format(
                    "Record<%s, %s>",
                    typeToName(typeArguments[0], loader),
                    typeToName(typeArguments[1], loader)
                );
            } else {
                return "Record<any, any>";
            }
        }

        if (Enum.class.isAssignableFrom(clazz)) {
            Enum<?>[] enumConstants = (Enum<?>[]) clazz.getEnumConstants();
            String[] enumNames = new String[enumConstants.length];
            for (int i = 0; i < enumConstants.length; i++) {
                Enum<?> constant = enumConstants[i];
                enumNames[i] = constant.name();
            }
            return '"' + String.join("\" | \"", enumNames) + '"';
        }

        if (clazz.isAnnotationPresent(JsonClass.class)) {
            JsonClass annotation = clazz.getAnnotation(JsonClass.class);
            if (annotation.serializer() != DefaultJsonSerializer.class) {
                // Custom serializer detected, we can't document this properly.
                return clazz.getSimpleName();
            }
        }

        Map<String, String> allFields = new HashMap<>();
        allFields.putAll(getDeclaredFields(clazz));
        allFields.putAll(getDeclaredSerializationMethods(clazz));

        List<String> toJoin = new LinkedList<>();
        toJoin.add("{");
        for (Map.Entry<String, String> e : allFields.entrySet()) {
            String fieldName = e.getKey();
            String fieldType = e.getValue();

            toJoin.add(String.format("%s: %s;", fieldName, fieldType));
        }
        toJoin.add("}");

        return String.join(" ", toJoin);
    }

    private static String typeToName(Type type, ClassLoader classLoader) {
        if (type instanceof Class) {
            return getTypeName(classLoader, (Class<?>) type, null); // Sometimes Java actually gives us a Class<?>!
        }

        try {
            Class<?> clazz = Class.forName(type.getTypeName(), false, classLoader);
            return getTypeName(classLoader, clazz, null);
        } catch (ClassNotFoundException e) {
            return "any";
        }
    }

    /* -------------------- */
    /* Method lookup        */
    /* -------------------- */

    static Map<String, String> getDeclaredSerializationMethods(Class<?> clazz) {
        Map<String, String> methods = new HashMap<>();
        getAllDeclaredMethods0(methods, clazz);
        return methods;
    }

    static void getAllDeclaredMethods0(Map<String, String> methods, Class<?> clazz) {
        Method[] declared = clazz.getDeclaredMethods();
        for (Method m : declared) {
//            if (m.isAnnotationPresent(JsonSerializationMethod.class)) {
//                JsonSerializationMethod annotation = m.getAnnotation(JsonSerializationMethod.class);
//                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();
//                methods.put(name, getReturnType(m));
//            }
            if (m.isAnnotationPresent(JsonDeserializationMethod.class)) {
                JsonDeserializationMethod annotation = m.getAnnotation(JsonDeserializationMethod.class);
                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();
                methods.put(name, getParameterType(m, 0));
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            getAllDeclaredMethods0(methods, superClass);
        }
    }

    /* -------------------- */
    /* Fields               */
    /* -------------------- */

    static Map<String, String> getDeclaredFields(Class<?> type) {
        Map<String, String> fields = new HashMap<>();
        List<Class<?>> toScan = new LinkedList<>();

        Class<?> currentClass = type;
        while (currentClass != null) {
            boolean exposeSuper = false;

            if (currentClass.isAnnotationPresent(JsonClass.class)) {
                JsonClass classAnnotation = currentClass.getAnnotation(JsonClass.class);

                exposeSuper = classAnnotation.exposeSuper();
            }

            if (exposeSuper) {
                toScan.addAll(Arrays.asList(currentClass.getInterfaces()));

                if (currentClass.getSuperclass() != null) {
                    toScan.add(currentClass.getSuperclass());
                }
            }

            toScan.add(currentClass);

            currentClass = currentClass.getSuperclass();
        }

        for (Class<?> clazz : toScan) {
            boolean exposeAll = false;

            if (clazz.isAnnotationPresent(JsonClass.class)) {
                JsonClass classAnnotation = clazz.getAnnotation(JsonClass.class);

                exposeAll = classAnnotation.exposeAll();
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(JsonExclude.class)) {
                    if (exposeAll || field.isAnnotationPresent(JsonField.class)) {
                        String fieldName = field.getName();

                        if (field.isAnnotationPresent(JsonField.class)) {
                            JsonField fieldAnnotation = field.getAnnotation(JsonField.class);

                            if (!fieldAnnotation.value().isEmpty()) {
                                fieldName = fieldAnnotation.value();
                            }
                        }

                        fields.put(fieldName, getType(field));
                    }
                }
            }
        }

        return fields;
    }

}
