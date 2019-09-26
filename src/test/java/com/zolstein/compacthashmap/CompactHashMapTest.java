package com.zolstein.compacthashmap;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompactHashMapTest {

    @Test
    public void testEmptyMap() {
        Map<?, ?> map = new CompactHashMap<>();
        assertThat(map).isEmpty();
        assertThat(map).hasSize(0);

        checkEmptyCollection(map.keySet());
        checkEmptyCollection(map.values());
        checkEmptyCollection(map.entrySet());

        assertThat(map.get("foo")).isNull();
        assertThat(map.containsKey("foo")).isFalse();
        assertThat(map.containsValue("foo")).isFalse();
    }

    @Test
    public void testOneElement() {
        Map<String, String> map = new CompactHashMap<>();
        assertThat(map.put("key", "value")).isNull();
        assertThat(map).isNotEmpty();
        assertThat(map).hasSize(1);
        assertThat(map.get("key")).isEqualTo("value");
        assertThat(map.get("value")).isNull();
        checkOneElementCollection(map.keySet(), "key");
        checkOneElementCollection(map.values(), "value");
        checkOneElementCollection(map.entrySet(), new CompactMapEntry<>("key".hashCode(), "key", "value"));
        // Replace element
        assertThat(map.put("key", "new_value")).isEqualTo("value");
        assertThat(map).isNotEmpty();
        assertThat(map).hasSize(1);
        assertThat(map.get("key")).isEqualTo("new_value");
        assertThat(map.get("value")).isNull();
        assertThat(map.get("new_value")).isNull();
        checkOneElementCollection(map.keySet(), "key");
        checkOneElementCollection(map.values(), "new_value");
        checkOneElementCollection(map.entrySet(), new CompactMapEntry<>("key".hashCode(), "key", "new_value"));
        // Remove element
        assertThat(map.remove("key")).isEqualTo("new_value");
        assertThat(map).isEmpty();
        assertThat(map).hasSize(0);
        assertThat(map.get("key")).isNull();
        assertThat(map.get("new_value")).isNull();
        checkEmptyCollection(map.keySet());
        checkEmptyCollection(map.values());
        checkEmptyCollection(map.entrySet());
    }

    @Test
    public void testManyInsertsNoCollisions() {
        int maxSize = 100000;
        Map<Integer, String> map = new CompactHashMap<>();
        for (int i = 1; i <= maxSize; i++) {
            assertThat(map.put(i, Integer.toString(i))).isNull();
            assertThat(map).hasSize(i);
        }
        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
        for (int i = 1; i < maxSize; i++) {
            String iStr = Integer.toString(i);
            assertThat(map).containsKey(i);
            assertThat(map.get(i)).isEqualTo(iStr);
            assertThat(iterator.hasNext()).isTrue();
            // Test of in-order traversal
            Map.Entry<Integer, String> entry = iterator.next();
            assertThat(entry.getKey()).isEqualTo(i);
            assertThat(entry.getValue()).isEqualTo(iStr);
        }
        for (int i = 1; i <= maxSize; i++) {
            String iStr = Integer.toString(i);
            assertThat(map.remove(i)).isEqualTo(iStr);
            assertThat(map.get(i)).isNull();
            assertThat(map).hasSize(maxSize - i);
        }
    }

    @Test
    public void testManyInsertsWithCollisions() {
        int maxSize = 100000;
        Map<String, Integer> map = new CompactHashMap<>();
        for (int i = 1; i <= maxSize; i++) {
            assertThat(map.put(Integer.toString(i), i)).isNull();
            assertThat(map).hasSize(i);
        }
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        for (int i = 1; i < maxSize; i++) {
            String iStr = Integer.toString(i);
            assertThat(map).containsKey(iStr);
            assertThat(map.get(iStr)).isEqualTo(i);
            assertThat(iterator.hasNext()).isTrue();
            // Test of in-order traversal
            Map.Entry<String, Integer> entry = iterator.next();
            assertThat(entry.getKey()).isEqualTo(iStr);
            assertThat(entry.getValue()).isEqualTo(i);
        }
        for (int i = 1; i <= maxSize; i++) {
            String iStr = Integer.toString(i);
            assertThat(map.remove(iStr)).isEqualTo(i);
            assertThat(map.get(iStr)).isNull();
            assertThat(map).hasSize(maxSize - i);
        }
    }

    @Test
    public void testManyInsertsBadHashCode() {
        int maxSize = 100000;
        Map<BadObject, Integer> map = new CompactHashMap<>();
        for (int i = 1; i <= maxSize; i++) {
            assertThat(map.put(BadObject.of(i), i)).isNull();
            assertThat(map).hasSize(i);
        }
        Iterator<Map.Entry<BadObject, Integer>> iterator = map.entrySet().iterator();
        for (int i = 1; i < maxSize; i++) {
            BadObject bad = BadObject.of(i);
            assertThat(map).containsKey(bad);
            assertThat(map.get(bad)).isEqualTo(i);
            assertThat(iterator.hasNext()).isTrue();
            // Test of in-order traversal
            Map.Entry<BadObject, Integer> entry = iterator.next();
            assertThat(entry.getKey()).isEqualTo(bad);
            assertThat(entry.getValue()).isEqualTo(i);
        }
        for (int i = 1; i <= maxSize; i++) {
            BadObject bad = BadObject.of(i);
            assertThat(map.remove(bad)).isEqualTo(i);
            assertThat(map.get(bad)).isNull();
            assertThat(map).hasSize(maxSize - i);
        }
    }

    @Test
    public void testManyInsertsAllCollide() {
        int maxSize = 10000;
        Map<AwfulObject, Integer> map = new CompactHashMap<>();
        for (int i = 1; i <= maxSize; i++) {
            assertThat(map.put(AwfulObject.of(i), i)).isNull();
            assertThat(map).hasSize(i);
        }
        Iterator<Map.Entry<AwfulObject, Integer>> iterator = map.entrySet().iterator();
        for (int i = 1; i < maxSize; i++) {
            AwfulObject awful = AwfulObject.of(i);
            assertThat(map).containsKey(awful);
            assertThat(map.get(awful)).isEqualTo(i);
            assertThat(iterator.hasNext()).isTrue();
            // Test of in-order traversal
            Map.Entry<AwfulObject, Integer> entry = iterator.next();
            assertThat(entry.getKey()).isEqualTo(awful);
            assertThat(entry.getValue()).isEqualTo(i);
        }
        for (int i = 1; i <= maxSize - 1; i++) {
            AwfulObject awful = AwfulObject.of(i);
            assertThat(map.remove(awful)).isEqualTo(i);
            assertThat(map.get(awful)).isNull();
            assertThat(map).hasSize(maxSize - i);
        }
        assertThat(map.get(AwfulObject.of(maxSize))).isEqualTo(maxSize);
    }

    @Test
    public void testIteratorOrderRemove() {
        Map<Integer, Integer> map = new CompactHashMap<>();
        for (int i = 1; i < 10; i++) {
            map.put(i, i);
        }
        Iterator<Integer> keyIterator = map.keySet().iterator();
        assertThat(keyIterator.next()).isEqualTo(1);
        assertThat(keyIterator.next()).isEqualTo(2);

        keyIterator.remove();
        assertThat(map).containsKey(1);
        assertThat(map).containsKey(3);
        assertThat(map.containsKey(2)).isFalse();

        assertThat(keyIterator.next()).isEqualTo(9);
        keyIterator.remove();
        assertThat(map).containsKey(1);
        assertThat(map).containsKey(3);
        assertThat(map.containsKey(9)).isFalse();

        assertThat(keyIterator.next()).isEqualTo(8);

        assertThat(keyIterator.next()).isEqualTo(3);
        keyIterator.remove();
        assertThat(map).containsKey(1);
        assertThat(map).containsKey(4);
        assertThat(map).containsKey(8);
        assertThat(map.containsKey(3)).isFalse();

        assertThat(keyIterator.next()).isEqualTo(7);
        assertThat(keyIterator.next()).isEqualTo(4);
        keyIterator.remove();
        assertThat(keyIterator.next()).isEqualTo(6);
        assertThat(keyIterator.next()).isEqualTo(5);

        assertThat(keyIterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, keyIterator::next);
    }

    @Test
    public void testMultiIterator() {
        Map<Integer, Integer> map = new CompactHashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(i, i);
        }

        Iterator<Integer> keyIterator = map.keySet().iterator();
        Iterator<Integer> valIterator = map.values().iterator();
        map.remove(-1); // Doesn't modify; should work
        map.put(0, 0);
        for (int i = 0; i < 10; i++) {
            assertThat(keyIterator.next()).isEqualTo(i);
            assertThat(valIterator.next()).isEqualTo(i);
        }
        assertThrows(NoSuchElementException.class, keyIterator::next);
        assertThrows(NoSuchElementException.class, valIterator::next);
    }


    @Test
    public void testIteratorErrors() {
        Map<Integer, Integer> map = new CompactHashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(i, i);
        }

        Iterator<Integer> iterator;
        iterator = map.keySet().iterator();
        map.remove(1);
        assertThrows(ConcurrentModificationException.class, iterator::next);

        iterator = map.keySet().iterator();
        map.put(1, 1);
        assertThrows(ConcurrentModificationException.class, iterator::next);

        iterator = map.keySet().iterator();
        iterator.next();
        map.put(10, 10);
        assertThrows(ConcurrentModificationException.class, iterator::remove);

        Iterator<Integer> keyIterator = map.keySet().iterator();
        Iterator<Integer> valIterator = map.values().iterator();
        keyIterator.next();
        valIterator.next();
        keyIterator.next();
        valIterator.remove();
        valIterator.next();
        assertThrows(ConcurrentModificationException.class, keyIterator::remove);
        assertThrows(ConcurrentModificationException.class, keyIterator::next);
        valIterator.remove();
        assertThrows(IllegalStateException.class, valIterator::remove);
    }

    @Test
    public void testHashCode() {
        CompactHashMap<Integer, Integer> map = new CompactHashMap<>();
        assertThat(map.hashCode()).isEqualTo(0);
        assertThat(map.entrySet().hashCode()).isEqualTo(0);
        for (int i = 0; i < 10; i++) {
            map.put(i, i);
        }
        assertThat(map.hashCode()).isEqualTo(0);
        assertThat(map.entrySet().hashCode()).isEqualTo(0);
    }

    @Test
    public void testEquals() {
        CompactHashMap<Integer, Integer> map = new CompactHashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put(i, i + 2);
        }
        HashMap<Integer, Integer> otherMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            otherMap.put(i, i + 2);
        }
        checkEqual(map, otherMap);
        otherMap.put(10, 12);
        checkNotEqual(map, otherMap);
        map.put(10, 12);
        checkEqual(map, otherMap);
        map.put(0, 10);
        otherMap.put(10, 10);
        checkNotEqual(map, otherMap);
        checkNotEqual(map.entrySet(), otherMap.entrySet());
        checkEqual(map.keySet(), otherMap.keySet());
    }

    @Test
    public void pseudoRandomTest() {
        Random random = new Random(0x8765432101234567L);
        Random hasher = new Random();
        int sizeLimit = 100000;
        Map<Long, Integer> map = new CompactHashMap<>();
        for (int i = 0; i < sizeLimit / 2; i++) {
            hasher.setSeed(i);
            long key = hasher.nextLong();
            map.put(key, i);
        }

        for (int i = 0; i < 10000000; i++) {
            int value = random.nextInt(sizeLimit);
            hasher.setSeed(value);
            long key = hasher.nextLong(); // Generates reasonable "hash" value
            if (map.containsKey(key)) {
                assertThat(map.remove(key)).isEqualTo(value);
            } else {
                map.put(key, value);
                assertThat(map.get(key)).isEqualTo(value);
            }
        }
    }

    private <K, V> void checkEqual(Map<K, V> m1, Map<K, V> m2) {
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m1).isEqualTo(m2);
        assertThat(m2).isEqualTo(m1);
        checkEqual(m1.keySet(), m2.keySet());
        checkEqual(m1.entrySet(), m2.entrySet());
    }

    private <K, V> void checkNotEqual(Map<K, V> m1, Map<K, V> m2) {
        assertThat(m1).isNotEqualTo(m2);
        assertThat(m2).isNotEqualTo(m1);
    }

    private <T> void checkEqual(Set<T> s1, Set<T> s2) {
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        assertThat(s1).isEqualTo(s2);
        assertThat(s2).isEqualTo(s1);
    }

    private <T> void checkNotEqual(Set<T> s1, Set<T> s2) {
        assertThat(s1).isNotEqualTo(s2);
        assertThat(s2).isNotEqualTo(s1);
    }

    private void checkEmptyCollection(Collection<?> collection) {
        assertThat(collection).isEmpty();
        assertThat(collection).hasSize(0);
        assertThat(collection.iterator().hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, () -> collection.iterator().next());
    }

    private <T> void checkOneElementCollection(Collection<T> collection, T element) {
        assertThat(collection).isNotEmpty();
        assertThat(collection).hasSize(1);
        assertThat(collection).containsExactly(element);
        Iterator<T> iterator = collection.iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(element);
        assertThat(iterator.hasNext()).isFalse();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    private static class BadObject {

        private static int shift = 3;

        private int i;

        private BadObject(int i) {
            this.i = i;
        }

        public static BadObject of(int i) {
            return new BadObject(i);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof BadObject)) {
                return false;
            }
            BadObject other = (BadObject) o;
            return this.i == other.i;
        }

        @Override
        public int hashCode() {
            return i >> shift;
        }
    }

    private static class AwfulObject {

        private int i;

        private AwfulObject(int i) {
            this.i = i;
        }

        public static AwfulObject of(int i) {
            return new AwfulObject(i);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof AwfulObject)) {
                return false;
            }
            AwfulObject other = (AwfulObject) o;
            return this.i == other.i;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
