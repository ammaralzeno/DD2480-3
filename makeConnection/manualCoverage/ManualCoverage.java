package com.example.coverage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ManualCoverage {

    private static final Set<Integer> HITS =
            Collections.synchronizedSet(new HashSet<>());

    private ManualCoverage() {

    }

    public static void hit(final int id) {
        HITS.add(id);
    }

    public static void reset() {
        HITS.clear();
    }

    public static Set<Integer> getHits() {
        return new HashSet<>(HITS);
    }
}