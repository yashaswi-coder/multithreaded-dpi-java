package com.dpi.utils;

import java.util.logging.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for configuring the system logger.
 */
public class LoggerUtil {
    private static final Logger logger = Logger.getLogger("DPI_Logger");

    public static void setup() {
        try {
            File logDir = new File("../logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            FileHandler fileHandler = new FileHandler("../logs/app.log", true);
            fileHandler.setFormatter(new CustomFormatter());
            logger.addHandler(fileHandler);

            logger.setLevel(Level.INFO);
            logger.setUseParentHandlers(false);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new CustomFormatter());
            logger.addHandler(consoleHandler);

        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    private static class CustomFormatter extends Formatter {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            builder.append(dateFormat.format(new Date(record.getMillis())));
            builder.append(" [").append(record.getLevel()).append("] ");
            builder.append(formatMessage(record));
            builder.append("\n");
            return builder.toString();
        }
    }
}
