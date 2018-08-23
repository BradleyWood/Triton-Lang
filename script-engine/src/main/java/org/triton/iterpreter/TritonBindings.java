package org.triton.iterpreter;

import org.jetbrains.annotations.NotNull;

import javax.script.Bindings;
import java.util.*;

public class TritonBindings implements Bindings {

    private final Map<String, Object> attributes;

    public TritonBindings(@NotNull final Map<String, Object> bindings) {
        this.attributes = bindings;
    }

    public TritonBindings() {
        this(Collections.synchronizedMap(new HashMap<>()));
    }

    @Override
    public Object put(final String name, final Object value) {
        return attributes.put(name, value);
    }

    @Override
    public void putAll(final Map<? extends String, ?> toMerge) {
        attributes.putAll(toMerge);
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return attributes.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return attributes.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return attributes.entrySet();
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return attributes.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return attributes.containsValue(value);
    }

    @Override
    public Object get(final Object key) {
        return attributes.get(key);
    }

    @Override
    public Object remove(final Object key) {
        return attributes.remove(key);
    }
}
