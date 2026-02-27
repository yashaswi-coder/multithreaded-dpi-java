package com.dpi.service;

import com.dpi.model.Packet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.Collections;
import java.util.Map;

/**
 * Thread-safe collector for traffic statistics and suspicious activity
 * detection.
 */
public class StatsCollector {
    private final AtomicInteger totalPackets = new AtomicInteger(0);
    private final AtomicInteger droppedPackets = new AtomicInteger(0);
    private final AtomicInteger forwardedPackets = new AtomicInteger(0);

    private final Map<String, AtomicInteger> domainTraffic = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> ipBlockCount = new ConcurrentHashMap<>();
    private final Set<String> suspiciousIps = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final int SUSPICIOUS_THRESHOLD = 3;

    public void recordPacket(Packet packet, boolean blocked, String reason) {
        totalPackets.incrementAndGet();

        if (blocked) {
            droppedPackets.incrementAndGet();
            int blocks = ipBlockCount.computeIfAbsent(packet.getSourceIp(), k -> new AtomicInteger(0))
                    .incrementAndGet();
            if (blocks >= SUSPICIOUS_THRESHOLD) {
                suspiciousIps.add(packet.getSourceIp());
            }
        } else {
            forwardedPackets.incrementAndGet();
            domainTraffic.computeIfAbsent(packet.getDomain(), k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    public int getTotalPackets() {
        return totalPackets.get();
    }

    public int getDroppedPackets() {
        return droppedPackets.get();
    }

    public int getForwardedPackets() {
        return forwardedPackets.get();
    }

    public Map<String, AtomicInteger> getDomainTraffic() {
        return domainTraffic;
    }

    public Set<String> getSuspiciousIps() {
        return suspiciousIps;
    }

    public void printSummary() {
        System.out.println("\n=== Traffic Statistics Summary ===");
        System.out.println("Total Packets:     " + totalPackets.get());
        System.out.println("Dropped Packets:   " + droppedPackets.get());
        System.out.println("Forwarded Packets: " + forwardedPackets.get());

        System.out.println("\n--- Per-Domain Traffic ---");
        domainTraffic.forEach((domain, count) -> System.out.printf("%-20s : %d\n", domain, count.get()));

        if (!suspiciousIps.isEmpty()) {
            System.out.println("\n--- Suspicious IPs Detected ---");
            suspiciousIps.forEach(System.out::println);
        }
        System.out.println("==============================\n");
    }
}
