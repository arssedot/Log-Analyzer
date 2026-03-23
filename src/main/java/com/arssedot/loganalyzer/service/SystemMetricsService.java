package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.web.dto.SystemMetricsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@Service
@Slf4j
public class SystemMetricsService {

    private final OperatingSystemMXBean standardOsBean;
    @SuppressWarnings("restriction")
    private final com.sun.management.OperatingSystemMXBean sunOsBean;

    private volatile double cpuPercent = 0.0;

    @SuppressWarnings({"restriction", "unchecked"})
    public SystemMetricsService() {
        OperatingSystemMXBean rawOsBean = ManagementFactory.getOperatingSystemMXBean();
        standardOsBean = rawOsBean;
        sunOsBean = rawOsBean instanceof com.sun.management.OperatingSystemMXBean castedBean ? castedBean : null;
        if (sunOsBean == null) {
            log.warn("com.sun.management.OperatingSystemMXBean unavailable — CPU will use load-average fallback");
        }
    }

    @Scheduled(fixedDelay = 2_000)
    @SuppressWarnings("restriction")
    void refreshCpu() {
        if (sunOsBean != null) {
            double cpuLoad = sunOsBean.getCpuLoad();
            cpuPercent = cpuLoad >= 0 ? cpuLoad * 100.0 : 0.0;
        } else {
            double loadAverage = standardOsBean.getSystemLoadAverage();
            cpuPercent = loadAverage >= 0
                    ? Math.min(100.0, loadAverage / standardOsBean.getAvailableProcessors() * 100.0)
                    : 0.0;
        }
    }

    @SuppressWarnings("restriction")
    public SystemMetricsDto getMetrics() {
        int cpuCores = standardOsBean.getAvailableProcessors();

        long totalMemory, freeMemory;
        if (sunOsBean != null) {
            totalMemory = sunOsBean.getTotalMemorySize();
            freeMemory  = sunOsBean.getFreeMemorySize();
        } else {
            Runtime runtime = Runtime.getRuntime();
            totalMemory = runtime.maxMemory();
            freeMemory  = runtime.freeMemory();
        }
        long usedMemory = Math.max(0, totalMemory - freeMemory);

        long totalDisk = 0, freeDisk = 0;
        for (File root : File.listRoots()) {
            totalDisk += root.getTotalSpace();
            freeDisk  += root.getUsableSpace();
        }
        long usedDisk = Math.max(0, totalDisk - freeDisk);

        return new SystemMetricsDto(
                roundToOneDecimal(cpuPercent), cpuCores,
                usedMemory, totalMemory, calculatePercentage(usedMemory, totalMemory),
                usedDisk, totalDisk, calculatePercentage(usedDisk, totalDisk)
        );
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static double calculatePercentage(long used, long total) {
        return total > 0 ? roundToOneDecimal(used * 100.0 / total) : 0.0;
    }
}
