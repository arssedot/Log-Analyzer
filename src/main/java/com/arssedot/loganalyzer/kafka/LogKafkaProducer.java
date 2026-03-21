package com.arssedot.loganalyzer.kafka;

import com.arssedot.loganalyzer.config.KafkaConfig;
import com.arssedot.loganalyzer.web.dto.LogIngestRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Sends log messages to Kafka. Also exposes a demo generator
 * for populating sample data via the /api/demo/generate endpoint.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogKafkaProducer {

    private static final Random RANDOM = new Random();

    private static final List<String> SERVICES = List.of(
            "auth-service", "order-service", "payment-service",
            "user-service", "notification-service", "gateway"
    );

    private static final List<String> LEVELS = List.of("DEBUG", "INFO", "INFO", "INFO", "WARN", "ERROR");

    private static final List<String> MESSAGES = List.of(
            "User login successful",
            "Processing order #%d",
            "Payment gateway timeout after 3000ms",
            "Cache miss for key user_%d",
            "Database connection pool exhausted",
            "Request completed in %dms",
            "Retrying failed request (attempt %d/3)",
            "NullPointerException in OrderProcessor.process()",
            "Scheduled job completed successfully",
            "Rate limit exceeded for IP 192.168.1.%d"
    );

    private final KafkaTemplate<String, LogIngestRequest> kafkaTemplate;

    public void send(LogIngestRequest request) {
        kafkaTemplate.send(KafkaConfig.TOPIC_LOGS_INGEST, request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send log to Kafka: {}", ex.getMessage());
                    }
                });
    }

    public void sendSampleLogs(int count) {
        log.info("Generating {} sample log entries via Kafka...", count);
        for (int i = 0; i < count; i++) {
            String service = SERVICES.get(RANDOM.nextInt(SERVICES.size()));
            String level = LEVELS.get(RANDOM.nextInt(LEVELS.size()));
            String messageTemplate = MESSAGES.get(RANDOM.nextInt(MESSAGES.size()));
            String message = messageTemplate.contains("%d")
                    ? String.format(messageTemplate, RANDOM.nextInt(10000))
                    : messageTemplate;

            Instant ts = Instant.now().minusSeconds(RANDOM.nextInt(86400));

            var request = new LogIngestRequest(
                    ts,
                    level,
                    service,
                    message,
                    UUID.randomUUID().toString().substring(0, 16),
                    "host-" + (RANDOM.nextInt(5) + 1)
            );
            send(request);
        }
    }
}
