package com.dpi;

import com.dpi.core.*;
import com.dpi.model.Packet;
import com.dpi.service.StatsCollector;
import com.dpi.utils.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Entry point for the Advanced DPI System.
 */
public class Main {
    private static final int THREAD_POOL_SIZE = 4;
    private static final String STATS_JSON_FILE = "stats.json";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Error: Missing required arguments.");
            System.out.println("Usage: java com.dpi.Main <rulesFile> <packetsFile>");
            System.exit(1);
        }

        String rulesFile = args[0];
        String packetsFile = args[1];

        LoggerUtil.setup();
        LoggerUtil.getLogger().info("Initializing Multithreaded DPI System...");
        LoggerUtil.getLogger().info("Configuration: [Rules: " + rulesFile + ", Packets: " + packetsFile + "]");

        RuleEngine ruleEngine = new RuleEngine();
        StatsCollector statsCollector = new StatsCollector();

        try {
            ruleEngine.loadRules(rulesFile);
        } catch (IOException e) {
            LoggerUtil.getLogger().severe("Failed to load security rules: " + e.getMessage());
            System.exit(1);
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        LoggerUtil.getLogger().info("Starting packet inspection pipeline...");
        try (BufferedReader reader = new BufferedReader(new FileReader(packetsFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    LoggerUtil.getLogger()
                            .warning("Skipping invalid packet format at line " + lineNumber + ": " + line);
                    continue;
                }

                String ip = parts[0].trim();
                String domain = parts[1].trim();

                Packet packet = new Packet(ip, domain);
                executor.submit(new PacketProcessor(packet, ruleEngine, statsCollector));
            }
        } catch (FileNotFoundException e) {
            LoggerUtil.getLogger().severe("Packets file not found: " + packetsFile);
            executor.shutdownNow();
            System.exit(1);
        } catch (IOException e) {
            LoggerUtil.getLogger().severe("Error reading packets file: " + e.getMessage());
            executor.shutdownNow();
            System.exit(1);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LoggerUtil.getLogger().info("Processing complete. Generating reports...");
        JsonExporter.export(statsCollector, STATS_JSON_FILE);
        statsCollector.printSummary();
        LoggerUtil.getLogger().info("DPI System Shutdown Successfully.");
    }
}
