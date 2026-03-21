package com.arssedot.loganalyzer.web.dto;

public record SystemMetricsDto(
        double cpuUsagePercent,
        int    cpuCores,
        long   ramUsedBytes,
        long   ramTotalBytes,
        double ramUsagePercent,
        long   diskUsedBytes,
        long   diskTotalBytes,
        double diskUsagePercent
) {
    public String ramUsed()   { return fmtBytes(ramUsedBytes); }
    public String ramTotal()  { return fmtBytes(ramTotalBytes); }
    public String diskUsed()  { return fmtBytes(diskUsedBytes); }
    public String diskTotal() { return fmtBytes(diskTotalBytes); }

    private static String fmtBytes(long bytes) {
        if (bytes < 0) return "N/A";
        long kb = bytes >> 10, mb = kb >> 10, gb = mb >> 10;
        if (gb > 0) return gb + " GB";
        if (mb > 0) return mb + " MB";
        if (kb > 0) return kb + " KB";
        return bytes + " B";
    }
}
