package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.kafka.LogKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final LogKafkaProducer producer;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(
            @RequestParam(defaultValue = "20") int count) {
        producer.sendSampleLogs(count);
        return ResponseEntity.ok(Map.of(
                "message", "Sent " + count + " sample log entries to Kafka",
                "count", count
        ));
    }
}
