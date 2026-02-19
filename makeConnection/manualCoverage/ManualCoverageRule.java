package org.schabi.newpipe.player.datasource.coverage;

import com.example.coverage.ManualCoverage;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ManualCoverageRule implements TestRule {

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // Before all tests in this class
                ManualCoverage.reset();

                try {
                    base.evaluate(); // run all tests
                } finally {
                    // After all tests in this class
                    printCoverage(description);
                }
            }
        };
    }

    private void printCoverage(final Description description) {
        System.out.println("=== Coverage for " + description.getClassName() + " ===");
        ManualCoverage.getHits().stream()
                .sorted()
                .forEach(id -> System.out.println("  hit branch " + id));
        System.out.println("==============================================");
    }
}

