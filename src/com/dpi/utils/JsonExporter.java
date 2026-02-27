package com.dpi.utils;

import com.dpi.service.StatsCollector;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility to export system statistics to JSON format.
 */
public class JsonExporter {
    public static void export(StatsCollector stats, String filePath) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
        json.append("  \"total_packets\": ").append(stats.getTotalPackets()).append(",\n");
        json.append("  \"dropped_packets\": ").append(stats.getDroppedPackets()).append(",\n");
        json.append("  \"forwarded_packets\": ").append(stats.getForwardedPackets()).append(",\n");

        json.append("  \"domain_traffic\": {\n");
        int count = 0;
        Map<String, AtomicInteger> traffic = stats.getDomainTraffic();
        for (Map.Entry<String, AtomicInteger> entry : traffic.entrySet()) {
            json.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue().get());
            if (++count < traffic.size())
                json.append(",");
            json.append("\n");
        }
        json.append("  },\n");

        json.append("  \"suspicious_ips\": [\n");
        count = 0;
        for (String ip : stats.getSuspiciousIps()) {
            json.append("    \"").append(ip).append("\"");
            if (++count < stats.getSuspiciousIps().size())
                json.append(",");
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json.toString());
            LoggerUtil.getLogger().info("Statistics exported to " + filePath);
        } catch (IOException e) {
            LoggerUtil.getLogger().severe("Failed to export JSON: " + e.getMessage());
        }
    }
}
