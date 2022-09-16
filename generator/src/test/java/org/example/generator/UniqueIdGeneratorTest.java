package org.example.generator;

import org.example.generator.impl.UniqueIdGeneratorImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UniqueIdGeneratorTest {

    @Mock
    private Clock clock;

    @Mock
    private StateStore stateStore;

    @Test
    public void testAllIDsUniqueWhenNormalOperation() {

        // Fresh start.
        when(stateStore.load()).thenReturn(Optional.empty());

        // Provide real time.
        when(clock.millis()).thenAnswer((ignored) -> System.currentTimeMillis());

        // Generate 100000 ids, and expect them all to be unique.
        Assertions.assertDoesNotThrow(() -> {
            UniqueIdGenerator uniqueIdGenerator = new UniqueIdGeneratorImpl(clock, stateStore, 1L);
            Set<Long> previouslyGeneratedIds = new HashSet<>();
            for (int i = 0; i < 100000; i++) {
                long id = uniqueIdGenerator.generateID();
                Assertions.assertFalse(previouslyGeneratedIds.contains(id));
                previouslyGeneratedIds.add(id);
            }
        });

    }

    @Test
    public void testAllIDsUniqueOnSameMillisecond() {

        // Fresh start.
        when(stateStore.load()).thenReturn(Optional.empty());

        // Provide fixed time.
        final long now = System.currentTimeMillis();
        when(clock.millis()).thenAnswer((ignored) -> now);

        // Generate 128 ids (8 bit limit), and expect them all to be unique.
        Assertions.assertDoesNotThrow(() -> {
            UniqueIdGenerator uniqueIdGenerator = new UniqueIdGeneratorImpl(clock, stateStore, 1L);
            Set<Long> previouslyGeneratedIds = new HashSet<>();
            for (int i = 0; i < 128; i++) {
                long id = uniqueIdGenerator.generateID();
                Assertions.assertFalse(previouslyGeneratedIds.contains(id));
                previouslyGeneratedIds.add(id);
            }
        });
    }

    @Test
    public void testAllIDsUniqueOnDifferentNodeId() {

        // Fresh start.
        when(stateStore.load()).thenReturn(Optional.empty());

        // Ensure the exact same time for two generators.
        when(clock.millis()).thenReturn(0L);

        // Generate 128 ids (8 bit limit), and expect them all to be unique.
        Assertions.assertDoesNotThrow(() -> {
            UniqueIdGenerator uniqueIdGenerator1 = new UniqueIdGeneratorImpl(clock, stateStore, 1L);
            UniqueIdGenerator uniqueIdGenerator2 = new UniqueIdGeneratorImpl(clock, stateStore, 2L);
            for (long i = 1; i < 128; i++) {
                when(clock.millis()).thenReturn(i);
                Assertions.assertNotEquals(uniqueIdGenerator1.generateID(), uniqueIdGenerator2.generateID());
            }
        });
    }

    @Test
    public void testDifferentIdOnResume() {

        // Provide fixed time.
        final long now = System.currentTimeMillis();
        when(clock.millis()).thenReturn(now);

        Assertions.assertDoesNotThrow(() -> {

            // Generate one ID on a fresh state.
            when(stateStore.load()).thenReturn(Optional.empty());
            UniqueIdGenerator uniqueIdGenerator = new UniqueIdGeneratorImpl(clock, stateStore, 1L);
            long firstId = uniqueIdGenerator.generateID();

            // Recreate the generator from the previous state.
            when(stateStore.load()).thenReturn(Optional.of(firstId));
            uniqueIdGenerator = new UniqueIdGeneratorImpl(clock, stateStore, 1L);

            // Assert the IDs differ.
            long secondId = uniqueIdGenerator.generateID();
            Assertions.assertNotEquals(firstId, secondId);
        });
    }
}