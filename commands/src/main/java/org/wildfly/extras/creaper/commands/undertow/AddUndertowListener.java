package org.wildfly.extras.creaper.commands.undertow;

import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

/**
 * Command which creates HTTP/HTTPS/AJP listener in the Undertow subsystem.
 */
public final class AddUndertowListener implements OnlineCommand {
    private final UndertowListenerType listenerType;
    private String listenerName;
    private String serverName;

    private Boolean allowEncodedSlash;
    private Boolean allowEqualsInCookiesValues;
    private Boolean alwaysSetKeepAlive;
    private Boolean bufferPipelinedData;
    private String bufferPool;
    private Boolean decodeUrl;
    private Boolean enabled;
    private Integer maxBufferedRequestSize;
    private Integer maxCookies;
    private Integer maxHeaderSize;
    private Integer maxHeaders;
    private Integer maxParameters;
    private Long maxPostSize;
    private Integer noRequestTimeout;
    private Integer readTimeout;
    private Integer receiveBuffer;
    private Boolean recordRequestStartTime;
    private Integer requestParseTimeout;
    private Boolean resolvePeerAddress;
    private Integer sendBuffer;
    private String socketBinding;
    private Integer tcpBacklog;
    private Boolean tcpKeepAlive;
    private String urlCharset;
    private String worker;
    private Integer writeTimeout;

    // http
    private Boolean certificateForwarding;
    private Boolean proxyAddressForwarding;

    // http & https
    private Boolean enableHttp2;

    // http & ajp
    private String redirectSocket;

    // https
    private Boolean enableSpdy;
    private String enabledCipherSuites;
    private String enabledProtocols;
    private String securityRealm;
    private SslVerifyClient verifyClient;

    // ajp
    private String scheme;

    private AddUndertowListener(HttpBuilder builder) {
        this.listenerType = UndertowListenerType.HTTP_LISTENER;
        this.certificateForwarding = builder.certificateForwarding;
        this.proxyAddressForwarding = builder.proxyAddressForwarding;
        this.redirectSocket = builder.redirectSocket;
        this.enableHttp2 = builder.enableHttp2;
        initCommonOptions(builder);
    }

    private AddUndertowListener(HttpsBuilder builder) {
        this.listenerType = UndertowListenerType.HTTPS_LISTENER;
        this.enableHttp2 = builder.enableHttp2;
        this.enableSpdy = builder.enableSpdy;
        this.enabledCipherSuites = builder.enabledCipherSuites;
        this.enabledProtocols = builder.enabledProtocols;
        this.securityRealm = builder.securityRealm;
        this.verifyClient = builder.verifyClient;
        initCommonOptions(builder);
    }

    private AddUndertowListener(AjpBuilder builder) {
        this.listenerType = UndertowListenerType.AJP_LISTENER;
        this.scheme = builder.scheme;
        this.redirectSocket = builder.redirectSocket;
        initCommonOptions(builder);
    }

    private void initCommonOptions(UndertowListenerBuilder builder) {
        this.serverName = builder.serverName;
        this.listenerName = builder.listenerName;
        this.allowEncodedSlash = builder.allowEncodedSlash;
        this.allowEqualsInCookiesValues = builder.allowEqualsInCookiesValues;
        this.alwaysSetKeepAlive = builder.alwaysSetKeepAlive;
        this.bufferPipelinedData = builder.bufferPipelinedData;
        this.bufferPool = builder.bufferPool;
        this.decodeUrl = builder.decodeUrl;
        this.enabled = builder.enabled;
        this.maxBufferedRequestSize = builder.maxBufferedRequestSize;
        this.maxCookies = builder.maxCookies;
        this.maxHeaderSize = builder.maxHeaderSize;
        this.maxHeaders = builder.maxHeaders;
        this.maxParameters = builder.maxParameters;
        this.maxPostSize = builder.maxPostSize;
        this.noRequestTimeout = builder.noRequestTimeout;
        this.readTimeout = builder.readTimeout;
        this.receiveBuffer = builder.receiveBuffer;
        this.recordRequestStartTime = builder.recordRequestStartTime;
        this.requestParseTimeout = builder.requestParseTimeout;
        this.resolvePeerAddress = builder.resolvePeerAddress;
        this.sendBuffer = builder.sendBuffer;
        this.socketBinding = builder.socketBinding;
        this.tcpBacklog = builder.tcpBacklog;
        this.tcpKeepAlive = builder.tcpKeepAlive;
        this.urlCharset = builder.urlCharset;
        this.worker = builder.worker;
        this.writeTimeout = builder.writeTimeout;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws CommandFailedException, IOException {
        // Undertow is available since WildFly 8, but some options (e.g. HTTP/2) are only available
        // since WildFly 9; for now, restricting to WildFly 9 and above
        ctx.serverVersion.assertAtLeast(ManagementVersion.VERSION_3_0_0);

        Operations ops = new Operations(ctx.client);
        Address listenerAddress = Address.subsystem("undertow").and("server", serverName)
                .and(listenerType.listenerTypeName(), listenerName);

        Values params = Values.empty()
                .andOptional("allow-encoded-slash", allowEncodedSlash)
                .andOptional("allow-equals-in-cookie-value", allowEqualsInCookiesValues)
                .andOptional("always-set-keep-alive", alwaysSetKeepAlive)
                .andOptional("buffer-pipelined-data", bufferPipelinedData)
                .andOptional("buffer-pool", bufferPool)
                .andOptional("decode-url", decodeUrl)
                .andOptional("enabled", enabled)
                .andOptional("max-buffered-request-size", maxBufferedRequestSize)
                .andOptional("max-cookies", maxCookies)
                .andOptional("max-header-size", maxHeaderSize)
                .andOptional("max-headers", maxHeaders)
                .andOptional("max-parameters", maxParameters)
                .andOptional("max-post-size", maxPostSize)
                .andOptional("no-request-timeout", noRequestTimeout)
                .andOptional("read-timeout", readTimeout)
                .andOptional("receive-buffer", receiveBuffer)
                .andOptional("record-request-start-time", recordRequestStartTime)
                .andOptional("request-parse-timeout", requestParseTimeout)
                .andOptional("resolve-peer-address", resolvePeerAddress)
                .andOptional("send-buffer", sendBuffer)
                .and("socket-binding", socketBinding)
                .andOptional("tcp-backlog", tcpBacklog)
                .andOptional("tcp-keep-alive", tcpKeepAlive)
                .andOptional("url-charset", urlCharset)
                .andOptional("worker", worker)
                .andOptional("write-timeout", writeTimeout);

        if (listenerType == UndertowListenerType.HTTP_LISTENER || listenerType == UndertowListenerType.AJP_LISTENER) {
            params = params.andOptional("redirect-socket", redirectSocket);
        }

        if (listenerType == UndertowListenerType.HTTPS_LISTENER || listenerType == UndertowListenerType.HTTP_LISTENER) {
            params = params.andOptional("enable-http2", enableHttp2);
        }

        switch (listenerType) {
            case HTTP_LISTENER:
                params = params.andOptional("certificate-forwarding", certificateForwarding)
                        .andOptional("proxy-address-forwarding", proxyAddressForwarding);
                break;
            case HTTPS_LISTENER:
                params = params.andOptional("enable-spdy", enableSpdy)
                        .andOptional("enabled-cipher-suites", enabledCipherSuites)
                        .andOptional("enabled-protocols", enabledProtocols)
                        .and("security-realm", securityRealm)
                        .andOptional("verify-client", verifyClient != null ? verifyClient.name() : null);
                break;
            case AJP_LISTENER:
                params = params.andOptional("scheme", scheme);
                break;
            default:
                throw new CommandFailedException("Unknown listener type");
        }
        ops.add(listenerAddress, params);
    }

    @Override
    public String toString() {
        return "AddUndertowListener " + listenerName + " of type " + listenerType.listenerTypeName()
                + " for server " + serverName;
    }

    /**
     * Builder for building common attributes for all listeners.
     */
    abstract static class UndertowListenerBuilder<THIS extends UndertowListenerBuilder<THIS>> {
        protected final String listenerName;
        protected final String serverName;

        protected Boolean allowEncodedSlash;
        protected Boolean allowEqualsInCookiesValues;
        protected Boolean alwaysSetKeepAlive;
        protected Boolean bufferPipelinedData;
        protected String bufferPool;
        protected Boolean decodeUrl;
        protected Boolean enabled;
        protected Integer maxBufferedRequestSize;
        protected Integer maxCookies;
        protected Integer maxHeaderSize;
        protected Integer maxHeaders;
        protected Integer maxParameters;
        protected Long maxPostSize;
        protected Integer noRequestTimeout;
        protected Integer readTimeout;
        protected Integer receiveBuffer;
        protected Boolean recordRequestStartTime;
        protected Integer requestParseTimeout;
        protected Boolean resolvePeerAddress;
        protected Integer sendBuffer;
        protected String socketBinding;
        protected Integer tcpBacklog;
        protected Boolean tcpKeepAlive;
        protected String urlCharset;
        protected String worker;
        protected Integer writeTimeout;

        /**
         * Creates builder for listener with specified name {@code listenerName} for specified undertow server
         * {@code serverName} using specified socket binding {@code socketBinding}
         */
        public UndertowListenerBuilder(String listenerName, String serverName, String socketBinding) {
            if (listenerName == null) {
                throw new IllegalArgumentException("Name of the listener must be specified as non null value");
            }
            if (serverName == null) {
                throw new IllegalArgumentException("Name of the undertow server must be specified as non null value");
            }
            if (socketBinding == null) {
                throw new IllegalArgumentException("The socket binding must be specified as non null value");
            }

            this.socketBinding = socketBinding;
            this.listenerName = listenerName;
            this.serverName = serverName;
        }

        /**
         * Creates builder for listener with specified name {@code listenerName} for default undertow server
         * using specified socket binding {@code socketBinding}
         */
        public UndertowListenerBuilder(String listenerName, String socketBinding) {
            if (listenerName == null) {
                throw new IllegalArgumentException("Name of the listener must be specified as non null value");
            }
            if (socketBinding == null) {
                throw new IllegalArgumentException("The socket binding must be specified as non null value");
            }

            this.listenerName = listenerName;
            this.socketBinding = socketBinding;
            this.serverName = UndertowConstants.DEFAULT_SERVER_NAME;
        }

        /**
         * Sets whether request which comes in with encoded / characters (i.e. %2F), will these be decoded.
         */
        public final THIS allowEncodedSlash(boolean allowEncodedSlash) {
            this.allowEncodedSlash = allowEncodedSlash;
            return (THIS) this;
        }

        /**
         * Defines whether Undertow will allow non-escaped equals characters in unquoted cookie values.
         * Unquoted cookie values may not contain equals characters.
         * If present the value ends before the equals sign. The remainder of the cookie value will be dropped.
         */
        public THIS allowEqualsInCookiesValues(boolean allowEqualsInCookiesValues) {
            this.allowEqualsInCookiesValues = allowEqualsInCookiesValues;
            return (THIS) this;
        }

        /**
         * Defines whether a Connection: keep-alive header will be added to responses, even when it is not strictly
         * required by the specification.
         */
        public THIS alwaysSetKeepAlive(boolean alwaysSetKeepAlive) {
            this.alwaysSetKeepAlive = alwaysSetKeepAlive;
            return (THIS) this;
        }

        /**
         * Defines whether there should be buffered pipelined requests
         */
        public THIS bufferPipelinedData(boolean bufferPipelinedData) {
            this.bufferPipelinedData = bufferPipelinedData;
            return (THIS) this;
        }

        /**
         * Defines the AJP listeners buffer pool
         */
        public THIS bufferPool(String bufferPool) {
            this.bufferPool = bufferPool;
            return (THIS) this;
        }

        /**
         * If this is true then the parser will decode the URL and query parameters using the selected character
         * encoding (UTF-8 by default).
         * If this is false they will not be decoded. This will allow a later handler to decode them into whatever
         * charset is desired.
         */
        public THIS decodeUrl(boolean decodeUrl) {
            this.decodeUrl = decodeUrl;
            return (THIS) this;
        }

        /**
         * Defines whether the connector should be started on startup.
         */
        public THIS enabled(boolean enabled) {
            this.enabled = enabled;
            return (THIS) this;
        }

        /**
         * Defines maximum size of a buffered request, in bytesRequests are not usually buffered,
         * the most common case is when performing SSL renegotiation for a POST request,
         * and the post data must be fully buffered in order to perform the renegotiation.
         */
        public THIS maxBufferedRequestSize(int maxBufferedRequestSize) {
            this.maxBufferedRequestSize = maxBufferedRequestSize;
            return (THIS) this;
        }

        /**
         * Defines maximum number of cookies that will be parsed. This is used to protect against hash vulnerabilities.
         */
        public THIS maxCookies(int maxCookies) {
            this.maxCookies = maxCookies;
            return (THIS) this;
        }

        /**
         * Defines maximum size in bytes of a http request header.
         */
        public THIS maxHeaderSize(int maxHeaderSize) {
            this.maxHeaderSize = maxHeaderSize;
            return (THIS) this;
        }

        /**
         * Defines maximum number of headers that will be parsed. This is used to protect against hash vulnerabilities.
         */
        public THIS maxHeaders(int maxHeaders) {
            this.maxHeaders = maxHeaders;
            return (THIS) this;
        }

        /**
         * Defines The maximum number of parameters that will be parsed.
         * This is used to protect against hash vulnerabilities. This applies to both query parameters and to POST data,
         * but is not cumulative (i.e. you can potentially have max parameters * 2 total parameters).
         */
        public THIS maxParameters(int maxParameters) {
            this.maxParameters = maxParameters;
            return (THIS) this;
        }

        /**
         * Defines maximum size of a post that will be accepted
         */
        public THIS maxPostSize(long maxPostSize) {
            this.maxPostSize = maxPostSize;
            return (THIS) this;
        }

        /**
         * Defines the length of time in milliseconds that the connection can be idle before it is closed by the
         * container.
         */
        public THIS noRequestTimeout(int noRequestTimeout) {
            this.noRequestTimeout = noRequestTimeout;
            return (THIS) this;
        }

        /**
         * Defines a read timeout for a socket, in milliseconds. If the given amount of time elapses without
         * a successful read taking place, the socket's next read will throw a ReadTimeoutException.
         */
        public THIS readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return (THIS) this;
        }

        /**
         * The receive buffer size.
         */
        public THIS receiveBuffer(int receiveBuffer) {
            this.receiveBuffer = receiveBuffer;
            return (THIS) this;
        }

        /**
         * Defines whether Undertow will record the request start time, to allow for request time to be logged.
         * This has a small but measurable performance impact.
         */
        public THIS recordRequestStartTime(boolean recordRequestStartTime) {
            this.recordRequestStartTime = recordRequestStartTime;
            return (THIS) this;
        }

        /**
         * Defines maximum amount of time (in milliseconds) that can be spent parsing the request.
         */
        public THIS requestParseTimeout(int requestParseTimeout) {
            this.requestParseTimeout = requestParseTimeout;
            return (THIS) this;
        }

        /**
         * Defines whether host dns lookup is enabled.
         */
        public THIS resolvePeerAddress(boolean resolvePeerAddress) {
            this.resolvePeerAddress = resolvePeerAddress;
            return (THIS) this;
        }

        /**
         * Defines send buffer size.
         */
        public THIS sendBuffer(int sendBuffer) {
            this.sendBuffer = sendBuffer;
            return (THIS) this;
        }

        /**
         * Configures a server with the specified backlog.
         */
        public THIS tcpBacklog(int tcpBacklog) {
            this.tcpBacklog = tcpBacklog;
            return (THIS) this;
        }

        /**
         * Defines whether channel should send TCP keep-alive messages in an implementation-dependent manner.
         */
        public THIS tcpKeepAlive(boolean tcpKeepAlive) {
            this.tcpKeepAlive = tcpKeepAlive;
            return (THIS) this;
        }

        /**
         * Defines URL charset.
         */
        public THIS urlCharset(String urlCharset) {
            this.urlCharset = urlCharset;
            return (THIS) this;
        }

        /**
         * Defines the listener XNIO worker
         */
        public THIS worker(String worker) {
            this.worker = worker;
            return (THIS) this;
        }

        /**
         * Configure a write timeout for a socket, in milliseconds.
         * If the given amount of time elapses without a successful write taking place,
         * the socket's next write will throw a WriteTimeoutException.
         */
        public THIS writeTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
            return (THIS) this;
        }
    }

    /**
     * <p>Builder for adding an HTTP listener.</p>
     * <p>
     * Some details about the attributes can be found
     * at http://wildscribe.github.io/Wildfly/9.0.0.Beta1/subsystem/undertow/server/http-listener/index.html
     * </p>
     */
    public static final class HttpBuilder extends UndertowListenerBuilder<HttpBuilder> {
        private Boolean certificateForwarding;
        private Boolean proxyAddressForwarding;
        private String redirectSocket;
        private Boolean enableHttp2;

        public HttpBuilder(String listenerName, String serverName, String socketBinding) {
            super(listenerName, serverName, socketBinding);
        }

        public HttpBuilder(String listenerName, String socketBinding) {
            super(listenerName, socketBinding);
        }

        /** Assumes socket binding called {@code http}. */
        public HttpBuilder(String listenerName) {
            super(listenerName, "http");
        }

        /**
         * Enables HTTP2 support for this listener.
         */
        public HttpBuilder enableHttp2(boolean enableHttp2) {
            this.enableHttp2 = enableHttp2;
            return this;
        }

        /**
         * Defines whether certificate forwarding should be enabled.
         * If this is enabled then the listener will take the certificate from the SSL_CLIENT_CERT attribute.
         * This should only be enabled if behind a proxy, and the proxy is configured to always set these headers.
         */
        public HttpBuilder certificateForwarding(boolean certificateForwarding) {
            this.certificateForwarding = certificateForwarding;
            return this;
        }

        /**
         * Enables x-forwarded-host and similar headers and set a remote ip address and hostname
         */
        public HttpBuilder proxyAddressForwarding(Boolean proxyAddressForwarding) {
            this.proxyAddressForwarding = proxyAddressForwarding;
            return this;
        }

        /**
         * Defines socket binding port which shall be used if this listener is supporting non-SSL requests,
         * and a request is received for which a matching requires SSL transport, undertow will automatically redirect
         * the request to the socket binding defined by this option.
         */
        public HttpBuilder redirectSocket(String redirectSocket) {
            this.redirectSocket = redirectSocket;
            return this;
        }

        public AddUndertowListener build() {
            return new AddUndertowListener(this);
        }
    }

    /**
     * <p>Builder for adding an HTTPS listener</p>
     * <p>
     * Some details about the attributes can be found
     * at http://wildscribe.github.io/Wildfly/9.0.0.Beta1/subsystem/undertow/server/https-listener/index.html
     * </p>
     */
    public static final class HttpsBuilder extends UndertowListenerBuilder<HttpsBuilder> {
        private Boolean enableHttp2;
        private Boolean enableSpdy;
        private String enabledCipherSuites;
        private String enabledProtocols;
        private String securityRealm;
        private SslVerifyClient verifyClient;

        public HttpsBuilder(String listenerName, String serverName, String socketBinding) {
            super(listenerName, serverName, socketBinding);
        }

        public HttpsBuilder(String listenerName, String socketBinding) {
            super(listenerName, socketBinding);
        }

        /** Assumes socket binding called {@code https}. */
        public HttpsBuilder(String listenerName) {
            super(listenerName, "https");
        }

        /**
         * Enables HTTP2 support for this listener.
         */
        public HttpsBuilder enableHttp2(boolean enableHttp2) {
            this.enableHttp2 = enableHttp2;
            return this;
        }

        /**
         * Enables SPDY support for this listener.
         */
        public HttpsBuilder enableSpdy(boolean enableSpdy) {
            this.enableSpdy = enableSpdy;
            return this;
        }

        /**
         * Defines Enabled SSL ciphers.
         */
        public HttpsBuilder enabledCipherSuites(String enabledCipherSuites) {
            this.enabledCipherSuites = enabledCipherSuites;
            return this;
        }

        /**
         * Defines SSL protocols.
         */
        public HttpsBuilder enabledProtocols(String enabledProtocols) {
            this.enabledProtocols = enabledProtocols;
            return this;
        }

        /**
         * <p>Defines which security realm should be used by the listener.</p>
         * <p/>
         * <p>
         * Note, there is also created {@see AddHttpsSecurityRealm} allowing to easily create security realm with
         * specified name
         * </p>
         */
        public HttpsBuilder securityRealm(String securityRealm) {
            this.securityRealm = securityRealm;
            return this;
        }

        /**
         * Defines desired SSL client authentication mode for SSL channels
         */
        public HttpsBuilder verifyClient(SslVerifyClient sslVerifyClient) {
            this.verifyClient = sslVerifyClient;
            return this;
        }

        public AddUndertowListener build() {
            return new AddUndertowListener(this);
        }
    }

    /**
     * <p>Builder for adding an AJP listener.</p>
     * <p>
     * Some details about the attributes can be found
     * at http://wildscribe.github.io/Wildfly/9.0.0.Beta1/subsystem/undertow/server/ajp-listener/index.html
     * </p>
     */
    public static final class AjpBuilder extends UndertowListenerBuilder<AjpBuilder> {
        private String redirectSocket;
        private String scheme;

        public AjpBuilder(String listenerName, String serverName, String socketBinding) {
            super(listenerName, serverName, socketBinding);
        }

        public AjpBuilder(String listenerName, String socketBinding) {
            super(listenerName, socketBinding);
        }

        /** Assumes socket binding called {@code ajp}. */
        public AjpBuilder(String listenerName) {
            super(listenerName, "ajp");
        }

        /**
         * Defines socket binding port which shall be used if this listener is supporting non-SSL requests,
         * and a request is received for which a matching requires SSL transport, undertow will automatically redirect
         * the request to the socket binding defined by this option.
         */
        public AjpBuilder redirectSocket(String redirectSocket) {
            this.redirectSocket = redirectSocket;
            return this;
        }

        /**
         * Defines the listener scheme, can be HTTP or HTTPS.
         * By default the scheme will be taken from the incoming AJP request.
         */
        public AjpBuilder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public AddUndertowListener build() {
            return new AddUndertowListener(this);
        }
    }
}
