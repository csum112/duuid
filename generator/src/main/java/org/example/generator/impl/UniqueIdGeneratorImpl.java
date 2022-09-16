package org.example.generator.impl;

import org.example.generator.StateStore;
import org.example.generator.UniqueIdGenerator;

import java.time.Clock;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A generator for globally unique identifiers.
 * Works similar to (<a href="https://en.wikipedia.org/wiki/Snowflake_ID">Twitter's "Snowflake IDs"</a>).
 * In order to guarantee uniqueness in the case of a system failure, the generator checkpoints intermediary
 * ID's to be later resumed.
 * Uses atomic operations to perform mutations on a common buffer for greater performance in multithreaded environments.
 * <p>
 * Each ID is an 64-bit number composed of:
 * - First 46 bits:         proprietary timestamp; represents the number of milliseconds since 1/1/2022
 * - Next 10 bits:          nodeId; the node on which the id was generated
 * - Last 8 bits:           sequence number; the ordinal number of the generated id in the given millisecond.
 */
public class UniqueIdGeneratorImpl implements UniqueIdGenerator {
    private static final long CUSTOM_EPOCH_START = 1640988000000L; // Milliseconds since the start of 1/1/2022.
    private final static int TIMESTAMP_BITS = 46;   // Allows up to 2231 years in milliseconds, valid until year 4253.
    private final static int NODE_ID_BITS = 10;     // Allows up to 1024 nodes.
    private final static int SEQUENCE_BITS = 8;     // Allows up to 128 IDs per millisecond.
    private static final long NODE_ID_MASK = ((1L << (NODE_ID_BITS)) - 1) << SEQUENCE_BITS;
    private final AtomicLong buffer;
    private final StateStore stateStore;
    private final Timer timer;

    public UniqueIdGeneratorImpl(Clock clock, StateStore stateStore, Long nodeId) {
        this.stateStore = stateStore;
        this.timer = new Timer("IdGeneratorTimeKeeper");

        // Calculate the internal nodeId representation.
        // Assumes it's less than 10-bits. Any information beyond that is lost.
        long nodeIdBits = (nodeId << SEQUENCE_BITS) & NODE_ID_MASK;

        // Load the persisted state and increment it if it exists, else initialize a clean one.
        Optional<Long> maybeLastKnownState = stateStore.load();
        buffer = maybeLastKnownState
                .map(aLong -> new AtomicLong(aLong + 1))
                .orElseGet(() -> new AtomicLong(((clock.millis() - CUSTOM_EPOCH_START)
                        << (NODE_ID_BITS + SEQUENCE_BITS)) | nodeIdBits));

        // Schedule a periodic job to continuously update the timestamp and reset the sequence counter if needed.
        TimerTask timeKeepingTask = new TimeKeepingTask(clock, buffer);
        timer.schedule(timeKeepingTask, 0L, 1);
    }

    @Override
    public Long generateID() {
        // Increments the buffer to get a globally unique ID. Assumes this method is getting called less than 128 times
        // per millisecond, else it will overflow, case in which the uniqueness guarantee is lost.
        final Long id = buffer.getAndIncrement();

        // Checkpoint in case of a system failure.
        stateStore.persist(id);
        return id;
    }

    @Override
    public void close() throws Exception {
        timer.cancel();
    }

    /**
     * Periodic job definition responsible for updating the buffer timestamp and resetting the counter.
     * Updates the id's timestamp region and resets the sequence number on each pass only if 1ms has elapsed
     * since the previous run.
     */
    private static class TimeKeepingTask extends TimerTask {
        private final Clock clock;
        private final AtomicLong bufferHandle;
        private Long lastKnownTime;

        public TimeKeepingTask(Clock clock, AtomicLong bufferHandle) {
            this.clock = clock;
            this.bufferHandle = bufferHandle;
            this.lastKnownTime = clock.millis();
        }

        @Override
        public void run() {
            final long now = clock.millis();
            assert lastKnownTime <= now;
            if (lastKnownTime < now) {

                // Atomically update the buffer to account for concurrency.
                bufferHandle.updateAndGet(old -> {

                    // Calculate the new timestamp as millis since 'CUSTOM_EPOCH_START'.
                    // No longer viable after year 4253, since it will leak into the nodeId region.
                    final long customTimestamp = now - CUSTOM_EPOCH_START;

                    // Calculate the initial ID for this millisecond, preserving the nodeId.
                    // The sequence resets to 0.
                    return (old & NODE_ID_MASK) | (customTimestamp << (NODE_ID_BITS + SEQUENCE_BITS));
                });
                lastKnownTime = now;
            }
        }
    }
}
