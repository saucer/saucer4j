package app.saucer._impl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptGetter;
import app.saucer.bridge.JavascriptSetter;
import app.saucer.bridge.JavascriptValue;
import app.saucer.bridge.Mutable;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

class JavascriptObjectWrapper {
    final String id = UUID.randomUUID().toString();
    final String path;
    private Object obj;

    private Map<String, Method> getters = new HashMap<>();
    private Map<String, Method> setters = new HashMap<>();
    private Map<String, Method> functions = new HashMap<>();
    private Map<String, Field> gettableValues = new HashMap<>();
    private Map<String, Field> settableValues = new HashMap<>();

    private List<MutationField> mutableFields = new ArrayList<>();

    public JavascriptObjectWrapper(String path, Object obj) {
        this.path = path;
        this.obj = obj;

        for (Method m : getAllMethods(obj, obj.getClass())) {
            m.setAccessible(true);

            if (m.isAnnotationPresent(JavascriptGetter.class)) {
                JavascriptGetter annotation = m.getDeclaredAnnotation(JavascriptGetter.class);
                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();

                this.getters.put(name, m);
            }
            if (m.isAnnotationPresent(JavascriptSetter.class)) {
                JavascriptSetter annotation = m.getDeclaredAnnotation(JavascriptSetter.class);
                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();

                this.setters.put(name, m);
            }
            if (m.isAnnotationPresent(JavascriptFunction.class)) {
                JavascriptFunction annotation = m.getDeclaredAnnotation(JavascriptFunction.class);
                String name = annotation.value().isEmpty() ? m.getName() : annotation.value();

                this.functions.put(name, m);
            }
        }
        for (Field f : getAllFields(obj, obj.getClass())) {
            f.setAccessible(true);

            if (f.isAnnotationPresent(JavascriptValue.class)) {
                JavascriptValue annotation = f.getDeclaredAnnotation(JavascriptValue.class);
                String name = annotation.value().isEmpty() ? f.getName() : annotation.value();

                if (annotation.allowGet()) {
                    this.gettableValues.put(name, f);
                }
                if (annotation.allowSet()) {
                    this.settableValues.put(name, f);
                }
                if (annotation.watchForMutate()) {
                    this.mutableFields.add(new MutationField(name, f));
                }
            }
        }
    }

    public List<String> properties() {
        List<String> properties = new ArrayList<>(this.getters.size() + this.setters.size() + this.gettableValues.size() + this.settableValues.size());
        properties.addAll(this.getters.keySet());
        properties.addAll(this.setters.keySet());
        properties.addAll(this.gettableValues.keySet());
        properties.addAll(this.settableValues.keySet());
        return properties;
    }

    public List<String> functions() {
        return new ArrayList<>(this.functions.keySet());
    }

    @SneakyThrows
    public @Nullable JsonElement handleGet(String field) {
        Method getter = this.getters.get(field);
        if (getter != null) {
            return Rson.DEFAULT.toJson(getter.invoke(this.obj));
        }

        Field value = this.gettableValues.get(field);
        if (value != null) {
            Object toReturn = value.get(this.obj);

            if (toReturn instanceof Mutable) {
                toReturn = ((Mutable<?>) toReturn).get();
            }

            return Rson.DEFAULT.toJson(toReturn);
        }

        return null; // undefined.
    }

    @SneakyThrows
    public <T> void handleSet(String field, JsonElement newValue) {
        Method setter = this.setters.get(field);
        if (setter != null) {
            Object o = null;

            if (!newValue.isJsonNull()) {
                Class<?> type = setter.getParameterTypes()[0];
                o = Rson.DEFAULT.fromJson(newValue, type);
            }

            setter.invoke(this.obj, o);
            return;
        }

        Field value = this.settableValues.get(field);
        if (value != null) {
            Class<?> type = value.getType();

            if (type == Mutable.class) {
                @SuppressWarnings("unchecked")
                Mutable<T> mut = (Mutable<T>) value.get(this.obj);

                T o = newValue.isJsonNull() ? null : Rson.DEFAULT.fromJson(newValue, mut.type);
                mut.set(o);
            } else {
                Object o = newValue.isJsonNull() ? null : Rson.DEFAULT.fromJson(newValue, type);
                value.set(this.obj, o);
            }
            return;
        }

        throw new IllegalArgumentException("Cannot set field: " + field + ", did you mistype something or forget an annotation?");
    }

    @SneakyThrows
    public @Nullable JsonElement handleInvoke(String functionName, JsonArray parameters) {
        Method function = this.functions.get(functionName);
        if (function == null) {
            throw new IllegalArgumentException("Nonexistient function: " + functionName + ", did you mistype something or forget an annotation?");
        }

        Class<?>[] argTypes = function.getParameterTypes();
        assert argTypes.length == parameters.size() : "The invoking arguments do not match the expected length: " + argTypes.length;

        Object[] args = new Object[argTypes.length];

        for (int i = 0; i < args.length; i++) {
            try {
                args[i] = Rson.DEFAULT.fromJson(parameters.get(i), argTypes[i]);
            } catch (JsonParseException e) {
                throw new IllegalArgumentException("The provided argument " + parameters.get(i) + " could not be converted to " + argTypes[i].getCanonicalName());
            }
        }

        if (function.getReturnType() == Void.class) {
            function.invoke(this.obj, args);
            return null; // undefined (aka, void)
        } else {
            Object result = function.invoke(this.obj, args);
            return Rson.DEFAULT.toJson(result);
        }
    }

    public List<String> whichFieldsHaveMutated() {
        return this.mutableFields.stream()
            .filter((m) -> m.check())
            .map((m) -> m.name)
            .collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    private class MutationField {
        private final String name;
        private final Field f;

        private int lastHashCode = 0;
        private boolean isFirstCheck = true;

        @SneakyThrows
        private boolean check() {
            int currentHashCode = hash(this.f.get(obj));
            boolean has = this.isFirstCheck || this.lastHashCode != currentHashCode;

            this.isFirstCheck = false;
            this.lastHashCode = currentHashCode;

            return has;
        }

    }

    /**
     * This recurses through the inheritance to look for private fields.
     * 
     * @return
     */
    public static List<Field> getAllFields(Object obj, Class<?> clazz) {
        if (clazz == null) return Collections.emptyList();

        List<Field> fields = new LinkedList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        fields.addAll(getAllFields(obj, clazz.getSuperclass()));
        return fields;
    }

    /**
     * This recurses through the inheritance to look for private fields.
     * 
     * @return
     */
    public static List<Method> getAllMethods(Object obj, Class<?> clazz) {
        if (clazz == null) return Collections.emptyList();

        List<Method> methods = new LinkedList<>();
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        methods.addAll(getAllMethods(obj, clazz.getSuperclass()));
        return methods;
    }

    private static int hash(Object o) {
        try {
            if (o instanceof Map<?, ?>) {
                // We need to hash all of the keys and values, recursively.
                Map<?, ?> m = (Map<?, ?>) o;

                int result = 1;
                for (Map.Entry<?, ?> entry : m.entrySet().toArray(new Map.Entry[0])) {
                    result = 31 * result + (entry.getKey() == null ? 0 : hash(entry.getKey()));
                    result = 31 * result + (entry.getValue() == null ? 0 : hash(entry.getValue()));
                }
                return result;
            } else if (o instanceof Collection<?>) {
                // We need to hash all of the elements, recursively.
                Collection<?> c = (Collection<?>) o;

                int result = 1;
                for (Object element : c.toArray()) {
                    result = 31 * result + (element == null ? 0 : hash(element));
                }
                return result;
            } else if (o.getClass().isArray()) {
                // We need to hash all of the elements, recursively.
                final int length = Array.getLength(o);

                int result = 1;
                for (int idx = 0; idx < length; idx++) {
                    Object element = Array.get(o, idx);
                    result = 31 * result + (element == null ? 0 : hash(element));
                }
                return result;
            }
        } catch (StackOverflowError e) {
            // There's probably a circular reference in the maps/collections. We'll ignore
            // them and return the normal hashCode (fall through).
        }

        return Objects.hashCode(o);
    }

}
