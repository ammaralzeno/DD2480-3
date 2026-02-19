import java.util.Arrays;

/** Manual branch coverage: init, hit(id), report. */
public class manualCoverage {
    private static final boolean[] hit = new boolean[20];

    /** Reset. */
    public static void init() {
        Arrays.fill(hit, false);
    }

    /** Record branch id taken. */
    public static void hit(int branchId) {
        if (branchId >= 0 && branchId < 20) hit[branchId] = true;
    }

    /** Print hit branches to stdout. */
    public static void report() {
        int count = 0;
        for (int i = 0; i < 20; i++) {
            if (hit[i]) {
                System.out.println("  " + i);
                count++;
            }
        }
        System.out.println("Branch coverage: " + count + " branches hit");
    }


    public static void main(String[] args) {
        init();
        report();
    }
}
