package com.zolstein.compacthashmap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Benchmark {
  public static void main(String[] args) {
    new Benchmark(args).run();
  }

  private int sizeLimit = 10;

  private Map<Long, String> map = new CompactHashMap<>();
  private Random random = new Random();
  private long[] lookupKeys;

  public Benchmark(String[] args) {
    for (String arg : args) {
      if (arg.equals("hashmap")) {
        // Compare against built-in hashmap
        map = new HashMap<>();
      }
      if (arg.equals("tiny")) {
        sizeLimit = 10;
      }
      if (arg.equals("small")) {
        sizeLimit = 100;
      }
      if (arg.equals("medium")) {
        sizeLimit = 1000;
      }
      if (arg.equals("large")) {
        sizeLimit = 10000;
      }
      if (arg.equals("huge")) {
        sizeLimit = 100000;
      }
    }
    System.out.printf("Testing %s; size: %d\n", map.getClass().getSimpleName(), sizeLimit);
    System.out.printf("Testing %s; size: %d\n", map.getClass().getSimpleName(), sizeLimit);
  }

  public void setup() {
    long[] keys = new long[sizeLimit];
    for (int i = 0; i < sizeLimit; i++) {
      random.setSeed(i);
      keys[i] = random.nextLong(); // Generates reasonable "hash" value
    }
    lookupKeys = keys;
  }

  public void run() {
    setup();
    for (int i = 0; i < sizeLimit / 2; i++) {
      long key = lookupKeys[i];
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
    System.out.printf("%d iterate\n", iterate);
    random.setSeed(0xDEADBEEFDEADBEEFL);
    start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 30000) {
      doRandomLookup();
      ++lookup;
      ++iters;
    }
    System.out.printf("%d lookup\n", lookup);
    random.setSeed(0xFEDCBA0987654321L);
    start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 30000) {
      doModify();
      ++modify;
      ++iters;
    }
    System.out.printf("%d modify\n", modify);
    System.out.printf("%d iterations completed\n", iters);
  }

  public void doRandomLookup() {
    for (int i = 0; i < 50000; i++) {
      int value = random.nextInt(sizeLimit);
      long key = lookupKeys[value];
      map.get(key);
    }
  }

  public void doIterate() {
    int i = 0;
    while (i < 1000000) {
      int v = 0;
      Iterator<Map.Entry<Long, String>> iterator = map.entrySet().iterator();
      while (iterator.hasNext()) {
        v += iterator.next().hashCode();
        ++i;
      }
    }
  }

  public void doModify() {
    for (int i = 0; i < 50000; i++) {
      int value = random.nextInt(sizeLimit);
      long key = lookupKeys[value];
      if (random.nextBoolean()) {
        map.remove(key);
      } else {
        map.put(key, "");
      }
    }
  }
}
