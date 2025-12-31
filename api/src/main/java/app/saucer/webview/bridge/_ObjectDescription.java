package app.saucer.webview.bridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class _ObjectDescription {
    final String path;
    final List<_ObjectDescription> subObjects = new ArrayList<>();
    final Map<String, _MethodDescription> methods = new HashMap<>();
    final Map<String, _PropertyDescription> properties = new HashMap<>();

    _ObjectDescription(String accessor) {
        this.path = accessor;
    }

    static class _MethodDescription {
        final String returnType;
        final List<_ParameterDescription> parameterTypes = new ArrayList<>();

        _MethodDescription(Method method, boolean noReturn) {
            this.returnType = noReturn ? "void" : getTypeName(method.getReturnType());

            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> paramType = method.getParameterTypes()[i];
                String paramName = "arg" + i;
                this.parameterTypes.add(new _ParameterDescription(paramName, paramType));
            }
        }
    }

    static class _ParameterDescription {
        final String name;
        final String type;

        _ParameterDescription(String name, Class<?> type) {
            this.name = name;
            this.type = getTypeName(type);
        }
    }

    static class _PropertyDescription {
        final String type;
        boolean readable;
        boolean writable;
        boolean watchable;

        _PropertyDescription(Class<?> type) {
            this.type = getTypeName(type);
        }
    }

    String generateTypescriptDefinition() {
        List<String> lines = new ArrayList<>();

        for (_ObjectDescription subObject : this.subObjects) {
            lines.add(subObject.generateTypescriptDefinition());
            lines.add("");
        }

        String mutationType = "never";
        {
            List<String> mutableProperties = new ArrayList<>();
            for (Map.Entry<String, _PropertyDescription> e : this.properties.entrySet()) {
                String propertyName = e.getKey();
                _PropertyDescription property = e.getValue();

                if (property.watchable) {
                    mutableProperties.add('"' + propertyName + '"');
                }
            }

            if (!mutableProperties.isEmpty()) {
                mutationType = String.join(" | ", mutableProperties);
            }
        }

        lines.add(String.format("export declare interface %s extends MutationObject<%s> {", this.path.replace('.', '_'), mutationType));
        lines.add("");

        for (_ObjectDescription subObject : subObjects) {
            String subObjectName = subObject.path.substring(subObject.path.lastIndexOf('.') + 1);
            lines.add(String.format("    readonly %s: %s;", subObjectName, subObject.path.replace('.', '_')));
        }

        for (Map.Entry<String, _PropertyDescription> e : this.properties.entrySet()) {
            String propertyName = e.getKey();
            _PropertyDescription property = e.getValue();

            String line = "    ";
            if (property.readable && !property.writable) {
                line += "readonly ";
                lines.add(String.format("    readonly %s: Promise<%s>;", propertyName, property.type));
                continue;
            }

            if (!property.readable && property.writable) {
                lines.add("    /** write-only */");
                lines.add(String.format("    %s: %s;", propertyName, property.type));
                continue;
            }

            line += String.format("%s: Promise<%s> | %s;", propertyName, property.type, property.type);
            lines.add(line);
        }

        for (Map.Entry<String, _MethodDescription> e : this.methods.entrySet()) {
            String methodName = e.getKey();
            _MethodDescription method = e.getValue();

            String params = String.join(
                ", ",
                method.parameterTypes.stream()
                    .map((param) -> String.format("%s: %s", param.name, param.type))
                    .toArray(String[]::new)
            );
            lines.add(String.format("    %s(%s): Promise<%s>;", methodName, params, method.returnType));
        }
        lines.add("}");
        return String.join("\n", lines);
    }

    /**
     * @return The TypeScript type name corresponding to the given Java class.
     */
    static String getTypeName(Class<?> cls) {
        if (cls.isArray()) {
            return getTypeName(cls.getComponentType()) + "[]";
        }

        if (Collection.class.isAssignableFrom(cls)) {
            return "any[]";
        }

        if (Map.class.isAssignableFrom(cls)) {
            return "Record<any, any>";
        }

        if (cls.isPrimitive()) {
            if (cls == boolean.class) return "boolean";
            if (cls == int.class) return "number";
            if (cls == byte.class) return "number";
            if (cls == char.class) return "number";
            if (cls == short.class) return "number";
            if (cls == long.class) return "number";
            if (cls == float.class) return "number";
            if (cls == double.class) return "number";
            return "void"; // for void type
        }

        if (cls == String.class) {
            return "string";
        }

        return cls.getSimpleName();
    }

}
