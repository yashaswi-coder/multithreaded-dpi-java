package com.dpi.model;

/**
 * Represents a network packet with source IP, destination domain, and priority
 * classification.
 */
public class Packet {
    private final String sourceIp;
    private final String domain;
    private final Priority priority;
    private final long timestamp;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    public Packet(String sourceIp, String domain) {
        this.sourceIp = sourceIp;
        this.domain = domain;
        this.timestamp = System.currentTimeMillis();
        this.priority = classifyPriority(domain);
    }

    private Priority classifyPriority(String domain) {
        if (domain.endsWith(".gov") || domain.endsWith(".edu") || domain.contains("bank")) {
            return Priority.HIGH;
        } else if (domain.endsWith(".com") || domain.endsWith(".org")) {
            return Priority.MEDIUM;
        }
        return Priority.LOW;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDomain() {
        return domain;
    }

    public Priority getPriority() {
        return priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] IP: %s, Domain: %s, Priority: %s",
                priority, sourceIp, domain, priority);
    }
}
