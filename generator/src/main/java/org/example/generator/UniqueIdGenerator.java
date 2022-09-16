package org.example.generator;


/**
 * Generates a 64-bit globally unique identifier encoded in decimal form.
 * Either to be used with try-with-resources, or manually closed before disposal.
 */
public interface UniqueIdGenerator extends AutoCloseable {
    /**
     * Thread-safe factory method to generate IDs.
     * Guarantees uniqueness even in the case of a system failure.
     *
     * @return - a globally unique identifier.
     */
    Long generateID();
}
