package org.example.generator;

import org.example.generator.exceptions.StatePersistenceException;

import java.util.Optional;

/**
 * Simple persistent storage for safe-keeping intermediary generator states.
 * Used to recover in case of a failure. Either to be used with try-with-resources, or
 * manually closed before disposal.
 */
public interface StateStore extends AutoCloseable {
    /**
     * Synchronously persists the given state in a thread safe manner.
     * Each write overwrites the previous one.
     *
     * @param state - the intermediary state to be stored.
     * @throws StatePersistenceException if an I/O exception occurs.
     */
    void persist(Long state);

    /**
     * Tries to load the last known state before the application stopped.
     *
     * @return - the last known state if available, else empty.
     * @throws StatePersistenceException if an I/O exception occurs.
     */
    Optional<Long> load();
}
