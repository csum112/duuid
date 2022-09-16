package org.example.generator.benchmark;

import org.example.generator.UniqueIdGenerator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class Benchmark {

    private final static int N = 100000;
    private final static int SAMPLES = 30;

    public static class BenchmarkResults {

        private double avgThroughput = 0;
        private double avgLatency = 0;
        private double avgFailureCount = 0;

        public void printSummary() {
            System.out.printf(
                    "=================================== %n" +
                            "BENCHMARK RESULTS: %n" +
                            "No. Samples: \t %d %n" +
                            "Avg. Throughput (IDs/second): \t %f %n" +
                            "Avg. Latency (ms): \t %f %n" +
                            "Avg. Failure Count (exceptions): \t %f %n" +
                            "=================================== %n",
                    SAMPLES, avgThroughput, avgLatency, avgFailureCount
            );
        }
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private static BenchmarkResults runTest(UniqueIdGenerator generator) {
        BenchmarkResults results = new BenchmarkResults();
        AtomicLong latency = new AtomicLong(0);
        AtomicLong failures = new AtomicLong(0);

        final long start = now();
        IntStream.range(0, N)
                .parallel()
                .forEach(ignored -> {
                    try {
                        generator.generateID();
                        latency.compareAndExchange(0L, now() - start);
                    } catch (Exception exception) {
                        failures.incrementAndGet();
                    }
                });

        final long elapsed = now() - start;
        results.avgThroughput = (double) N / elapsed * 1000;
        results.avgLatency = latency.get();
        results.avgFailureCount = failures.get();
        return results;
    }

    public static BenchmarkResults evaluate(UniqueIdGenerator generator) {
        BenchmarkResults results = new BenchmarkResults();

        for (int i = 0; i < SAMPLES; i++) {
            BenchmarkResults intermediary = runTest(generator);
            results.avgThroughput += intermediary.avgThroughput;
            results.avgLatency += intermediary.avgLatency;
            results.avgFailureCount += intermediary.avgFailureCount;
        }

        results.avgThroughput /= SAMPLES;
        results.avgLatency /= SAMPLES;
        results.avgFailureCount /= SAMPLES;

        return results;
    }
}
