package org.example.generator.server;

import lombok.RequiredArgsConstructor;
import org.example.generator.UniqueIdGenerator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
@RequiredArgsConstructor
public class Controller {
    private final UniqueIdGenerator generator;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Long generateId() {
        return generator.generateID();
    }
}
