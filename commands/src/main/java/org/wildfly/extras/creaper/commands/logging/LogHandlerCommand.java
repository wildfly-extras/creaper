package org.wildfly.extras.creaper.commands.logging;


import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommand;

/**
 * Class encapsulate commands for configuring logger handlers.
 * Class also provides static methods for obtaining factories that provides methods for configuring handler of specific
 * type. E.G. LogHandlerCommand.console().add("handler").build()
 */
public abstract class LogHandlerCommand implements OnlineCommand, OfflineCommand {

    public static ConsoleLogHandlerCommandFactory console() {
        return new ConsoleLogHandlerCommandFactory();
    }

    public static PeriodicRotatingFileHandlerFactory periodicRotatingFile() {
        return new PeriodicRotatingFileHandlerFactory();
    }

    public static class ConsoleLogHandlerCommandFactory {

        /**
         * @return Builder for command that add handler of given name
         */
        public final AddConsoleLogHandler.Builder add(String name) {
            return new AddConsoleLogHandler.Builder(name);
        }

        /**
         * @return builder for command that change handler of given name
         */
        public final ChangeConsoleLogHandler.Builder change(String name) {
            return new ChangeConsoleLogHandler.Builder(name);
        }

        /**
         * @return command that remove handler of given name
         */
        public final LogHandlerCommand remove(String name) {
            return new RemoveHandler(HandlerType.CONSOLE, name);
        }
    }

    /**
     * Class provide method for configuring periodic rotating file handler
     */
    public static class PeriodicRotatingFileHandlerFactory {

        /**
         * All parameters are mandatory
         *
         * @return Builder for command that add handler
         */
        public final AddPeriodicRotatingFileHandler.Builder add(String name, String file, String suffix) {
            return new AddPeriodicRotatingFileHandler.Builder(name, file, suffix);
        }


        /**
         * @param file   If the parameter is null then the file path will not be changed.
         * @param suffix If the parameter is null then the suffix will not be changed.
         * @return builder for command that change handler of given name
         */
        public final ChangePeriodicRotatingFileHandler.Builder change(String name, String file, String suffix) {
            return new ChangePeriodicRotatingFileHandler.Builder(name, file, suffix);
        }

        /**
         * @return command that remove handler of given name
         */
        public final LogHandlerCommand remove(String name) {
            return new RemoveHandler(HandlerType.PERIODIC_ROTATING_FILE, name);
        }
    }
}
