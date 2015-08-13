package org.wildfly.extras.creaper.commands.web;

import org.wildfly.extras.creaper.commands.foundation.offline.xml.GroovyXmlTransform;
import org.wildfly.extras.creaper.commands.foundation.offline.xml.Subtree;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command which creates connector with specified parameters
 */
public final class AddConnector implements OnlineCommand, OfflineCommand {
    private final String connectorName;

    private final Boolean enableLookups;
    private final Boolean enabled;
    private final String executor;
    private final Integer maxConnections;
    private final Integer maxPostSize;
    private final Integer maxSavePostSize;
    private final String protocol;
    private final String proxyBinding;
    private final String proxyName;
    private final Integer proxyPort;
    private final String redirectBinding;
    private final Integer redirectPort;
    private final Boolean secure;
    private final String scheme;
    private final String socketBinding;
    private final List<String> virtualServers;
    private final boolean replaceExisting;

    private AddConnector(Builder builder) {
        this.connectorName = builder.connectorName;
        this.enableLookups = builder.enableLookups;
        this.enabled = builder.enabled;
        this.executor = builder.executor;
        this.maxConnections = builder.maxConnections;
        this.maxPostSize = builder.maxPostSize;
        this.maxSavePostSize = builder.maxSavePostSize;
        this.protocol = builder.protocol;
        this.proxyBinding = builder.proxyBinding;
        this.proxyName = builder.proxyName;
        this.proxyPort = builder.proxyPort;
        this.redirectBinding = builder.redirectBinding;
        this.redirectPort = builder.redirectPort;
        this.secure = builder.secure;
        this.scheme = builder.scheme;
        this.socketBinding = builder.socketBinding;
        this.virtualServers = builder.virtualServers;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws IOException {
        Operations ops = new Operations(ctx.client);
        Address connectorAddress = Address.subsystem("web").and("connector", connectorName);
        if (replaceExisting) {
            try {
                ops.removeIfExists(connectorAddress);
            } catch (OperationException e) {
                throw new IOException("Failed to remove existing datasource " + connectorName, e);
            }
        }
        ops.add(connectorAddress, Values.empty()
                .andOptional("name", connectorName)
                .andOptional("enable-lookups", enableLookups)
                .andOptional("enabled", enabled)
                .andOptional("executor", executor)
                .andOptional("max-connections", maxConnections)
                .andOptional("max-post-size", maxPostSize)
                .andOptional("max-save-post-size", maxSavePostSize)
                .andOptional("protocol", protocol)
                .andOptional("proxy-binding", proxyBinding)
                .andOptional("proxy-name", proxyName)
                .andOptional("proxy-port", proxyPort)
                .andOptional("redirect-binding", redirectBinding)
                .andOptional("redirect-port", redirectPort)
                .andOptional("secure", secure)
                .andOptional("scheme", scheme)
                .andOptional("socket-binding", socketBinding)
                .andListOptional(String.class, "virtual-server", virtualServers));
    }

    @Override
    public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
        GroovyXmlTransform connector = GroovyXmlTransform
                .of(AddConnector.class)
                .subtree("web", Subtree.subsystem("web"))
                .parameter("connectorName", connectorName)
                .parameter("enableLookups", enableLookups)
                .parameter("enabled", enabled)
                .parameter("executor", executor)
                .parameter("maxConnections", maxConnections)
                .parameter("maxPostSize", maxPostSize)
                .parameter("maxSavePostSize", maxSavePostSize)
                .parameter("protocol", protocol)
                .parameter("proxyBinding", proxyBinding)
                .parameter("proxyName", proxyName)
                .parameter("proxyPort", proxyPort)
                .parameter("redirectBinding", redirectBinding)
                .parameter("redirectPort", redirectPort)
                .parameter("secure", secure)
                .parameter("scheme", scheme)
                .parameter("socketBinding", socketBinding)
                .parameter("virtualServers", virtualServers)
                .parameter("replaceExisting", replaceExisting)
                .build();
        ctx.client.apply(connector);
    }

    @Override
    public String toString() {
        return "AddConnector " + connectorName;
    }

    /**
     * Some details about the attributes can be found at http://wildscribe.github.io/JBoss%20EAP/6.2.0/subsystem/web/connector/index.html
     */
    public static final class Builder {
        private final String connectorName;

        private Boolean enableLookups;
        private Boolean enabled;
        private String executor;
        private Integer maxConnections;
        private Integer maxPostSize;
        private Integer maxSavePostSize;
        private String protocol;
        private String proxyBinding;
        private String proxyName;
        private Integer proxyPort;
        private String redirectBinding;
        private Integer redirectPort;
        private Boolean secure;
        private String scheme;
        private String socketBinding;
        private List<String> virtualServers;
        private boolean replaceExisting = false;

        public Builder(String connectorName) {
            if (connectorName == null) {
                throw new IllegalArgumentException("Name of the connector must be specified as non null value");
            }
            this.connectorName = connectorName;
        }

        /**
         * Specify whether to replace the existing connector based on its name.
         * By default existing connector is not replaced and exception is thrown.
         */
        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        /**
         * Enable/disable DNS lookups for Servlet API.
         */
        public Builder enableLookups(Boolean enableLookups) {
            this.enableLookups = enableLookups;
            return this;
        }

        /**
         * Defines whether the connector should be started on startup.
         */
        public Builder enabled(Boolean enable) {
            this.enabled = enable;
            return this;
        }

        /**
         * Sets the name of the executor that should be used for the processing threads of this connector. If undefined,
         * it defaults to using an internal pool.
         */
        public Builder executor(String executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Sets amount of concurrent connections that can be processed by the connector with optimum performance.
         * The default value depends on the connector used.
         */
        public Builder maxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        /**
         * Sets maximum size in bytes of a POST request that can be parsed by the container.
         */
        public Builder maxPostSize(Integer maxPostSize) {
            this.maxPostSize = maxPostSize;
            return this;
        }

        /**
         * Sets maximum size in bytes of a POST request that will be saved during certain authentication schemes.
         */
        public Builder maxSavePostSize(Integer maxSavePostSize) {
            this.maxSavePostSize = maxSavePostSize;
            return this;
        }

        /**
         * Sets the web connector protocol.
         */
        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets the socket binding to define the host and port that will be used when sending a redirect.
         */
        public Builder proxyBinding(String proxyBinding) {
            this.proxyBinding = proxyBinding;
            return this;
        }

        /**
         * Sets the host name that will be used when sending a redirect.
         */
        public Builder proxyName(String proxyName) {
            this.proxyName = proxyName;
            return this;
        }

        /**
         * Sets the port that will be used when sending a redirect.
         */
        public Builder proxyPort(Integer proxyPort) {
            this.proxyPort = proxyPort;
            return this;
        }

        /**
         * Sets the socket binding to define the port for redirection to a secure connector.
         */
        public Builder redirectBinding(String redirectBinding) {
            this.redirectBinding = redirectBinding;
            return this;
        }

        /**
         * Sets the port for redirection to a secure connector.
         */
        public Builder redirectPort(Integer redirectPort) {
            this.redirectPort = redirectPort;
            return this;
        }

        /**
         * Defines whether content sent or received by the connector is secured from the user perspective.
         */
        public Builder secure(Boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Sets the web connector scheme.
         */
        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        /**
         * Sets the web connector socket-binding reference, this connector should be bound to.
         */
        public Builder socketBinding(String socketBinding) {
            this.socketBinding = socketBinding;
            return this;
        }

        /**
         * Adds virtual servers to list of virtual servers that can be accessed through this connector. If none are
         * defined, the default is to allow all virtual servers.
         */
        public Builder virtualServer(String... virtualServers) {
            if (this.virtualServers == null && virtualServers != null) {
                this.virtualServers = new ArrayList<String>();
            }
            if (virtualServers != null) {
                this.virtualServers.addAll(Arrays.asList(virtualServers));
            }
            return this;
        }

        public AddConnector build() {
            return new AddConnector(this);
        }
    }
}
