package org.wildfly.extras.creaper.commands.elytron;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.tls.AbstractAddSSLContextOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class CreateServerSSLContextOnlineTest extends AbstractAddSSLContextOnlineTest {

    private static final String SERVER_SSL_CONTEXT_PROTOCOL = "TLSv1.2";
    private static final String PASSWORD = "secret";
    private static final String SERVER_SSL_CONTEXT_NAME = "CreaperTestServerSSLContext";
    private static final Address SERVER_SSL_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS.and("server-ssl-context",
            SERVER_SSL_CONTEXT_NAME);
    private static final Address KEY_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("key-store",
            "key-store-name_" + SERVER_SSL_CONTEXT_NAME);
    private static final Address KEY_MANAGER_ADDRESS = SUBSYSTEM_ADDRESS.and("key-manager",
            "key-manager-name_" + SERVER_SSL_CONTEXT_NAME);
    private static final Address TRUST_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("key-store",
            "trust-store-name_" + SERVER_SSL_CONTEXT_NAME);
    private static final Address TRUST_MANAGER_ADDRESS = SUBSYSTEM_ADDRESS.and("trust-manager",
            "trust-manager-name_" + SERVER_SSL_CONTEXT_NAME);


    private static final String SERVER_SSL_CONTEXT_NAME2 = "CreaperTestServerSSLContext2";
    private static final Address SERVER_SSL_CONTEXT_ADDRESS2 = SUBSYSTEM_ADDRESS.and("server-ssl-context",
            SERVER_SSL_CONTEXT_NAME2);
    private static final Address KEY_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-store",
            "key-store-name_" + SERVER_SSL_CONTEXT_NAME2);
    private static final Address KEY_MANAGER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-manager",
            "key-manager-name_" + SERVER_SSL_CONTEXT_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(SERVER_SSL_CONTEXT_ADDRESS);
        ops.removeIfExists(KEY_MANAGER_ADDRESS);
        ops.removeIfExists(KEY_STORE_ADDRESS);
        ops.removeIfExists(TRUST_MANAGER_ADDRESS);
        ops.removeIfExists(TRUST_STORE_ADDRESS);
        ops.removeIfExists(SERVER_SSL_CONTEXT_ADDRESS2);
        ops.removeIfExists(KEY_MANAGER_ADDRESS2);
        ops.removeIfExists(KEY_STORE_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleServerSSLContext() throws Exception {
        CreateServerSSLContext createServerSSLContext = new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(PASSWORD)
                .keyPassword(PASSWORD)
                .build();
        assertFalse("The server ssl context should not exist", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        client.apply(createServerSSLContext);
        assertTrue("Server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
    }

    @Test
    public void addTwoSimpleServerSSLContexts() throws Exception {
        CreateServerSSLContext createServerSSLContext = new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(PASSWORD)
                .keyPassword(PASSWORD)
                .build();
        CreateServerSSLContext createServerSSLContext2 = new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME2)
                .keyStorePassword(PASSWORD)
                .keyPassword(PASSWORD)
                .build();

        assertFalse("The server ssl context should not exist", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        assertFalse("The server ssl context should not exist", ops.exists(SERVER_SSL_CONTEXT_ADDRESS2));

        client.apply(createServerSSLContext);
        client.apply(createServerSSLContext2);

        assertTrue("Server SSL context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        assertTrue("Server SSL context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateServerSSLContextNotAllowed() throws Exception {
        CreateServerSSLContext createServerSSLContext = new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(PASSWORD)
                .keyPassword(PASSWORD)
                .build();
        CreateServerSSLContext createServerSSLContext2 = new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(PASSWORD)
                .keyPassword(PASSWORD)
                .build();
        client.apply(createServerSSLContext);
        assertTrue("The server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        client.apply(createServerSSLContext2);
        fail("Server ssl context is already configured, exception should be thrown");
    }

    @Test
    public void addFullServerSSLContext() throws Exception {
        CreateServerSSLContext createServerSSLContext = new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(PASSWORD)
                .keyPassword(PASSWORD)
                .cipherSuiteFilter("ALL")
                .protocols(SERVER_SSL_CONTEXT_PROTOCOL)
                .maximumSessionCacheSize(0)
                .sessionTimeout(0)
                .needClientAuth(true)
                .wantClientAuth(true)
                .authenticationOptional(true)
                .securityDomain("ApplicationDomain")
                .algorithm("PKIX")
                .keyStoreAlias("alias")
                .keyStorePath("/path")
                .keyStoreRelativeTo("jboss.server.config.dir")
                .keyStoreRequired(false)
                .keyStoreType("JKS")
                .trustStoreAlias("alias")
                .trustStorePassword(PASSWORD)
                .trustStorePath("/path")
                .trustStoreRelativeTo("jboss.server.config.dir")
                .trustStoreRequired(false)
                .build();
        client.apply(createServerSSLContext);
        assertTrue("The server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));

        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "cipher-suite-filter", "ALL");
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "maximum-session-cache-size", "0");
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "session-timeout", "0");
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "protocols", Arrays.asList(SERVER_SSL_CONTEXT_PROTOCOL));
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "need-client-auth", "true");
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "want-client-auth", "true");
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "authentication-optional", "true");
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "security-domain", "ApplicationDomain");
        checkAttribute(KEY_STORE_ADDRESS, "credential-reference.clear-text", PASSWORD);
        checkAttribute(KEY_STORE_ADDRESS, "alias-filter", "alias");
        checkAttribute(KEY_STORE_ADDRESS, "path", "/path");
        checkAttribute(KEY_STORE_ADDRESS, "relative-to", "jboss.server.config.dir");
        checkAttribute(KEY_STORE_ADDRESS, "required", "false");
        checkAttribute(KEY_STORE_ADDRESS, "type", "JKS");
        checkAttribute(TRUST_STORE_ADDRESS, "credential-reference.clear-text", PASSWORD);
        checkAttribute(TRUST_STORE_ADDRESS, "alias-filter", "alias");
        checkAttribute(TRUST_STORE_ADDRESS, "path", "/path");
        checkAttribute(TRUST_STORE_ADDRESS, "relative-to", "jboss.server.config.dir");
        checkAttribute(TRUST_STORE_ADDRESS, "required", "false");
        checkAttribute(TRUST_STORE_ADDRESS, "type", "JKS");
        checkAttribute(KEY_MANAGER_ADDRESS, "credential-reference.clear-text", PASSWORD);
        checkAttribute(KEY_MANAGER_ADDRESS, "algorithm", "PKIX");
        checkAttribute(TRUST_MANAGER_ADDRESS, "algorithm", "PKIX");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_nullName() throws Exception {
        new CreateServerSSLContext.Builder(null)
                .keyStorePassword(PASSWORD)
                .keyPassword(PASSWORD)
                .build();
        fail("Creating command with null server SSL context name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_emptyName() throws Exception {
        new CreateServerSSLContext.Builder("")
            .keyStorePassword(PASSWORD)
            .keyPassword(PASSWORD)
            .build();
        fail("Creating command with empty server ssl context name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_nullKeyStorePassword() throws Exception {
        new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(null)
                .keyPassword(PASSWORD)
                .build();
        fail("Creating command with null key store password should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_emptyKeyStorePassword() throws Exception {
        new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword("")
                .keyPassword(PASSWORD)
                .build();
        fail("Creating command with empty key store password should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_nullKeyPassword() throws Exception {
        new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(PASSWORD)
                .keyPassword(null)
                .build();
        fail("Creating command with null key password should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_emptyKeyPassword() throws Exception {
        new CreateServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyStorePassword(PASSWORD)
                .keyPassword("")
                .build();
        fail("Creating command with empty key password should throw exception");
    }

}
