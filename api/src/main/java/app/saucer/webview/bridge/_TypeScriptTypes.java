package app.saucer.webview.bridge;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonBoolean;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonNumber;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;

class _TypeScriptTypes {

    /**
     * @return The TypeScript type name corresponding to the given Java class.
     */
    static String getTypeName(ClassLoader loader, Class<?> clazz, @Nullable Type generic) {
        if (clazz == boolean.class) return "boolean";
        if (clazz == int.class) return "number";
        if (clazz == byte.class) return "number";
        if (clazz == char.class) return "number";
        if (clazz == short.class) return "number";
        if (clazz == long.class) return "number";
        if (clazz == float.class) return "number";
        if (clazz == double.class) return "number";
        if (clazz == void.class) return "void";

        if (clazz == Boolean.class) return "boolean";
        if (clazz == Void.class) return "void";
        if (clazz == Object.class) return "any";

        if (clazz == JsonNull.class) return "null";
        if (clazz == JsonArray.class) return "any[]";
        if (clazz == JsonObject.class) return "Record<string, any>";
        if (clazz == JsonString.class) return "string";
        if (clazz == JsonNumber.class) return "number";
        if (clazz == JsonBoolean.class) return "boolean";
        if (clazz == JsonElement.class) return "any";

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

        return clazz.getSimpleName();
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

}
