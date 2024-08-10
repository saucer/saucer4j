package co.casterlabs.saucer._impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.saucer.bridge.JavascriptFunction;
import co.casterlabs.saucer.bridge.JavascriptGetter;
import co.casterlabs.saucer.bridge.JavascriptSetter;
import co.casterlabs.saucer.bridge.JavascriptValue;
import co.casterlabs.saucer.bridge.Mutable;
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

        for (Method m : this.obj.getClass().getMethods()) {
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
        for (Field f : this.obj.getClass().getFields()) {
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
            int currentHashCode = Objects.hashCode(this.f.get(obj));
            boolean has = this.isFirstCheck || this.lastHashCode != currentHashCode;

            this.isFirstCheck = false;
            this.lastHashCode = currentHashCode;

            return has;
        }

    }

}
