package com.zolstein.compacthashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class WrapperEntrySet<K, V> implements Set<Map.Entry<K, V>> {

    private CompactHashMap<K, V> backingMap;

    WrapperEntrySet(CompactHashMap<K, V> map) {
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
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        Map.Entry entry = (Map.Entry) o;
        Map.Entry<K, V> inSet = backingMap.getEntry(entry.getKey());
        return Objects.equals(inSet, entry);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new WrapperEntryIterator<>(backingMap);
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[backingMap.size()];
        Iterator<CompactHashMap.Entry<K, V>> iter = this.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            array[i] = iter.next();
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Object[] array = new Object[backingMap.size()];
        Iterator<CompactHashMap.Entry<K, V>> iter = this.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            array[i] = (T) iter.next();
        }
        return (T[]) array;
    }

    @Override
    public boolean add(Map.Entry<K, V> kvEntry) {
        throw new UnsupportedOperationException("Map.entrySet() does not support add");
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        Map.Entry entry = (Map.Entry) o;
        Map.Entry<K, V> inSet = backingMap.getEntry(entry.getKey());
        if (Objects.equals(entry, inSet)) {
            backingMap.remove(entry.getKey());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
        throw new UnsupportedOperationException("Map.entrySet() does not support add");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<?> cSet = new HashSet<>(c);
        Iterator<Map.Entry<K, V>> iter = iterator();
        boolean ret = false;
        while(iter.hasNext()) {
            Map.Entry<K, V> elem = iter.next();
            if (!cSet.contains(elem)) {
                iter.remove();
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().map(this::remove).reduce(false, Boolean::logicalOr);
    }

    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public int hashCode() {
        int ret = 0;
        for (Map.Entry<K, V> entry: this) {
            ret += entry.hashCode();
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Set)) {
            return false;
        }
        Set<?> other = (Set) o;
        return size() == other.size() && containsAll(other);
    }
}
