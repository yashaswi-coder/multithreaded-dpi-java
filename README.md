# Advanced Deep Packet Inspection (DPI) System

A professional-grade, multithreaded Deep Packet Inspection (DPI) system implemented in pure Java. This system demonstrates concurrent processing, security rule enforcement, rate limiting, and traffic statistics aggregation.

## 🏗️ Architecture Design

The system is built using a modular, package-based architecture following the **Worker Thread (Producer-Consumer)** pattern.

```text
com.dpi
  ├── Main (Orchestrator)
  ├── core (Processing Engine)
  │    ├── RuleEngine (Filtering Logic)
  │    └── PacketProcessor (Worker Task)
  ├── model (Data Objects)
  │    └── Packet (Network Entity)
  ├── service (Business Services)
  │    └── StatsCollector (Metric Aggregator)
  └── utils (System Utilities)
       ├── LoggerUtil (Logging)
       └── JsonExporter (Reporting)
```

## 🚀 Key Features

- **Concurrent Inspection**: Processes multiple packets simultaneously using a fixed thread pool.
- **Rule-Based Filtering**: Block malicious traffic by IP address or Domain.
- **Intelligent Rate Limiting**: Automatically blocks IPs exceeding pre-defined traffic thresholds.
- **Priority Classification**: Categorizes traffic into High, Medium, and Low priorities.
- **Suspicious IP Detection**: Flags IPs that consistently trigger security violations.
- **Nanosecond Performance Metrics**: Tracks and logs processing latency for every packet.
- **Audit Logs**: Comprehensive activity logging in `logs/app.log`.
- **JSON Reports**: Exports session statistics to `stats.json`.

## 🛠️ Technologies Used

- **Language**: Java 11+
- **Concurrency**: `java.util.concurrent` (ExecutorService, AtomicInteger, ConcurrentHashMap)
- **Logging**: `java.util.logging`
- **Build**: Manual `javac` with package structure handling

## 📖 How to Run

### Prerequisites
- Java Development Kit (JDK) 11 or higher installed and configured.

### Compilation
Navigate to the `src` directory and run:
```bash
cd AdvancedDPI/src
javac -d . com/dpi/Main.java com/dpi/core/*.java com/dpi/model/*.java com/dpi/service/*.java com/dpi/utils/*.java
```

### Execution
Run the system by providing the rules and packets files as arguments:
```bash
java com.dpi.Main ../rules.txt ../packets.txt
```

## 📝 Sample Input/Output

### rules.txt
```text
BLOCK_IP 192.168.1.100
BLOCK_DOMAIN malicious.com
```

### Console Summary Output
```text
=== Traffic Statistics Summary ===
Total Packets:     16
Dropped Packets:   5
Forwarded Packets: 11

--- Per-Domain Traffic ---
google.com           : 1
github.com           : 1
...
==============================
```

## 🔮 Future Improvements

- **Web Dashboard**: Implement a real-time monitoring UI using a lightweight HTTP server.
- **Database Persistence**: Store traffic logs and statistics in a SQL or NoSQL database for long-term analysis.
- **Regex Rule Matching**: Support complex pattern matching for payload inspection.
- **Dynamic Rule Management**: Allow adding/removing rules via an API or watcher service.
