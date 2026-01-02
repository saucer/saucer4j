package app.saucer.webview.bridge;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

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
            if (noReturn) {
                this.returnType = "void";
            } else {
                this.returnType = _TypeScriptTypes.getTypeName(
                    method.getDeclaringClass().getClassLoader(),
                    method.getReturnType(),
                    method.getGenericReturnType()
                );
            }

            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> paramType = method.getParameterTypes()[i];
                Type genericParamType = method.getGenericParameterTypes()[i];
                String paramName = method.getParameters()[0].getName();
                this.parameterTypes.add(new _ParameterDescription(paramName, paramType, genericParamType));
            }
        }
    }

    static class _ParameterDescription {
        final String name;
        final String type;

        _ParameterDescription(String name, Class<?> clazz, @Nullable Type genericType) {
            this.name = name;
            this.type = _TypeScriptTypes.getTypeName(
                clazz.getClassLoader(),
                clazz,
                genericType
            );
        }
    }

    static class _PropertyDescription {
        final String type;
        boolean readable;
        boolean writable;
        boolean watchable;

        _PropertyDescription(Class<?> clazz, @Nullable Type genericType) {
            this.type = _TypeScriptTypes.getTypeName(
                clazz.getClassLoader(),
                clazz,
                genericType
            );
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

}
