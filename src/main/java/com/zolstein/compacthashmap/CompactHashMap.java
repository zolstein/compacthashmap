package com.zolstein.compacthashmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * HashMap implementation based on the dictionary implementation used by Python.
 * Should provide lower memory utilization and faster iteration than standard
 * HashMaps, as well as iteration over elements in insertion order if elements
 * have not been removed.
 *
 * Adapted from code available at http://code.activestate.com/recipes/578375/
 * Special thanks to Raymond Hettinger
 */
@SuppressWarnings("ALL")
public class CompactHashMap<K, V> implements Map<K, V> {

  private enum KeySize {
    BYTE,
    SHORT,
    INT
  }

  private int indexMapSize;
  private Object indexMap; // byte[] | short[] | int[]
  private KeySize indexKeySize;
  private int used;
  private int filled;
  ArrayList<CompactMapEntry<K, V>> entries;
  long version = 0;

  private static final int PERTURB_SHIFT = 5;
  private static final int FREE = -1;
  private static final int DUMMY = -2;
  private static final int BYTE_LIMIT = Byte.MAX_VALUE;
  private static final int SHORT_LIMIT = Short.MAX_VALUE;

  public CompactHashMap() {
    entries = new ArrayList<>();
    clear();
  }

  public CompactHashMap(Map<? extends K, ? extends V> source) {
    this();
    putAll(source);
  }

  private interface IntIterator {
    boolean hasNext();

    int next();
  }

  private static IntIterator genProbes(int hashValue, int mask) {
    return new IntIterator() {
      boolean hasRun = false;
      int i;
      int perturb;

      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public int next() {
        if (!hasRun) {
          int hash = hashValue;
          if (hash < 0) {
            hash = -hash;
          }
          perturb = hash;
          i = hash & mask;
          hasRun = true;
          return i;
        } else {
          i = (5 * i + perturb + 1);
          perturb = perturb >> PERTURB_SHIFT;
          return i & mask;
        }
      }
    };
  }

  private int[] lookup(Object key, int hashValue) {
    int indexLength = indexMapSize;
    assert filled < indexLength;
    int freeSlot = FREE;
    IntIterator probes = genProbes(hashValue, indexLength - 1);
    for (int i = probes.next(); ; i = probes.next()) {
      int index = getIndex(i);
      if (index == FREE) {
        return freeSlot == FREE ? new int[] {FREE, i} : new int[] {DUMMY, freeSlot};
      } else if (index == DUMMY) {
        if (freeSlot == FREE) {
          freeSlot = i;
        }
      } else {
        CompactMapEntry<K, V> entry = entries.get(index);
        if (entry.key == key || (entry.hash == hashValue && entry.key.equals(key))) {
          return new int[] {index, i};
        }
      }
    }
  }

  private int lookupForIndex(int desiredIndex, int hashValue) {
    int indexLength = indexMapSize;
    assert filled < indexLength;
    IntIterator probes = genProbes(hashValue, indexLength - 1);
    for (int i = probes.next(); ; i = probes.next()) {
      int index = getIndex(i);
      if (index == desiredIndex) {
        return i;
      } else if (index == FREE) {
        return FREE;
      }
    }
  }

  private Object makeIndex(int n) {
    indexMapSize = n;
    if (n <= BYTE_LIMIT) {
      indexKeySize = KeySize.BYTE;
      byte[] ret = new byte[n];
      Arrays.fill(ret, (byte) -1);
      return ret;
    } else if (n <= SHORT_LIMIT) {
      indexKeySize = KeySize.SHORT;
      short[] ret = new short[n];
      Arrays.fill(ret, (short) -1);
      return ret;
    } else {
      indexKeySize = KeySize.INT;
      int[] ret = new int[n];
      Arrays.fill(ret, -1);
      return ret;
    }
  }

  private int getIndex(int i) {
    switch (indexKeySize) {
      case BYTE:
        return ((byte[]) indexMap)[i];
      case SHORT:
        return ((short[]) indexMap)[i];
      case INT:
        return ((int[]) indexMap)[i];
    }
    throw new RuntimeException("Unreachable");
  }

  private void setIndex(int i, int v) {
    switch (indexKeySize) {
      case BYTE:
        ((byte[]) indexMap)[i] = (byte) v;
        break;
      case SHORT:
        ((short[]) indexMap)[i] = (short) v;
        break;
      case INT:
        ((int[]) indexMap)[i] = v;
        break;
    }
  }

  private void resize(int n) {
    n = Integer.highestOneBit(n) * 2; // Round up to next power of two
    indexMap = makeIndex(n);
    for (int index = 0; index < entries.size(); index++) {
      int hash = entries.get(index).hash;
      IntIterator probes = genProbes(hash, n - 1);
      int i = probes.next();
      while (getIndex(i) != FREE) {
        i = probes.next();
      }
      setIndex(i, index);
    }
    filled = used;
  }

  @Override
  public int size() {
    return used;
  }

  @Override
  public boolean isEmpty() {
    return used == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return lookup(key, key.hashCode())[0] >= 0;
  }

  @Override
  public boolean containsValue(Object value) {
    for (CompactMapEntry<K, V> e : entries) {
      if (Objects.equals(value, e.val)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {
    int hash = key.hashCode();
    int[] lookups = lookup(key, hash);
    int index = lookups[0];
    if (index < 0) {
      return null;
    }
    return entries.get(index).val;
  }

  Map.Entry<K, V> getEntry(Object key) {
    int hash = key.hashCode();
    int[] lookups = lookup(key, hash);
    int index = lookups[0];
    if (index < 0) {
      return null;
    }
    return entries.get(index);
  }

  @Override
  public V put(K key, V value) {
    int hash = key.hashCode();
    int[] lookups = lookup(key, hash);
    int index = lookups[0];
    int i = lookups[1];
    V old = null;
    if (index < 0) {
      setIndex(i, used++);
      CompactMapEntry<K, V> entry = new CompactMapEntry<>(hash, key, value);
      entries.add(entry);
      if (index == FREE) {
        int localFilled = ++filled;
        if (localFilled * 3 > (indexMapSize) * 2) {
          resize(4 * size());
        }
      }
      ++version;
    } else {
      CompactMapEntry<K, V> entry = entries.get(index);
      old = entry.val;
      entry.setValue(value);
    }

    return old;
  }

  @Override
  public V remove(Object key) {
    int hash = key.hashCode();
    int[] lookups = lookup(key, hash);
    int index = lookups[0];
    int i = lookups[1];
    if (index < 0) {
      return null;
    }
    return removeInternal(i, index);
  }

  void removeAtIndex(int index) {
    int hash = entries.get(index).hash;
    int i = lookupForIndex(index, hash);
    removeInternal(i, index);
  }

  private V removeInternal(int i, int index) {
    setIndex(i, DUMMY);
    int localUsed = --used;
    int lastIndex = entries.size() - 1;
    CompactMapEntry<K, V> lastEntry = entries.remove(lastIndex);
    if (index != localUsed) {
      int j = lookupForIndex(lastIndex, lastEntry.hash);
      assert lastIndex >= 0 && i != j;
      setIndex(j, index);
      lastEntry = entries.set(index, lastEntry);
    }
    ++version;
    return lastEntry.val;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    indexMap = makeIndex(8);
    entries.clear();
    used = 0;
    filled = 0;
  }

  @Override
  public Set<K> keySet() {
    return new WrapperKeySet<>(this);
  }

  @Override
  public Collection<V> values() {
    return new WrapperValueCollection<>(this);
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new WrapperEntrySet<>(this);
  }

  @Override
  public int hashCode() {
    int ret = 0;
    for (Entry<K, V> entry : entries) {
      ret += entry.hashCode();
    }
    return ret;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Map)) {
      return false;
    }
    Map<?, ?> other = (Map) o;
    return entrySet().equals(other.entrySet());
  }
}
