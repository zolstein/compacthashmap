package com.zolstein.compacthashmap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Benchmark {
  public static void main(String[] args) {
    new Benchmark().run();
  }

  private static final int sizeLimit = 10;

  private Map<Long, String> map = new CompactHashMap<>();
  private Random random = new Random();
  private Random hasher = new Random();

  public void run() {
    for (int i = 0; i < sizeLimit / 2; i++) {
      hasher.setSeed(i);
      long key = hasher.nextLong(); // Generates reasonable "hash" value
      map.put(key, "");
    }
    long iters = 0;
    long modify = 0;
    long iterate = 0;
    long lookup = 0;
    long start;
    random.setSeed(0x8765432101234567L);
    start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 30000) {
      doIterate();
      ++iterate;
      ++iters;
    }
    random.setSeed(0xDEADBEEFDEADBEEFL);
    start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 30000) {
      doRandomLookup();
      ++lookup;
      ++iters;
    }
    random.setSeed(0xFEDCBA0987654321L);
    start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 30000) {
      doModify();
      ++modify;
      ++iters;
    }
    System.out.println(
        String.format(
            "%d iterations completed\n%d lookup\n%d iterate\n%d modify\n",
            iters, lookup, iterate, modify));
  }

  public void doRandomLookup() {
    for (int i = 0; i < 50000; i++) {
      int value = random.nextInt(sizeLimit);
      hasher.setSeed(value);
      long key = hasher.nextLong(); // Generates reasonable "hash" value
      map.get(key);
    }
  }

  public void doIterate() {
    Iterator<Map.Entry<Long, String>> iterator = map.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      i += iterator.next().hashCode();
    }
  }

  public void doModify() {
    for (int i = 0; i < 5000; i++) {
      int value = random.nextInt(sizeLimit);
      hasher.setSeed(value);
      long key = hasher.nextLong(); // Generates reasonable "hash" value
      if (random.nextBoolean()) {
        map.remove(key);
      } else {
        map.put(key, "");
      }
    }
  }
}
