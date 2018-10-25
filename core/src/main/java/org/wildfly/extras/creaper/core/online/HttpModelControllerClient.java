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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
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
import org.jboss.threads.AsyncFutureTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Controller provides execution of {@link ModelNode} or {@link Operation} over HTTP.
 * Controller does not inform about any progress, therefore, there is no point in using {@link OperationMessageHandler}.
 * If an error occurs (server does not respond 401 or header does not contain WWW-Authenticate field after
 * first request) {@code IllegalStateException is thrown} (if username and password is provided)
 * Execution on {@link Operation} is allowed with <b>no attachments</b>.
 */
final class HttpModelControllerClient implements ModelControllerClient {
    private static final int NO_TIMEOUT = 0;

    private final String url;
    private final RequestConfig requestConfig;
    private final Registry<ConnectionSocketFactory> registry;
    private final CloseableHttpClient client;
    private final int timeout;
    private ExecutorService executorService;

    HttpModelControllerClient(String host, int port, String username, String password, int timeoutMillis,
                              SslOptions ssl) throws IOException {
        // timeout configuration
        this.timeout = timeoutMillis;
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if (timeoutMillis != NO_TIMEOUT) {
            requestConfigBuilder
                    .setConnectTimeout(timeoutMillis)
                    .setSocketTimeout(timeoutMillis);
        }
        requestConfig = requestConfigBuilder.build();

        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
        if (ssl != null) {
            url = "https://" + host + ":" + port + "/management";
            SSLConnectionSocketFactory sslConnectionSocketFactory;
            if (ssl.hostnameVerification) {
                sslConnectionSocketFactory = new SSLConnectionSocketFactory(ssl.createSslContext());
            } else {
                sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                        ssl.createSslContext(), NoopHostnameVerifier.INSTANCE);
            }
            registryBuilder.register("https", sslConnectionSocketFactory);
        } else {
            url = "http://" + host + ":" + port + "/management";
            registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        }
        registry = registryBuilder.build();

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

    @Override
    public AsyncFuture<ModelNode> executeAsync(ModelNode modelNode, OperationMessageHandler handler) {
        ExecutorService executor = obtainExecutorService();
        Future<ModelNode> submit = executor.submit(new ModelNodeFromModelNodeCallable(modelNode));
        ModelNode result = getFutureResult(submit);
        return new HttpModelControllerClientAsyncFutureTask(executor, result);
    }

    @Override
    public AsyncFuture<ModelNode> executeAsync(Operation operation, OperationMessageHandler handler) {
        ExecutorService executor = obtainExecutorService();
        Future<ModelNode> submit = executor.submit(new ModelNodeFromOperationCallable(operation));
        ModelNode result = getFutureResult(submit);
        return new HttpModelControllerClientAsyncFutureTask(executor, result);
    }

    @Override
    public AsyncFuture<OperationResponse> executeOperationAsync(Operation operation, OperationMessageHandler handler) {
        ExecutorService executor = obtainExecutorService();
        Future<OperationResponse> submit = executor.submit(new OperationResponseFromOperationCallable(operation));
        OperationResponse result = getFutureResult(submit);
        return new HttpModelControllerClientAsyncFutureTask(executor, result);
    }

    @Override
    public void close() throws IOException {
        client.close();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private ExecutorService obtainExecutorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
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
                .setConnectionManager(new PoolingHttpClientConnectionManager(registry))
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpPost post = new HttpPost(url);
        post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        // this is not necessary, but without this server produce parse error exception when authentication is disabled
        // which may confuse user
        post.setEntity(new StringEntity("[]"));

        CloseableHttpResponse response = null;
        String content;
        try {
            response = defaultHttpClient.execute(post);
            content = EntityUtils.toString(response.getEntity());
        } finally {
            if (response != null) {
                response.close();
            }
            defaultHttpClient.close();
        }
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw new IllegalStateException(String.format("Failed to obtain management realm name. Server responded %d instead of %d. Isn't server authentication turned off while username and password set? Content: %s",
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, HttpStatus.SC_UNAUTHORIZED, content));
        }
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
            throw new IllegalStateException(String.format("Failed to obtain management realm name. Server responded %d instead of %d. Content: %s",
                    response.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED, content));
        }
        if (!response.containsHeader(HttpHeaders.WWW_AUTHENTICATE)) {
            throw new IllegalStateException("Failed to obtain management realm name. Missing WWW-Authenticate header in server response.");
        }
        for (HeaderElement el : response.getHeaders(HttpHeaders.WWW_AUTHENTICATE)[0].getElements()) {
            if (el.getName().equals("Digest realm")) {
                return el.getValue();
            }
        }
        throw new IllegalStateException("Failed to obtain management realm name. Digest realm not found in WWW-Authenticate header.");
    }

    private <T> T getFutureResult(Future<T> submit) {
        T get = null;
        try {
            if (timeout == NO_TIMEOUT) {
                get = submit.get();
            } else {
                get = submit.get(timeout, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        } catch (TimeoutException ex) {
            throw new RuntimeException(ex);
        }
        return get;
    }

    private static class HttpModelControllerClientAsyncFutureTask<T> extends AsyncFutureTask<T> {

        HttpModelControllerClientAsyncFutureTask(Executor executor, T result) {
            super(executor);
            super.setResult(result);
        }

    }

    private class ModelNodeFromModelNodeCallable implements Callable<ModelNode> {

        private final ModelNode modelNode;

        ModelNodeFromModelNodeCallable(ModelNode modelNode) {
            this.modelNode = modelNode;
        }

        @Override
        public ModelNode call() throws Exception {
            return execute(modelNode);
        }
    }

    private class ModelNodeFromOperationCallable implements Callable<ModelNode> {

        private final Operation operation;

        ModelNodeFromOperationCallable(Operation operation) {
            this.operation = operation;
        }

        @Override
        public ModelNode call() throws Exception {
            return execute(operation);
        }
    }

    private class OperationResponseFromOperationCallable implements Callable<OperationResponse> {

        private final Operation operation;

        OperationResponseFromOperationCallable(Operation operation) {
            this.operation = operation;
        }

        @Override
        public OperationResponse call() throws Exception {
            return executeOperation(operation, null);
        }
    }
}
