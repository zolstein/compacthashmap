package com.zolstein.compacthashmap;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

class WrapperEntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {

    private CompactHashMap<K, V> backingMap;
    private boolean canRemove;
    private int nextIndex;
    private long version;
    private int size;

    WrapperEntryIterator(CompactHashMap<K, V> map) {
        this.backingMap = map;
        this.version = map.version;
        this.canRemove = false;
        this.nextIndex = 0;
        this.size = map.size();
    }

    @Override
    public boolean hasNext() {
        return nextIndex < size;
    }

    @Override
    public Map.Entry<K, V> next() {
        if (version != backingMap.version) {
            throw new ConcurrentModificationException();
        }
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Map.Entry<K, V> ret = backingMap.entries.get(nextIndex++);
        canRemove = true;
        return ret;
    }

    @Override
    public void remove() {
        if (!canRemove) {
            throw new IllegalStateException();
        }
        if (version != backingMap.version) {
            throw new ConcurrentModificationException();
        }
        backingMap.removeAtIndex(--nextIndex);
        version = backingMap.version; // increment?
        size = backingMap.size(); // decrement?
        canRemove = false;
    }
}
