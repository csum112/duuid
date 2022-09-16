package org.example.generator.impl;

import org.example.generator.StateStore;
import org.example.generator.exceptions.StatePersistenceException;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Optional;

/**
 * Simply writes each intermediary states to disk in a synchronized manner.
 */
public class StateStoreImpl implements StateStore {
    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;

    public StateStoreImpl(File file) throws IOException {
        randomAccessFile = new RandomAccessFile(file, "rw");
        fileChannel = randomAccessFile.getChannel();

        // Force file content and metadata to write to disk continuously.
        fileChannel.force(true);
    }

    @Override
    public synchronized void persist(Long state) {
        try {
            ByteBuffer bytes = ByteBuffer.allocate(Long.BYTES).putLong(0, state);

            // Overwrite every time.
            fileChannel.write(bytes, 0);
        } catch (IOException e) {
            throw new StatePersistenceException(e);
        }
    }

    public Optional<Long> load() {
        try {
            return Optional.of(randomAccessFile.readLong());
        } catch (EOFException ignored) {
            return Optional.empty();
        } catch (IOException e) {
            throw new StatePersistenceException(e);
        }
    }

    @Override
    public void close() throws Exception {
        fileChannel.close();
        randomAccessFile.close();
    }
}
