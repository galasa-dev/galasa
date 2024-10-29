/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Boot Logger for outside Felix framework
 */
public class BootLogger {

    private static Level bootlogLevel = Level.DEBUG;

    // Use Log4j log level from log4j.properties
    static {
        String log4jFileName = "log4j.properties";
        Properties log4jProps = new Properties();
        InputStream input = null;

        try {
            input = BootLogger.class.getClassLoader().getResourceAsStream(log4jFileName);
            if (input == null) {
                // Running from a jar
                input = BootLogger.class.getClass().getResourceAsStream("/" + log4jFileName);
            }
            if (input != null) {
                log4jProps.load(input);
                String rootLogger = log4jProps.getProperty("log4j.rootLogger");
                if (rootLogger != null) {
                    String[] parts = rootLogger.split(",");
                    if (parts.length >= 1 && Level.isValid(parts[0])) {
                        bootlogLevel = Level.valueOf(parts[0]);
                    }
                }
            }
        } catch (IOException e) {
            // Ignore
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Debug message
     * 
     * @param message
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    /**
     * Info message
     * 
     * @param message
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Trace message
     * 
     * @param message
     */
    public void trace(String message) {
        log(Level.TRACE, message);
    }

    /**
     * Error message
     * 
     * @param message
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }

    /**
     * Error message with Throwable
     * 
     * @param message
     * @param e
     */
    public void error(String message, Throwable e) {
        log(Level.ERROR, message);
        e.printStackTrace(System.out); // NOSONAR
    }

    /**
     * Fatal message
     * 
     * @param message
     */
    public void fatal(String message) {
        log(Level.FATAL, message);
    }

    /**
     * Fatal message with Throwable
     * 
     * @param message
     * @param e
     */
    public void fatal(String message, Throwable e) {
        log(Level.FATAL, message);
        e.printStackTrace(System.out); // NOSONAR
    }

    /**
     * Is trace Level active
     * 
     * @return
     */
    public boolean isTraceEnabled() {
        return (bootlogLevel.getValue() >= Level.TRACE.getValue());
    }

    private void log(Level level, String message) {
        if (level.getValue() <= bootlogLevel.getValue()) {
            StringBuilder sb = new StringBuilder();
            sb.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date()));
            sb.append(' ');
            sb.append(level);
            sb.append(' ');
            sb.append(Thread.currentThread().getStackTrace()[3].getClassName());
            sb.append('.');
            sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
            sb.append(" - ");
            sb.append(message);
            System.out.println(sb.toString()); // NOSONAR
        }
    }

    public void setLevel(Level level) {
        bootlogLevel = level;
    }

    public enum Level {
        FATAL(0),
        ERROR(1),
        WARN(2),
        INFO(3),
        DEBUG(4),
        TRACE(5),
        ALL(9);

        private final int name;

        private Level(int level) {
            this.name = level;
        }

        public int getValue() {
            return name;
        }

        public static boolean isValid(String level) {
            for (Level lvl : Level.values()) {
                if (lvl.name().equalsIgnoreCase(level))
                    return true;
            }
            return false;
        }
    }

}