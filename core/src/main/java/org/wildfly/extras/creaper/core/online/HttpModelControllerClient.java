package org.wildfly.extras.creaper.core.online;

import org.apache.http.HeaderElement;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.as.controller.client.OperationResponse;
import org.jboss.dmr.ModelNode;
import org.jboss.threads.AsyncFuture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Controller provides execution of {@link ModelNode} or {@link Operation} over HTTP.
 * Asynchronous execution is not provided. Controller does not inform about any progress, therefore, there is no point
 * in using {@link OperationMessageHandler}.
 * If an error occurs (server does not respond 401 or header does not contain WWW-Authenticate field after
 * first request) {@code IllegalStateException is thrown} (if username and password is provided)
 * Execution on {@link Operation} is allowed with <b>no attachments</b>.
 */
final class HttpModelControllerClient implements ModelControllerClient {
    private static final int NO_TIMEOUT = 0;

    private final String url;
    private final RequestConfig requestConfig;
    private final CloseableHttpClient client;

    HttpModelControllerClient(String host, int port, String username, String password) throws IOException {
        this(host, port, username, password, NO_TIMEOUT);
    }

    HttpModelControllerClient(String host, int port, String username, String password, int timeoutMillis)
            throws IOException {
        url = "http://" + host + ":" + port + "/management";
        // timeout configuration
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if (timeoutMillis != NO_TIMEOUT) {
            requestConfigBuilder
                    .setConnectTimeout(timeoutMillis)
                    .setSocketTimeout(timeoutMillis);
        }
        requestConfig = requestConfigBuilder.build();
        // registry of factories
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (username != null && password != null) {
            credentialsProvider.setCredentials(
                    new AuthScope(host, port, getManagementRealm(url), AuthSchemes.DIGEST),
                    new UsernamePasswordCredentials(username, password));
        }
        client = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager(registry))
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Override
    public ModelNode execute(ModelNode modelNode) throws IOException {
        ModelNode result;
        CloseableHttpResponse response = client.execute(buildRequest(modelNode));
        try {
            result = parseResponse(response);
        } finally {
            response.close();
        }
        return result;
    }

    @Override
    public ModelNode execute(Operation operation) throws IOException {
        if (!operation.getInputStreams().isEmpty()) {
            throw new IllegalStateException("Operation has one or more attachments which is not allowed.");
        }
        return execute(operation.getOperation());
    }

    @Override
    public ModelNode execute(ModelNode modelNode, OperationMessageHandler handler) throws IOException {
        return execute(modelNode);
    }

    @Override
    public ModelNode execute(Operation operation, OperationMessageHandler handler) throws IOException {
        return execute(operation);
    }

    @Override
    public OperationResponse executeOperation(Operation operation, OperationMessageHandler handler) throws IOException {
        return OperationResponse.Factory.createSimple(execute(operation));
    }

    /**
     * <b>Not supported!</b>
     */
    @Override
    public AsyncFuture<ModelNode> executeAsync(ModelNode modelNode, OperationMessageHandler handler) {
        throw new UnsupportedOperationException("Asynchronous execution is not supported by " + getClass().getName());
    }

    /**
     * <b>Not supported!</b>
     */
    @Override
    public AsyncFuture<ModelNode> executeAsync(Operation operation, OperationMessageHandler handler) {
        throw new UnsupportedOperationException("Asynchronous execution is not supported by " + getClass().getName());
    }

    /**
     * <b>Not supported!</b>
     */
    @Override
    public AsyncFuture<OperationResponse> executeOperationAsync(Operation operation, OperationMessageHandler handler) {
        throw new UnsupportedOperationException("Asynchronous execution is not supported by " + getClass().getName());
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    private ModelNode parseResponse(CloseableHttpResponse response) throws IOException {
        ModelNode result;
        String content = EntityUtils.toString(response.getEntity());
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK || status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            result = ModelNode.fromJSONString(content);
        } else {
            throw new RuntimeException(String.format("Server responded %s%nMessage:%n%s", status, content));
        }
        return result;
    }

    private HttpPost buildRequest(ModelNode model) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(url);
        request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setEntity(new StringEntity(model.toJSONString(true)));
        return request;
    }

    private String getManagementRealm(String url) throws IOException {
        // we need client without credentials
        CloseableHttpClient defaultHttpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpPost post = new HttpPost(url);
        post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = null;
        try {
            response = defaultHttpClient.execute(post);
            response.close();
        } finally {
            defaultHttpClient.close();
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
            throw new IllegalStateException(String.format("Server responded with %d instead of %d after first request.",
                    response.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED));
        }
        if (!response.containsHeader(HttpHeaders.WWW_AUTHENTICATE)) {
            throw new IllegalStateException("Missing WWW-Authenticate header in server response after first request.");
        }
        for (HeaderElement el : response.getHeaders(HttpHeaders.WWW_AUTHENTICATE)[0].getElements()) {
            if (el.getName().equals("Digest realm")) {
                return el.getValue();
            }
        }
        throw new IllegalStateException("Digest realm not found in WWW-Authenticate header.");
    }
}
