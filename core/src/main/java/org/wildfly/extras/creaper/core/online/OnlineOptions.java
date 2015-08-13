package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.ModelControllerClient;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerType;

import javax.net.ssl.SSLContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * This basically follows the builder pattern, but ensures on the type level that everything that must be set is
 * actually set. That means that the IDE guides you through the configuration and once the code compiles, you can be
 * sure you didn't forget anything important. The downside is that the implementation is a bit involved and exposes
 * quite a number of public types.
 */
public final class OnlineOptions {
    private static final String CREAPER_WILDFLY = "creaper.wildfly";

    public final boolean isStandalone;

    public final boolean isDomain;
    public String defaultProfile;
    public String defaultHost;
    /** @deprecated use {@code defaulProfile} instead, this will be removed before 1.0 */
    public final String domainProfile;
    /** @deprecated use {@code defaulhost} instead, this will be removed before 1.0 */
    public final String domainHost;

    final String host;
    final int port;
    private final ServerType serverType; // this can be "null" for unknown server type
    private final int connectionTimeout;

    private final String username;
    private final String password;

    private final boolean localAuthDisabled;

    private final ModelControllerClient wrappedModelControllerClient;
    final boolean isWrappedClient; // see OnlineManagementClientImpl.reconnect

    private OnlineOptions(Data data) {
        if (data.serverType == null && System.getProperty(CREAPER_WILDFLY) != null) {
            data.serverType = ServerType.WILDFLY;
            // if the server type wasn't set manually and the system property isn't set,
            // it _doesn't_ mean that it's AS7! see also OptionalOnlineOptions.serverType below
        }

        if (data.localDefault) {
            data.host = "localhost";
            data.port = data.serverType == ServerType.WILDFLY ? 9990 : 9999;
        }

        this.isStandalone = data.isStandalone;
        this.isDomain = data.isDomain;
        this.defaultProfile = data.defaultProfile;
        this.defaultHost = data.defaultHost;
        this.domainProfile = data.defaultProfile;
        this.domainHost = data.defaultHost;
        this.host = data.host;
        this.port = data.port;
        this.serverType = data.serverType;
        this.connectionTimeout = data.connectionTimeout;
        this.username = data.username;
        this.password = data.password;
        this.localAuthDisabled = data.localAuthDisabled;
        this.wrappedModelControllerClient = data.wrappedModelControllerClient;
        this.isWrappedClient = data.wrappedModelControllerClient != null;
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

        private ServerType serverType; // often not set at all
        private int connectionTimeout;

        private String username;
        private String password;

        private boolean localAuthDisabled;

        private ModelControllerClient wrappedModelControllerClient;
    }

    /** Connect to a standalone server. */
    public static ConnectionOnlineOptions standalone() {
        Data data = new Data();
        data.isStandalone = true;
        return new ConnectionOnlineOptions(data);
    }

    /** Connect to a domain controller. */
    public static DomainOnlineOptions domain() {
        Data data = new Data();
        data.isDomain = true;
        return new DomainOnlineOptions(data);
    }

    public static final class DomainOnlineOptions {
        private final Data data;

        private DomainOnlineOptions(Data data) {
            this.data = data;
        }

        /** Apply profile configuration changes to the {@code profile}. Optional, can only be called once.  */
        public DomainOnlineOptions forProfile(String profile) {
            assert data.isDomain;

            if (data.defaultProfile != null) {
                throw new IllegalStateException("Profile was already set (" + data.defaultProfile + ")");
            }

            data.defaultProfile = profile;
            return this;
        }

        /** Apply host configuration changes to the {@code host}. Optional, can only be called once.  */
        public DomainOnlineOptions forHost(String host) {
            assert data.isDomain;

            if (data.defaultHost != null) {
                throw new IllegalStateException("Host was already set (" + data.defaultHost + ")");
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
         * This is {@code 9999} by default (JBoss AS 7), and if the system property {@code creaper.wildfly} is defined,
         * it changes to 9990 (WildFly). This makes it easy to run the same code against both AS7 and WildFly
         * only by defining a single system property.</p>
         *
         * <p>Alternatively, the {@link OptionalOnlineOptions#serverType(ServerType) serverType()} method can be used,
         * which takes precedence over the methods described above. When it is called, the default port depends on
         * chosen {@link ServerType}.</p>
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

        /** Timeout to enforce when connecting to the server. Optional, must be {@code > 0}. */
        public OptionalOnlineOptions connectionTimeout(int connectionTimeout) {
            if (connectionTimeout <= 0) {
                throw new IllegalArgumentException("Connection timeout must be > 0");
            }

            data.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * <p>When connecting, assume that the server is of given {@link ServerType type}. Optional.</p>
         *
         * <p>This method affects two things:</p>
         *
         * <ul>
         *     <li>the <i>connection protocol</i> that the client libraries will use to connect to the server</li>
         *     <li>the <i>server port</i>, if {@link ConnectionOnlineOptions#localDefault() localDefault} is used</li>
         * </ul>
         *
         * <p>AS7 uses a native remoting protocol, while WildFly uses the remoting protocol wrapped in HTTP
         * (using the HTTP upgrade mechanism). When the client libraries on classpath match the server version,
         * they should choose the correct protocol automatically, so in this situation, this method is not required.
         * However, when using a single set of client libraries for all server versions (which is possible, because
         * the client libraries should be backward compatible), this method must be used to specify the type
         * of the server.</p>
         *
         * <p>The server port is only affected by this method when
         * {@link ConnectionOnlineOptions#localDefault() localDefault} is used. When server port is specified directly
         * (using {@link ConnectionOnlineOptions#hostAndPort(String, int) hostAndPort}), this method doesn't change it.
         * </p>
         *
         * <p>If the {@code creaper.wildfly} system property is set (because of
         * {@link ConnectionOnlineOptions#localDefault() localDefault}), it is also used as a signal that the server
         * type is WildFly, but this method has a priority. When the system property is not set, it <i>doesn't</i>
         * mean that the server type is AS7; we simply don't know.</p>
         */
        public OptionalOnlineOptions serverType(ServerType serverType) {
            if (serverType == null) {
                throw new IllegalArgumentException("Server type must be set");
            }

            data.serverType = serverType;
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

        // the variant with the "protocol" parameter exists since WildFly 8, and if it is available, it is preferred
        // to the protocol-less variant available in JBoss AS 7, because we want to choose the protocol dynamically
        //
        // also, in all versions of WildFly 8 and all versions of WildFly Core before 1.0.0.Beta4, the protocol-less
        // variant is missing (see WFCORE-623)
        try {
            Method createMethod = ModelControllerClient.Factory.class.getMethod("create",
                    String.class,          // protocol -- new in WildFly 8
                    String.class,          // host
                    int.class,             // port
                    CallbackHandler.class, // callbackHandler
                    SSLContext.class,      // sslContext
                    int.class,             // connectionTimeout
                    Map.class              // saslOptions
            );

            String protocol = null;
            if (serverType == ServerType.WILDFLY) {
                protocol = "http-remoting";
            }
            if (serverType == ServerType.AS7) {
                protocol = "remote";
            }

            return (ModelControllerClient) createMethod.invoke(null, // static method
                    protocol, host, port, callbackHandler, null, connectionTimeout, saslOptions);
        } catch (NoSuchMethodException e) {
            if (serverType == ServerType.WILDFLY) {
                // user asks for WildFly, but the client library is from AS7, this can't work
                throw new IllegalStateException("Server type is WildFly (either ServerType.WILDFLY was used or the '"
                        + CREAPER_WILDFLY + "' system property was set), but client libraries are AS7-only");
            }

            return ModelControllerClient.Factory.create(host, port, callbackHandler, null, connectionTimeout,
                    saslOptions);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
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
