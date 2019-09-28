package com.zolstein.compacthashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class WrapperKeySet<K> implements Set<K> {

  private CompactHashMap<K, ?> backingMap;

  WrapperKeySet(CompactHashMap<K, ?> map) {
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
    return backingMap.containsKey(o);
  }

  @Override
  public Iterator<K> iterator() {
    return new TransformedIterator<>(backingMap.entrySet().iterator(), Map.Entry::getKey);
  }

  @Override
  public Object[] toArray() {
    Object[] array = new Object[backingMap.size()];
    Iterator<K> iter = this.iterator();
    for (int i = 0; iter.hasNext(); i++) {
      array[i] = iter.next();
    }
    return array;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    Object[] array = new Object[backingMap.size()];
    Iterator<K> iter = this.iterator();
    for (int i = 0; iter.hasNext(); i++) {
      array[i] = (T) iter.next();
    }
    return (T[]) array;
  }

  @Override
  public boolean add(K k) {
    throw new UnsupportedOperationException("Map.keySet() does not support add");
  }

  @Override
  public boolean remove(Object o) {
    boolean contains = backingMap.containsKey(o);
    if (contains) {
      backingMap.remove(o);
    }
    return contains;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return c.stream().allMatch(backingMap::containsKey);
  }

  @Override
  public boolean addAll(Collection<? extends K> c) {
    throw new UnsupportedOperationException("Map.keySet() does not support addAll");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    Set<?> cSet = new HashSet<>(c);
    Iterator<K> iter = iterator();
    boolean ret = false;
    while (iter.hasNext()) {
      K elem = iter.next();
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
    for (K elem : this) {
      ret += elem.hashCode();
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
