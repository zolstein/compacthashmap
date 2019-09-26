package com.zolstein.compacthashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

class WrapperValueCollection<V> implements Collection<V> {

    private CompactHashMap<?, V> backingMap;

    WrapperValueCollection(CompactHashMap<?, V> map) {
        this.backingMap = map;
    }

    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return backingMap.entrySet().stream().map(Map.Entry::getValue).anyMatch(v -> Objects.equals(v, o));
    }

    @Override
    public Iterator<V> iterator() {
        return new TransformedIterator<>(backingMap.entrySet().iterator(), Map.Entry::getValue);
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[backingMap.size()];
        Iterator<V> iter = this.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            array[i] = iter.next();
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Object[] array = new Object[backingMap.size()];
        Iterator<V> iter = this.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            array[i] = (T) iter.next();
        }
        return (T[]) array;
    }

    @Override
    public boolean add(V v) {
        throw new UnsupportedOperationException("Map.values() does not support add");
    }

    @Override
    public boolean remove(Object o) {
        Iterator<V> iter = iterator();
        while(iter.hasNext()) {
            V value = iter.next();
            if (Objects.equals(o, value)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        HashSet<V> values = new HashSet<>();
        iterator().forEachRemaining(values::add);
        return values.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException("Map.values() does not support add");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        HashSet<?> cSet = new HashSet<>(c);
        Iterator<V> iter = iterator();
        boolean result = false;
        while (iter.hasNext()) {
            V value = iter.next();
            if (cSet.contains(value)) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        HashSet<?> cSet = new HashSet<>(c);
        Iterator<V> iter = iterator();
        boolean result = false;
        while (iter.hasNext()) {
            V value = iter.next();
            if (!cSet.contains(value)) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }

    @Override
    public void clear() {
        backingMap.clear();
    }
}
