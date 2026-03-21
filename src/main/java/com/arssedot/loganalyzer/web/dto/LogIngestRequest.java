package com.arssedot.loganalyzer.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;


public record LogIngestRequest(

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp,

        @NotNull
        @Pattern(regexp = "DEBUG|INFO|WARN|ERROR", message = "level must be one of: DEBUG, INFO, WARN, ERROR")
        String level,

        @NotBlank
        String serviceName,

        @NotBlank
        String message,

        String traceId,

        String host
) {
}
