package com.dpi.core;

import com.dpi.model.Packet;
import com.dpi.service.StatsCollector;
import com.dpi.utils.LoggerUtil;

/**
 * Task for processing an individual packet.
 */
public class PacketProcessor implements Runnable {
    private final Packet packet;
    private final RuleEngine ruleEngine;
    private final StatsCollector statsCollector;

    public PacketProcessor(Packet packet, RuleEngine ruleEngine, StatsCollector statsCollector) {
        this.packet = packet;
        this.ruleEngine = ruleEngine;
        this.statsCollector = statsCollector;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();

        boolean blocked = ruleEngine.shouldBlock(packet);
        String reason = "";

        if (blocked) {
            if (ruleEngine.isRateLimited(packet.getSourceIp())) {
                reason = "Rate Limit Exceeded";
            } else {
                reason = "Security Rule Match";
            }
        }

        statsCollector.recordPacket(packet, blocked, reason);

        long endTime = System.nanoTime();
        double processingTimeMs = (endTime - startTime) / 1_000_000.0;

        if (blocked) {
            LoggerUtil.getLogger().warning(String.format("BLOCKED: %s | Reason: %s | Perf: %.3fms",
                    packet, reason, processingTimeMs));
        } else {
            LoggerUtil.getLogger().info(String.format("FORWARDED: %s | Perf: %.3fms",
                    packet, processingTimeMs));
        }
    }
}
