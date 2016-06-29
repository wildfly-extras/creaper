package org.wildfly.extras.creaper.commands.logging;

/** A slightly more convenient way to create commands that affect the {@code logging} subsystem. */
public final class Logging {
    private Logging() {} // avoid instantiation

    public static RootLogger rootLogger() {
        return RootLogger.INSTANCE;
    }

    public static Logger logger() {
        return Logger.INSTANCE;
    }

    public static LogHandler handler() {
        return LogHandler.INSTANCE;
    }

    // ---

    public static final class RootLogger {
        private static final RootLogger INSTANCE = new RootLogger();

        private RootLogger() {}

        public ChangeRootLogger.Builder change() {
            return new ChangeRootLogger.Builder();
        }
    }

    public static final class Logger {
        private static final Logger INSTANCE = new Logger();

        private Logger() {}

        public AddLogger.Builder add(String category) {
            return new AddLogger.Builder(category);
        }

        public ChangeLogger.Builder change(String category) {
            return new ChangeLogger.Builder(category);
        }

        public RemoveLogger remove(String category) {
            return new RemoveLogger(category);
        }

        /**
         * Ensures that a logger for given category exists. This is essentially
         * equivalent to {@code add(category).replaceExisting()}.
         */
        public AddLogger.Builder define(String category) {
            return add(category).replaceExisting();
        }
    }

    public static final class LogHandler {
        private static final LogHandler INSTANCE = new LogHandler();

        private LogHandler() {}

        public ConsoleLogHandler console() {
            return ConsoleLogHandler.INSTANCE;
        }

        public PeriodicRotatingFileLogHandler periodicRotatingFile() {
            return PeriodicRotatingFileLogHandler.INSTANCE;
        }
    }

    // ---

    public static final class ConsoleLogHandler {
        private static final ConsoleLogHandler INSTANCE = new ConsoleLogHandler();

        private ConsoleLogHandler() {}

        public AddConsoleLogHandler.Builder add(String name) {
            return new AddConsoleLogHandler.Builder(name);
        }

        public ChangeConsoleLogHandler.Builder change(String name) {
            return new ChangeConsoleLogHandler.Builder(name);
        }

        public RemoveLogHandler remove(String name) {
            return new RemoveLogHandler(LogHandlerType.CONSOLE, name);
        }
    }

    public static final class PeriodicRotatingFileLogHandler {
        private static final PeriodicRotatingFileLogHandler INSTANCE = new PeriodicRotatingFileLogHandler();

        private PeriodicRotatingFileLogHandler() {}

        /** all parameters are mandatory (i.e., must not be {@code null}) */
        public AddPeriodicRotatingFileLogHandler.Builder add(String name, String file, String suffix) {
            return new AddPeriodicRotatingFileLogHandler.Builder(name, file, suffix);
        }

        /**
         * A variant that doesn't change the file name nor the suffix.
         * @see #change(String, String, String)
         */
        public ChangePeriodicRotatingFileLogHandler.Builder change(String name) {
            return new ChangePeriodicRotatingFileLogHandler.Builder(name, null, null);
        }

        /** {@code file} and {@code suffix} can be {@code null} if they don't need to be changed */
        public ChangePeriodicRotatingFileLogHandler.Builder change(String name, String file, String suffix) {
            return new ChangePeriodicRotatingFileLogHandler.Builder(name, file, suffix);
        }

        public RemoveLogHandler remove(String name) {
            return new RemoveLogHandler(LogHandlerType.PERIODIC_ROTATING_FILE, name);
        }
    }
}
