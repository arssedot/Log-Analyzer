package com.arssedot.loganalyzer.kafka;

import com.arssedot.loganalyzer.service.LogIngestionService;
import com.arssedot.loganalyzer.web.dto.LogIngestRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogKafkaConsumer {

    private final LogIngestionService ingestionService;

    @KafkaListener(
            topics = "#{T(com.arssedot.loganalyzer.config.KafkaConfig).TOPIC_LOGS_INGEST}",
            groupId = "log-analyzer-group"
    )
    public void consume(LogIngestRequest request) {
        log.debug("Kafka message received from service={}, level={}", request.serviceName(), request.level());
        ingestionService.ingest(request);
    }
}
