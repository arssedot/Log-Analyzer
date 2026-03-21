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

    private final OperatingSystemMXBean stdBean;
    @SuppressWarnings("restriction")
    private final com.sun.management.OperatingSystemMXBean sunBean;

    private volatile double cpuPercent = 0.0;

    @SuppressWarnings({"restriction", "unchecked"})
    public SystemMetricsService() {
        var raw = ManagementFactory.getOperatingSystemMXBean();
        stdBean = raw;
        sunBean = raw instanceof com.sun.management.OperatingSystemMXBean b ? b : null;
        if (sunBean == null) {
            log.warn("com.sun.management.OperatingSystemMXBean unavailable — CPU will use load-average fallback");
        }
    }

    @Scheduled(fixedDelay = 2_000)
    @SuppressWarnings("restriction")
    void refreshCpu() {
        if (sunBean != null) {
            double load = sunBean.getCpuLoad();
            cpuPercent = load >= 0 ? load * 100.0 : 0.0;
        } else {
            double avg = stdBean.getSystemLoadAverage();
            cpuPercent = avg >= 0
                    ? Math.min(100.0, avg / stdBean.getAvailableProcessors() * 100.0)
                    : 0.0;
        }
    }

    @SuppressWarnings("restriction")
    public SystemMetricsDto getMetrics() {
        int cores = stdBean.getAvailableProcessors();

        long totalMem, freeMem;
        if (sunBean != null) {
            totalMem = sunBean.getTotalMemorySize();
            freeMem  = sunBean.getFreeMemorySize();
        } else {
            Runtime rt = Runtime.getRuntime();
            totalMem = rt.maxMemory();
            freeMem  = rt.freeMemory();
        }
        long usedMem = Math.max(0, totalMem - freeMem);

        long diskTotal = 0, diskFree = 0;
        for (File root : File.listRoots()) {
            diskTotal += root.getTotalSpace();
            diskFree  += root.getUsableSpace();
        }
        long diskUsed = Math.max(0, diskTotal - diskFree);

        return new SystemMetricsDto(
                round(cpuPercent), cores,
                usedMem, totalMem, pct(usedMem, totalMem),
                diskUsed, diskTotal, pct(diskUsed, diskTotal)
        );
    }

    private static double round(double v) { return Math.round(v * 10.0) / 10.0; }
    private static double pct(long used, long total) {
        return total > 0 ? round(used * 100.0 / total) : 0.0;
    }
}
