package org.wildfly.extras.creaper.commands.auditlog;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class AddAuditLogSyslogHandler implements OnlineCommand, OfflineCommand {

    private final String name;
    private final String appName;
    private final SyslogFacilityType facility;
    private final String formatter;
    private final int maxFailureCount;
    private final int maxLength;
    private final SyslogFormatType syslogFormat;
    private final Boolean truncate;
    private final boolean replaceExisting;
    private final String host;
    private final Integer port;
    private final MessageTransferType messageTransfer;
    private final Integer reconnectTimeout;
    private final String keyPassword;
    private final String keystorePassword;
    private final String keystorePath;
    private final String keystoreRelativeTo;
    private final AuthenticationType authenticationType;
    private final TransportProtocolType transportProtocol;

    private AddAuditLogSyslogHandler(UdpBuilder builder) {
        this.name = builder.name;
        this.appName = builder.appName;
        this.facility = builder.facility;
        this.formatter = builder.formatter;
        this.maxFailureCount = builder.maxFailureCount;
        this.maxLength = builder.maxLength;
        this.syslogFormat = builder.syslogFormat;
        this.truncate = builder.truncate;
        this.replaceExisting = builder.replaceExisting;
        this.host = builder.host;
        this.port = builder.port;
        this.messageTransfer = null;
        this.reconnectTimeout = null;
        this.keyPassword = null;
        this.keystorePassword = null;
        this.keystorePath = null;
        this.keystoreRelativeTo = null;
        this.authenticationType = null;
        this.transportProtocol = TransportProtocolType.UDP;
    }

    protected AddAuditLogSyslogHandler(TcpBuilder builder) {
        this.name = builder.name;
        this.appName = builder.appName;
        this.facility = builder.facility;
        this.formatter = builder.formatter;
        this.maxFailureCount = builder.maxFailureCount;
        this.maxLength = builder.maxLength;
        this.syslogFormat = builder.syslogFormat;
        this.truncate = builder.truncate;
        this.replaceExisting = builder.replaceExisting;
        this.host = builder.host;
        this.port = builder.port;
        this.messageTransfer = builder.messageTransfer;
        this.reconnectTimeout = builder.reconnectTimeout;
        this.keyPassword = null;
        this.keystorePassword = null;
        this.keystorePath = null;
        this.keystoreRelativeTo = null;
        this.authenticationType = null;
        this.transportProtocol = TransportProtocolType.TCP;
    }

    private AddAuditLogSyslogHandler(TlsBuilder builder) {
        this.name = builder.name;
        this.appName = builder.appName;
        this.facility = builder.facility;
        this.formatter = builder.formatter;
        this.maxFailureCount = builder.maxFailureCount;
        this.maxLength = builder.maxLength;
        this.syslogFormat = builder.syslogFormat;
        this.truncate = builder.truncate;
        this.replaceExisting = builder.replaceExisting;
        this.host = builder.host;
        this.port = builder.port;
        this.messageTransfer = builder.messageTransfer;
        this.reconnectTimeout = builder.reconnectTimeout;
        this.keyPassword = builder.keyPassword;
        this.keystorePassword = builder.keystorePassword;
        this.keystorePath = builder.keystorePath;
        this.keystoreRelativeTo = builder.keystoreRelativeTo;
        this.authenticationType = builder.authenticationType;
        this.transportProtocol = TransportProtocolType.TLS;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (reconnectTimeout != null) {
            if (ctx.version.lessThan(ServerVersion.VERSION_1_7_0)
                    || ctx.version.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0)) {
                throw new AssertionError("Option reconnect-timeout is available since WildFly 9 or in EAP 6.4.x.");
            }
        }

        Operations ops = new Operations(ctx.client);

        Address handlerAddress = Address.coreService("management")
                .and("access", "audit")
                .and("syslog-handler", name);
        Address handlerProtocolAddress = handlerAddress.and("protocol", transportProtocol.value());
        Address handlerAuthAddress = null;

        if (replaceExisting) {
            try {
                ops.removeIfExists(handlerAddress);
            } catch (OperationException ex) {
                throw new CommandFailedException("Failed to remove existing syslog-handler " + name, ex);
            }
        }

        Batch batch = new Batch();

        batch.add(handlerAddress, Values.empty()
                .andOptional("name", name)
                .andOptional("app-name", appName)
                .andOptional("facility", facility == null ? null : facility.value())
                .andOptional("formatter", formatter)
                .andOptional("max-failure-count", maxFailureCount)
                .andOptional("max-length", maxLength)
                .andOptional("syslog-format", syslogFormat == null ? null : syslogFormat.value())
                .andOptional("truncate", truncate));

        if (transportProtocol.equals(TransportProtocolType.UDP)) {
            batch.add(handlerProtocolAddress, Values.empty()
                    .andOptional("host", host)
                    .andOptional("port", port));
        } else {
            batch.add(handlerProtocolAddress, Values.empty()
                    .andOptional("host", host)
                    .andOptional("port", port)
                    .andOptional("message-transfer", messageTransfer == null ? null : messageTransfer.value())
                    .andOptional("reconnect-timeout", reconnectTimeout));
        }

        if (transportProtocol.equals(TransportProtocolType.TLS)) {
            handlerAuthAddress = handlerProtocolAddress.and("authentication", authenticationType.value());
            batch.add(handlerAuthAddress, Values.empty()
                    .andOptional("key-password", keyPassword)
                    .andOptional("keystore-password", keystorePassword)
                    .andOptional("keystore-path", keystorePath)
                    .andOptional("keystore-relative-to", keystoreRelativeTo));
        }

        ops.batch(batch);
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws Exception {
        ctx.client.apply(GroovyXmlTransform.of(AddAuditLogSyslogHandler.class)
                .subtree("management", Subtree.management())
                .parameter("atrTransportProtocol", transportProtocol == null ? null : transportProtocol.value())
                .parameter("atrName", name)
                .parameter("atrAppName", appName)
                .parameter("atrFacility", facility == null ? null : facility.value())
                .parameter("atrFormatter", formatter)
                .parameter("atrMaxFailureCount", maxFailureCount)
                .parameter("atrMaxLength", maxLength)
                .parameter("atrSyslogFormat", syslogFormat == null ? null : syslogFormat.value())
                .parameter("atrTruncate", truncate)
                .parameter("atrHost", host)
                .parameter("atrPort", port)
                .parameter("atrMessageTransfer", messageTransfer == null ? null : messageTransfer.value())
                .parameter("atrReconnectTimeout", reconnectTimeout)
                .parameter("atrKeyPassword", keyPassword)
                .parameter("atrKeystorePassword", keystorePassword)
                .parameter("atrKeystorePath", keystorePath)
                .parameter("atrKeystoreRelativeTo", keystoreRelativeTo)
                .parameter("atrAuthenticationType", authenticationType == null ? null : authenticationType.value())
                .parameter("atrReplaceExisting", replaceExisting)
                .build());
    }

    abstract static class AbstractBuilder<THIS extends AbstractBuilder> {

        protected String formatter;
        protected String name;
        protected String appName;
        protected SyslogFacilityType facility;
        protected int maxFailureCount;
        protected int maxLength;
        protected SyslogFormatType syslogFormat;
        protected Boolean truncate;
        protected boolean replaceExisting;
        protected String host;
        protected Integer port;

        public AbstractBuilder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Syslog handler name must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Syslog handler name must not be an empty string");
            }
            this.name = name;
        }

        public final THIS appName(String appName) {
            this.appName = appName;
            return (THIS) this;
        }

        public final THIS facility(SyslogFacilityType facility) {
            this.facility = facility;
            return (THIS) this;
        }

        public final THIS formatter(String formatter) {
            if (formatter == null) {
                throw new IllegalArgumentException("formatter can not be null.");
            }
            this.formatter = formatter;
            return (THIS) this;
        }

        public final THIS maxFailureCount(int maxFailureCount) {
            this.maxFailureCount = maxFailureCount;
            return (THIS) this;
        }

        public final THIS maxLength(int maxLength) {
            this.maxLength = maxLength;
            return (THIS) this;
        }

        public final THIS syslogFormat(SyslogFormatType syslogFormat) {
            this.syslogFormat = syslogFormat;
            return (THIS) this;
        }

        public final THIS truncate(boolean truncate) {
            this.truncate = truncate;
            return (THIS) this;
        }

        public final THIS replaceExisting() {
            this.replaceExisting = true;
            return (THIS) this;
        }

        public final THIS host(String host) {
            this.host = host;
            return (THIS) this;
        }

        public final THIS port(int port) {
            this.port = port;
            return (THIS) this;
        }
    }

    public static final class UdpBuilder extends AbstractBuilder<UdpBuilder> {

        public UdpBuilder(String name) {
            super(name);
        }

        public AddAuditLogSyslogHandler build() {
            if (formatter == null) {
                throw new IllegalArgumentException("Formatter parameter must be specified as non null value");
            }
            if (formatter.isEmpty()) {
                throw new IllegalArgumentException("Formatter parameter cannot be empty");
            }
            return new AddAuditLogSyslogHandler(this);
        }

    }

    public static final class TcpBuilder extends AbstractBuilder<TcpBuilder> {

        private MessageTransferType messageTransfer;
        private Integer reconnectTimeout;

        public TcpBuilder(String name) {
            super(name);
        }

        public TcpBuilder messageTransfer(MessageTransferType messageTransfer) {
            this.messageTransfer = messageTransfer;
            return this;
        }

        /** This is only supported for EAP 6.4.x or WildFly 9.0.0 and above. */
        public TcpBuilder reconnectTimeout(int reconnectTimeout) {
            this.reconnectTimeout = reconnectTimeout;
            return this;
        }

        public AddAuditLogSyslogHandler build() {
            if (formatter == null) {
                throw new IllegalArgumentException("Formatter parameter must be specified as non null value");
            }
            if (formatter.isEmpty()) {
                throw new IllegalArgumentException("Formatter parameter cannot be empty");
            }
            return new AddAuditLogSyslogHandler(this);
        }

    }

    public static final class TlsBuilder extends AbstractBuilder<TlsBuilder> {

        private MessageTransferType messageTransfer;
        private Integer reconnectTimeout;
        private String keyPassword;
        private String keystorePassword;
        private String keystorePath;
        private String keystoreRelativeTo;
        private AuthenticationType authenticationType;

        public TlsBuilder(String name) {
            super(name);
        }

        public TlsBuilder messageTransfer(MessageTransferType messageTransfer) {
            this.messageTransfer = messageTransfer;
            return this;
        }

        /** This is only supported for EAP 6.4.x or WildFly 9.0.0 and above. */
        public TlsBuilder reconnectTimeout(int reconnectTimeout) {
            this.reconnectTimeout = reconnectTimeout;
            return this;
        }

        public TlsBuilder keyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public TlsBuilder keystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
            return this;
        }

        public TlsBuilder keystorePath(String keystorePath) {
            this.keystorePath = keystorePath;
            return this;
        }

        public TlsBuilder keystoreRelativeTo(String keystoreRelativeTo) {
            this.keystoreRelativeTo = keystoreRelativeTo;
            return this;
        }

        public TlsBuilder authenticationType(AuthenticationType authenticationType) {
            this.authenticationType = authenticationType;
            return this;
        }

        public AddAuditLogSyslogHandler build() {
            if (formatter == null) {
                throw new IllegalArgumentException("Formatter parameter must be specified as non null value");
            }
            if (formatter.isEmpty()) {
                throw new IllegalArgumentException("Formatter parameter cannot be empty");
            }
            if (keystorePassword == null) {
                throw new IllegalArgumentException("Keystore-password parameter must be specified as non null value");
            }
            if (keystorePassword.isEmpty()) {
                throw new IllegalArgumentException("Keystore-password parameter cannot be empty");
            }
            if (authenticationType == null) {
                throw new IllegalArgumentException("Authentication type must be specified as non null value");
            }

            return new AddAuditLogSyslogHandler(this);
        }
    }
}
