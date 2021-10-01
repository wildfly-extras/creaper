package org.wildfly.extras.creaper.commands.auditlog;

/**
 * A slightly more convenient way to create commands for Audit Logging. All parameters in this class's methods must be
 * specified.
 */
public final class AuditLogging {

    private AuditLogging() {
    }

    /**
     * Configures the destinations where should be the auditable events sent to.
     */
    public static AuditLogDestinationHandler destinationHandler() {
        return AuditLogDestinationHandler.INSTANCE;
    }

    /**
     * Sets whether is AuditLogging enabled and/or which destination handlers will be used.
     */
    public static AuditLogger auditLogger() {
        return AuditLogger.INSTANCE;
    }

    public static final class AuditLogDestinationHandler {

        private static final AuditLogDestinationHandler INSTANCE = new AuditLogDestinationHandler();

        private AuditLogDestinationHandler() {
        }

        public AuditLogFileHandler file() {
            return AuditLogFileHandler.INSTANCE;
        }

        public AuditLogUdpSyslogHandler udpSyslog() {
            return AuditLogUdpSyslogHandler.INSTANCE;
        }

        public AuditLogTcpSyslogHandler tcpSyslog() {
            return AuditLogTcpSyslogHandler.INSTANCE;
        }

        public AuditLogTlsSyslogHandler tlsSyslog() {
            return AuditLogTlsSyslogHandler.INSTANCE;
        }
    }

    public static final class AuditLogger {

        private static final AuditLogger INSTANCE = new AuditLogger();

        private AuditLogger() {
        }

        public AddAuditLogger.Builder add() {
            return new AddAuditLogger.Builder();
        }

        public RemoveAuditLogger remove(String name) {
            return new RemoveAuditLogger();
        }

        public ChangeAuditLogger.Builder change(String name) {
            return new ChangeAuditLogger.Builder();
        }
    }

    public static final class AuditLogFileHandler {

        private static final AuditLogFileHandler INSTANCE = new AuditLogFileHandler();

        private AuditLogFileHandler() {
        }

        public AddAuditLogFileHandler.Builder add(String name) {
            return new AddAuditLogFileHandler.Builder(name);
        }

        public RemoveAuditLogFileHandler remove(String name) {
            return new RemoveAuditLogFileHandler(name);
        }
    }

    public static final class AuditLogUdpSyslogHandler {

        private static final AuditLogUdpSyslogHandler INSTANCE = new AuditLogUdpSyslogHandler();

        private AuditLogUdpSyslogHandler() {
        }

        public AddAuditLogSyslogHandler.UdpBuilder add(String name) {
            return new AddAuditLogSyslogHandler.UdpBuilder(name);
        }

        public RemoveAuditLogSyslogHandler remove(String name) {
            return new RemoveAuditLogSyslogHandler(name);
        }
    }

    public static final class AuditLogTcpSyslogHandler {

        private static final AuditLogTcpSyslogHandler INSTANCE = new AuditLogTcpSyslogHandler();

        private AuditLogTcpSyslogHandler() {
        }

        public AddAuditLogSyslogHandler.TcpBuilder add(String name) {
            return new AddAuditLogSyslogHandler.TcpBuilder(name);
        }

        public RemoveAuditLogSyslogHandler remove(String name) {
            return new RemoveAuditLogSyslogHandler(name);
        }
    }

    public static final class AuditLogTlsSyslogHandler {

        private static final AuditLogTlsSyslogHandler INSTANCE = new AuditLogTlsSyslogHandler();

        private AuditLogTlsSyslogHandler() {
        }

        public AddAuditLogSyslogHandler.TlsBuilder add(String name) {
            return new AddAuditLogSyslogHandler.TlsBuilder(name);
        }

        public RemoveAuditLogSyslogHandler remove(String name) {
            return new RemoveAuditLogSyslogHandler(name);
        }
    }
}
