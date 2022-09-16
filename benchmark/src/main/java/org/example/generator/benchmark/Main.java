package org.example.generator.benchmark;

import org.example.generator.StateStore;
import org.example.generator.UniqueIdGenerator;
import org.example.generator.impl.StateStoreImpl;
import org.example.generator.impl.UniqueIdGeneratorImpl;

import java.io.File;
import java.io.IOException;
import java.time.Clock;

public class Main {
    public static void main(String[] args) throws IOException {
        final long nodeId = 0L;
        final Clock clock = Clock.systemUTC();
        final File file = new File("benchmark.bin");

        try (StateStore stateStore = new StateStoreImpl(file);
             UniqueIdGenerator generator = new UniqueIdGeneratorImpl(clock, stateStore, nodeId)
        ) {
            Benchmark.evaluate(generator).printSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
