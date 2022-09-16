package org.example.generator.server;

import org.example.generator.StateStore;
import org.example.generator.UniqueIdGenerator;
import org.example.generator.impl.StateStoreImpl;
import org.example.generator.impl.UniqueIdGeneratorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.time.Clock;

@Configuration
public class Config {

    @Bean
    public Long nodeId() {
        return 0L;
    }

    @Bean
    public Clock createClock() {
        return Clock.systemUTC();
    }

    @Bean
    public File createStateFile() {
        return new File("state.bin");
    }

    @Bean
    @Autowired
    public StateStore createStateStore(File stateFile) throws IOException {
        return new StateStoreImpl(stateFile);
    }

    @Bean
    @Autowired
    public UniqueIdGenerator createGenerator(Clock clock, StateStore stateStore, Long nodeId) {
        return new UniqueIdGeneratorImpl(clock, stateStore, nodeId);
    }
}
