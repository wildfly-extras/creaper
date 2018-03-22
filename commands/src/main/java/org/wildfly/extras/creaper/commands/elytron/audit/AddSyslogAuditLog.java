package org.wildfly.extras.creaper.commands.elytron.audit;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddSyslogAuditLog implements OnlineCommand {

    private final String name;
    private final String serverAddress;
    private final Integer port;
    private final TransportProtocolType transport;
    private final AuditFormat format;
    private final String hostName;
    private final String sslContext;
    private final boolean replaceExisting;

    private AddSyslogAuditLog(Builder builder) {
        this.name = builder.name;
        this.serverAddress = builder.serverAddress;
        this.port = builder.port;
        this.transport = builder.transport;
        this.format = builder.format;
        this.hostName = builder.hostName;
        this.replaceExisting = builder.replaceExisting;
        this.sslContext = builder.sslContext;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        if (ctx.version.lessThan(ServerVersion.VERSION_5_0_0)) {
            throw new AssertionError("Elytron is available since WildFly 11.");
        }

        Operations ops = new Operations(ctx.client);
        Address syslogAuditAddress = Address.subsystem("elytron").and("syslog-audit-log", name);
        if (replaceExisting) {
            ops.removeIfExists(syslogAuditAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(syslogAuditAddress, Values.empty()
                .and("server-address", serverAddress)
                .and("port", port)
                .and("host-name", hostName)
                .andOptional("ssl-context", sslContext)
                .andOptional("transport", transport == null ? null : transport.name())
                .andOptional("format", format == null ? null : format.name()));
    }

    public static final class Builder {

        private final String name;
        private String serverAddress;
        private Integer port;
        private TransportProtocolType transport;
        private String hostName;
        private AuditFormat format;
        private String sslContext;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the syslog-audit-log must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the syslog-audit-log must not be empty value");
            }
            this.name = name;
        }

        public Builder serverAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder transport(TransportProtocolType transport) {
            this.transport = transport;
            return this;
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder format(AuditFormat format) {
            this.format = format;
            return this;
        }

        public Builder sslContext(String sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddSyslogAuditLog build() {
            if (serverAddress == null || serverAddress.isEmpty()) {
                throw new IllegalArgumentException("Server-address must not be null and must have a minimum length of 1 character");
            }
            if (port == null) {
                throw new IllegalArgumentException("Port must not be null");
            }
            if (hostName == null || hostName.isEmpty()) {
                throw new IllegalArgumentException("Host-name must not be null and must have a minimum length of 1 character");
            }

            return new AddSyslogAuditLog(this);
        }

    }

    public static enum TransportProtocolType {

        UDP, TCP;
    }
}
