package com.zolstein.compacthashmap;

import java.util.Map;
import java.util.Objects;

class CompactMapEntry<K, V> implements Map.Entry<K, V> {

  private K[] sourceKeys;
  private V[] sourceValues;
  private int index;

  CompactMapEntry(CompactHashMap<K, V> source, int index) {
    this.sourceKeys = source.keys;
    this.sourceValues = source.values;
    this.index = index;
  }

  @Override
  public K getKey() {
    return sourceKeys[index];
  }

  @Override
  public V getValue() {
    return sourceValues[index];
  }
  @Override
  public V setValue(V value) {
    V ret = getValue();
    sourceValues[index] = value;
    return ret;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Map.Entry)) {
      return false;
    }
    Map.Entry<?, ?> other = (Map.Entry) o;
    return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
  }
}
