package org.example.generator;

import org.example.generator.impl.StateStoreImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Random;

public class StateStoreTest {
    @Test
    public void testStoresAndLoadsCorrectlyOnClose() throws IOException {
        final File tempFile = Files.createTempFile("persistenceTest", ".bin").toFile();
        final long data = new Random().nextLong();

        // Write some data and destroy the object.
        Assertions.assertDoesNotThrow(() -> {
            try (StateStore stateStore = new StateStoreImpl(tempFile)) {
                stateStore.persist(data);
            }
        });

        // Open a new state store and expect it to load the persisted data.
        Assertions.assertDoesNotThrow(() -> {
            try (StateStore stateStore = new StateStoreImpl(tempFile)) {
                final Optional<Long> storedData = stateStore.load();
                Assertions.assertTrue(storedData.isPresent());
                Assertions.assertEquals(data, storedData.get());
            }
        });
    }

    @Test
    public void testAlwaysStoresLatestEntry() throws IOException {
        final File tempFile = Files.createTempFile("persistenceTest", ".bin").toFile();
        final Random random = new Random();

        Assertions.assertDoesNotThrow(() -> {
            try (StateStore stateStore = new StateStoreImpl(tempFile)) {

                // Store 100 entries and remember the last one.
                long data = 0;

                for (int i = 0; i < 100; i++) {
                    data = random.nextLong();
                    stateStore.persist(data);
                }

                // Expect it to load the latest persisted entry.
                Optional<Long> storedData = stateStore.load();
                Assertions.assertTrue(storedData.isPresent());
                Assertions.assertEquals(data, storedData.get());
            }
        });
    }
}