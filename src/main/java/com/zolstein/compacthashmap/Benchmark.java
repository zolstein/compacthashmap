package com.zolstein.compacthashmap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Benchmark {
    public static void main(String[] args) {
        new Benchmark().run();
    }

    private static final int sizeLimit = 100000;

    private Map<Long, Integer> map = new CompactHashMap<>();
    private Random random = new Random(0x8765432101234567L);
    private Random hasher = new Random();

    public void run() {
        for (int i = 0; i < sizeLimit / 2; i++) {
            hasher.setSeed(i);
            long key = hasher.nextLong(); // Generates reasonable "hash" value
            map.put(key, i);
        }
        long iters = 0;
        long modify = 0;
        long iterate = 0;
        long lookup = 0;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 60000) {
            double r = random.nextDouble();
            if (r > 0.66) {
                doModify();
                modify++;
            } else if (r < .33) {
                doIterate();
                iterate++;
            } else {
                doRandomLookup();
                lookup++;
            }
            ++iters;
        }
        System.out.println(
                String.format("%d iterations completed\n%d lookup\n%d iterate\n%d modify\n",
                        iters, lookup, iterate, modify));
    }

    public void doRandomLookup() {
        for (int i = 0; i < map.size(); i++) {
            int value = random.nextInt(sizeLimit);
            hasher.setSeed(value);
            long key = hasher.nextLong(); // Generates reasonable "hash" value
            map.get(key);
        }
    }

    public void doIterate() {
        Iterator<Map.Entry<Long, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
    }

    public void doModify() {
        for (int i = 0; i < map.size() / 10; i++) {
            int value = random.nextInt(sizeLimit);
            hasher.setSeed(value);
            long key = hasher.nextLong(); // Generates reasonable "hash" value
            if (map.containsKey(key)) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        }
    }
}
