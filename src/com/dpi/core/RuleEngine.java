package com.dpi.core;

import com.dpi.model.Packet;
import com.dpi.utils.LoggerUtil;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rule Engine for matching packets against security rules and enforcing rate
 * limits.
 */
public class RuleEngine {
    private final Set<String> blockedIps = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> blockedDomains = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Rate limiting: IP -> AtomicInteger (count in current session)
    private final Map<String, AtomicInteger> ipHitCount = new ConcurrentHashMap<>();
    private static final int RATE_LIMIT_THRESHOLD = 5;

    public void loadRules(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 2)
                    continue;

                String action = parts[0];
                String value = parts[1];

                if ("BLOCK_IP".equalsIgnoreCase(action)) {
                    blockedIps.add(value);
                } else if ("BLOCK_DOMAIN".equalsIgnoreCase(action)) {
                    blockedDomains.add(value);
                }
            }
            LoggerUtil.getLogger()
                    .info("Rules loaded: " + (blockedIps.size() + blockedDomains.size()) + " rules found.");
        }
    }

    public boolean shouldBlock(Packet packet) {
        // 1. Check direct IP block
        if (blockedIps.contains(packet.getSourceIp())) {
            return true;
        }

        // 2. Check direct Domain block
        if (blockedDomains.contains(packet.getDomain())) {
            return true;
        }

        // 3. Check Rate Limiting
        AtomicInteger hits = ipHitCount.computeIfAbsent(packet.getSourceIp(), k -> new AtomicInteger(0));
        if (hits.incrementAndGet() > RATE_LIMIT_THRESHOLD) {
            return true; // Rate limit exceeded
        }

        return false;
    }

    public boolean isRateLimited(String ip) {
        return ipHitCount.getOrDefault(ip, new AtomicInteger(0)).get() > RATE_LIMIT_THRESHOLD;
    }
}
