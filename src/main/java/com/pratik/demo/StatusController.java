package com.pratik.demo;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "application", "CI/CD Live Demo",
            "status", "UP",
            "message", "Hello Feroz Sir — this app is ready for the pipeline demo!",
            "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from the CI/CD demo application");
    }
}
