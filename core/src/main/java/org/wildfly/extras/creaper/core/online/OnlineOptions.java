package org.wildfly.extras.creaper.core.online;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ManagementClient;

/**
 * This basically follows the builder pattern, but ensures on the type level that everything that must be set is
 * actually set. That means that the IDE guides you through the configuration and once the code compiles, you can be
 * sure you didn't forget anything important. The downside is that the implementation is a bit involved and exposes
 * quite a number of public types.
 */
public final class OnlineOptions {

    public final boolean isStandalone;

    public final boolean isDomain;
    public String defaultProfile;
    public String defaultHost;

    final String host;
    final int port;
    private final ManagementProtocol protocol; // this can be "null" for unspecified protocol
    private final int connectionTimeout;
    private final int bootTimeout;

    private final String username;
    private final String password;

    private final boolean localAuthDisabled;

    private final SslOptions sslOptions;

    private final ModelControllerClient wrappedModelControllerClient;
    final boolean isWrappedClient; // see OnlineManagementClientImpl.reconnect

    private OnlineOptions(Data data) {
        if (data.protocol == null) {
            if (data.sslOptions == null) {
                data.protocol = ManagementProtocol.HTTP_REMOTING;
            } else {
                data.protocol = ManagementProtocol.HTTPS_REMOTING;
            }
            // if the protocol wasn't set manually, it _doesn't_ mean that it's "http-remoting"!
            // see also OptionalOnlineOptions.protocol below
        }

        if (data.localDefault) {
            data.host = "localhost";
            switch (data.protocol) {
                case REMOTE:
                    data.port = 9999;
                    break;
                case HTTPS_REMOTING:
                case REMOTE_HTTPS:
                case HTTPS:
                    data.port = 9993;
                    break;
                default:
                    data.port = 9990;
            }
        }

        this.isStandalone = data.isStandalone;
        this.isDomain = data.isDomain;
        this.defaultProfile = data.defaultProfile;
        this.defaultHost = data.defaultHost;
        this.host = data.host;
        this.port = data.port;
        this.protocol = data.protocol;
        this.connectionTimeout = data.connectionTimeout;
        this.bootTimeout = data.bootTimeout;
        this.username = data.username;
        this.password = data.password;
        this.localAuthDisabled = data.localAuthDisabled;
        this.sslOptions = data.sslOptions;
        this.wrappedModelControllerClient = data.wrappedModelControllerClient;
        this.isWrappedClient = data.wrappedModelControllerClient != null;

        if ((protocol == ManagementProtocol.HTTPS || protocol == ManagementProtocol.HTTPS_REMOTING
                || protocol == ManagementProtocol.REMOTE_HTTPS) && sslOptions == null) {
            throw new IllegalArgumentException("SSL must be configured when HTTPS or HTTPS_REMOTING protocol is used!");
        }
    }

    /**
     * A single shared instance of this class is passed through the entire builder invocation chain to accumulate
     * all the options.
     */
    private static final class Data {
        private boolean isStandalone;

        private boolean isDomain;
        private String defaultProfile;
        private String defaultHost;

        private String host;
        private int port;
        private boolean localDefault; // if true, then "host" and "port" above are not set

        private ManagementProtocol protocol; // often not set at all
        private int connectionTimeout;
        private int bootTimeout;

        private String username;
        private String password;

        private boolean localAuthDisabled;

        private SslOptions sslOptions;

        private ModelControllerClient wrappedModelControllerClient;
    }

    /** Connect to a standalone server. */
    public static ConnectionOnlineOptions standalone() {
        Data data = new Data();
        data.isStandalone = true;
        data.bootTimeout = 20000;
        return new ConnectionOnlineOptions(data);
    }

    /** Connect to a domain controller. */
    public static DomainOnlineOptions domain() {
        Data data = new Data();
        data.isDomain = true;
        data.bootTimeout = 120000;
        return new DomainOnlineOptions(data);
    }

    public static final class DomainOnlineOptions {
        private final Data data;

        private DomainOnlineOptions(Data data) {
            this.data = data;
        }

        /**
         * <p>Apply profile configuration changes to the {@code profile}. Optional, can only be called once.</p>
         *
         * <p>If set, operations on addresses {@code /subsystem=...} will be automatically performed against
         * an address {@code /profile=.../subsystem=...}. If not set, an exception will be thrown when
         * trying to execute an operation on address {@code /subsystem=...}.</p>
         */
        public DomainOnlineOptions forProfile(String profile) {
            assert data.isDomain;

            if (data.defaultProfile != null) {
                throw new IllegalStateException("Default profile was already set (" + data.defaultProfile + ")");
            }

            data.defaultProfile = profile;
            return this;
        }

        /**
         * <p>Apply host configuration changes to the {@code host}. Optional, can only be called once.</p>
         *
         * <p>If set, operations on addresses {@code /core-service=...} will be automatically performed against
         * an address {@code /host=.../core-service=...}. If not set, the operation will be performed against
         * the original address.</p>
         */
        public DomainOnlineOptions forHost(String host) {
            assert data.isDomain;

            if (data.defaultHost != null) {
                throw new IllegalStateException("Default host was already set (" + data.defaultHost + ")");
            }

            data.defaultHost = host;
            return this;
        }

        /** You want to call this in order to configure other options: host, port, etc. */
        public ConnectionOnlineOptions build() {
            return new ConnectionOnlineOptions(data);
        }
    }

    public static final class ConnectionOnlineOptions {
        private final Data data;

        private ConnectionOnlineOptions(Data data) {
            this.data = data;
        }

        /**
         * <p>Connect to {@code localhost} and use the default management port of the application server.
         * This is {@code 9990} by default, but if {@link OptionalOnlineOptions#ssl(SslOptions) ssl} is not null,
         * then it is 9993.
         * </p>
         *
         * <p>Alternatively, the {@link OptionalOnlineOptions#protocol(ManagementProtocol) protocol()} method
         * can be used, which takes precedence over the methods described above. When it is called, the default port
         * depends on chosen {@link ManagementProtocol}.</p>
         */
        public OptionalOnlineOptions localDefault() {
            data.localDefault = true;
            return new OptionalOnlineOptions(data);
        }

        /** Connect to {@code host}:{@code port}. */
        public OptionalOnlineOptions hostAndPort(String host, int port) {
            if (data.host != null) {
                throw new IllegalStateException("Host and port were already set");
            }
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("Host must be set");
            }
            if (port <= 0) {
                throw new IllegalArgumentException("Port must be set");
            }

            data.host = host;
            data.port = port;
            return new OptionalOnlineOptions(data);
        }

        /**
         * <p>The {@link org.wildfly.extras.creaper.core.online.OnlineManagementClient} that will be created will wrap
         * the provided {@link org.jboss.as.controller.client.ModelControllerClient modelControllerClient}.</p>
         *
         * <p><b>Note that doing this ties together the lifecycles of the {@code OnlineManagementClient} and
         * the wrapped {@code ModelControllerClient}!</b> If you close the {@code ModelControllerClient}, the
         * {@code OnlineManagementClient} becomes unusable. If you close the {@code OnlineManagementClient},
         * the wrapped {@code ModelControllerClient} will be closed as well.</p>
         *
         * <p>This also means that the new {@code OnlineManagementClient} <b>can't be {@code reconnect}ed</b>, which
         * is sometimes needed if server reload or restart is performed. Hence, use client wrapping with caution!</p>
         */
        public OnlineOptions wrap(ModelControllerClient modelControllerClient) {
            if (modelControllerClient == null) {
                throw new IllegalArgumentException("ModelControllerClient to be wrapped must be set");
            }

            data.wrappedModelControllerClient = modelControllerClient;
            return new OnlineOptions(data);
        }
    }

    public static final class OptionalOnlineOptions {
        private final Data data;

        private OptionalOnlineOptions(Data data) {
            this.data = data;
        }

        /** When connecting, use the {@code username} and {@code password} for auth. Optional. */
        public OptionalOnlineOptions auth(String username, String password) {
            if (data.username != null) {
                throw new IllegalStateException("Username and password were already set ("
                        + username + ")");
            }
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username must be set");
            }
            if (password == null) { // can be empty
                throw new IllegalArgumentException("Password must be set");
            }

            data.username = username;
            data.password = password;
            return this;
        }

        /** Disable local auth. Optional. */
        public OptionalOnlineOptions disableLocalAuth() {
            data.localAuthDisabled = true;
            return this;
        }

        /** SSL/TLS connection settings. Optional. */
        public OptionalOnlineOptions ssl(SslOptions sslOptions) {
            data.sslOptions = sslOptions;
            return this;
        }

        /**
         * <p>Timeout to use when connecting to the server. In milliseconds. Optional. A value {@code <= 0}
         * means "no timeout". By default, no timeout is used.</p>
         *
         * <p>This has two meanings. First, it's the maximum wait time used when connections to the server
         * are being refused. This can happen when the server process has just been started and the management
         * endpoint is not yet available. Second, it's the connection timeout passed
         * to {@link ModelControllerClient.Factory}.</p>
         */
        public OptionalOnlineOptions connectionTimeout(int timeoutInMillis) {
            if (timeoutInMillis <= 0) {
                timeoutInMillis = 0;
            }

            data.connectionTimeout = timeoutInMillis;
            return this;
        }

        /**
         * Timeout to use when waiting for the server to boot. In milliseconds. Optional. A value {@code <= 0}
         * means "no timeout". By default, a value of {@code 20000} (20 seconds) is used for a standalone server
         * and a value of {@code 120000} (2 minutes) is used for managed domain.
         */
        public OptionalOnlineOptions bootTimeout(int timeoutInMillis) {
            if (timeoutInMillis <= 0) {
                timeoutInMillis = 0;
            }

            data.bootTimeout = timeoutInMillis;
            return this;
        }

        /**
         * <p>When connecting, use the specified {@link ManagementProtocol protocol}. Optional.</p>
         *
         * <p>This also affects the <i>server port</i>, if {@link ConnectionOnlineOptions#localDefault() localDefault}
         * is used.</p>
         *
         * <p>WildFly uses the remoting protocol wrapped in HTTP (using the HTTP upgrade mechanism),
         * called {@code http-remoting}, or uses its secured version called {@code https-remoting}.
         *
         * <p>The server port is only affected by this method when
         * {@link ConnectionOnlineOptions#localDefault() localDefault} is used. When server port is specified directly
         * (using {@link ConnectionOnlineOptions#hostAndPort(String, int) hostAndPort}), this method doesn't change it.
         * </p>
         */
        public OptionalOnlineOptions protocol(ManagementProtocol protocol) {
            if (protocol == null) {
                throw new IllegalArgumentException("Management protocol must be set");
            }

            data.protocol = protocol;
            return this;
        }

        /** Build the final {@code OnlineOptions}. */
        public OnlineOptions build() {
            return new OnlineOptions(data);
        }
    }

    // ---

    ModelControllerClient createModelControllerClient() throws IOException {
        if (wrappedModelControllerClient != null) {
            return wrappedModelControllerClient;
        }

        CallbackHandler callbackHandler = null;
        Map<String, String> saslOptions = null;
        SSLContext sslContext = null;

        if (username != null) {
            callbackHandler = new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (Callback callback : callbacks) {
                        if (callback instanceof NameCallback) {
                            NameCallback ncb = (NameCallback) callback;
                            ncb.setName(username);
                        } else if (callback instanceof PasswordCallback) {
                            PasswordCallback pcb = (PasswordCallback) callback;
                            pcb.setPassword(password.toCharArray());
                        } else if (callback instanceof RealmCallback) {
                            RealmCallback rcb = (RealmCallback) callback;
                            rcb.setText(rcb.getDefaultText());
                        } else {
                            throw new UnsupportedCallbackException(callback);
                        }
                    }
                }
            };
        }

        if (localAuthDisabled) {
            saslOptions = Collections.singletonMap("SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER");
        }

        if (sslOptions != null) {
            sslContext = sslOptions.createSslContext();
        }

        ModelControllerClient modelControllerClient;

        if (protocol == ManagementProtocol.HTTP || protocol == ManagementProtocol.HTTPS) {
            modelControllerClient = new HttpModelControllerClient(host, port, username, password, connectionTimeout,
                    sslOptions);
            try {
                connectAndWaitUntilServerBoots(modelControllerClient, connectionTimeout, bootTimeout);
            } catch (Exception e) {
                modelControllerClient.close();

                if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof IllegalStateException) {
                    throw (IllegalStateException) e;
                } else {
                    throw new IllegalStateException(e);
                }
            }
            return modelControllerClient;
        }

        modelControllerClient = ModelControllerClient.Factory.create(new ModelControllerClientConfiguration.Builder()
                .setProtocol(protocol != null ? protocol.protocolName() : null)
                .setHostName(host)
                .setPort(port)
                .setSslContext(sslContext)
                .setConnectionTimeout(connectionTimeout)
                .setSaslOptions(saslOptions)
                .setHandler(callbackHandler).build());

        try {
            connectAndWaitUntilServerBoots(modelControllerClient, connectionTimeout, bootTimeout);
        } catch (Exception e) {
            modelControllerClient.close();

            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            } else {
                throw new IllegalStateException(e);
            }
        }

        return modelControllerClient;
    }

    private static void connectAndWaitUntilServerBoots(ModelControllerClient client, int connectionTimeoutInMillis,
                                                       int bootTimeoutInMillis)
            throws IOException, InterruptedException, TimeoutException {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.WHOAMI);
        op.get(Constants.OP_ADDR).setEmptyList();

        long endTime = System.currentTimeMillis() + connectionTimeoutInMillis;
        while (System.currentTimeMillis() < endTime) {
            try {
                client.execute(op);
                break;
            } catch (IOException e) {
                // server is probably not up yet, keep waiting
                Thread.sleep(100);
            }
        }

        endTime = System.currentTimeMillis() + bootTimeoutInMillis;
        while (System.currentTimeMillis() < endTime) {
            ModelNodeResult result = new ModelNodeResult(client.execute(op));

            if (result.isSuccess()) {
                return;
            }

            boolean stillBooting = false;

            String failureDescription = result.get(Constants.FAILURE_DESCRIPTION).asString();
            for (String code : Constants.RESULT_CODES_FOR_BOOT_IN_PROGRESS) {
                if (failureDescription.startsWith(code)) {
                    stillBooting = true;
                    break;
                }
            }

            if (stillBooting) {
                Thread.sleep(100);
            } else {
                // shouldn't happen
                throw new IllegalStateException("Unknown server state: " + failureDescription);
            }
        }

        ModelNodeResult result = new ModelNodeResult(client.execute(op));
        if (!result.isSuccess()) {
            throw new TimeoutException("Waiting for server to boot timed out");
        }
    }

    static {
        ManagementClient.OnlineClientFactory.set(new ManagementClient.OnlineClientFactory() {
            @Override
            protected OnlineManagementClient create(OnlineOptions options) throws IOException {
                return new OnlineManagementClientImpl(options);
            }

            @Override
            protected OnlineManagementClient createLazy(OnlineOptions options) {
                return new LazyOnlineManagementClient(options);
            }
        });
    }
}
