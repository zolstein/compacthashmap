package com.zolstein.compacthashmap;

import java.util.Map;
import java.util.Objects;

class CompactMapEntry<K, V> implements Map.Entry<K, V> {

  int hash;
  K key;
  V val;

  CompactMapEntry(int hash, K key, V val) {
    this.hash = hash;
    this.key = key;
    this.val = val;
  }

  @Override
  public K getKey() {
    return key;
  }

  @Override
  public V getValue() {
    return val;
  }

  @Override
  public V setValue(V value) {
    V ret = this.val;
    this.val = value;
    return ret;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key) ^ Objects.hashCode(val);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Map.Entry)) {
      return false;
    }
    Map.Entry<?, ?> other = (Map.Entry) o;
    return Objects.equals(this.key, other.getKey()) && Objects.equals(this.val, other.getValue());
  }
}
