package org.schabi.newpipe.coverage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Mp4DashReaderBranchCoverage {

    private static final int TOTAL_BRANCHES = 33;
    private static final Map<Integer, Integer> HITS = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(Mp4DashReaderBranchCoverage::printReport));
    }

    private Mp4DashReaderBranchCoverage() {
    }

    public static void recordBranch(final int id) {
        HITS.merge(id, 1, Integer::sum);
    }

    public static void printReport() {
        System.out.println("\n==== DIY Branch Coverage: getNextChunk ====");
        int covered = 0;

        for (int i = 1; i <= TOTAL_BRANCHES; i++) {
            final int count = HITS.getOrDefault(i, 0);
            if (count > 0) {
                covered++;
            }

            System.out.printf("Branch %2d: %s (hit %d times)%n",
                    i,
                    count > 0 ? "COVERED" : "NOT COVERED",
                    count);
        }

        System.out.printf("%nCoverage: %.2f%% (%d/%d)%n",
                (covered * 100.0) / TOTAL_BRANCHES,
                covered,
                TOTAL_BRANCHES);

        System.out.println("=============================\n");
    }

    public static void reset() {
        HITS.clear();
    }
}
