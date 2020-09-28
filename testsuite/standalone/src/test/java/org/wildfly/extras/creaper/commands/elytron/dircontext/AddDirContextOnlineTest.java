package org.wildfly.extras.creaper.commands.elytron.dircontext;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.elytron.Property;
import org.wildfly.extras.creaper.commands.elytron.authenticationclient.AddAuthenticationContext;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyManager;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyStore;
import org.wildfly.extras.creaper.commands.elytron.tls.AddServerSSLContext;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddDirContextOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_DIR_CONTEXT_NAME = "CreaperTestDirContext";
    private static final Address TEST_DIR_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS
            .and("dir-context", TEST_DIR_CONTEXT_NAME);
    private static final String TEST_DIR_CONTEXT_NAME2 = "CreaperTestDirContext2";
    private static final Address TEST_DIR_CONTEXT_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("dir-context", TEST_DIR_CONTEXT_NAME2);

    private static final String TEST_SERVER_SSL_CONTEXT = "CreaperTestSslContext";
    private static final Address TEST_SERVER_SSL_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS
            .and("server-ssl-context", TEST_SERVER_SSL_CONTEXT);

    private static final String TEST_AUTHENTICATION_CONTEXT_NAME = "CreaperTestAuthenticationContext";
    private static final Address TEST_AUTHENTICATION_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS
            .and("authentication-context", TEST_AUTHENTICATION_CONTEXT_NAME);

    private static final String TEST_KEY_STORE_NAME = "CreaperTestKeyStore";
    private static final Address TEST_KEY_STORE_NAME_ADDRESS = SUBSYSTEM_ADDRESS
            .and("key-store", TEST_KEY_STORE_NAME);
    private static final String TEST_KEY_STORE_TYPE = "JKS";
    private static final String TEST_KEY_STORE_PASSWORD = "password";
    private static final String TEST_KEY_PASSWORD = "password";
    private static final String TEST_KEY_MNGR_NAME = "CreaperTestKeyManager";
    private static final Address TEST_KEY_MNGR_NAME_ADDRESS = SUBSYSTEM_ADDRESS
            .and("key-manager", TEST_KEY_MNGR_NAME);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_DIR_CONTEXT_ADDRESS);
        ops.removeIfExists(TEST_DIR_CONTEXT_ADDRESS2);
        ops.removeIfExists(TEST_SERVER_SSL_CONTEXT_ADDRESS);
        ops.removeIfExists(TEST_KEY_MNGR_NAME_ADDRESS);
        ops.removeIfExists(TEST_KEY_STORE_NAME_ADDRESS);
        ops.removeIfExists(TEST_AUTHENTICATION_CONTEXT_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addDirContext() throws Exception {
        AddDirContext addDirContext = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .build();
        client.apply(addDirContext);

        assertTrue("Dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS));
    }

    @Test
    public void addDirContexts() throws Exception {
        AddDirContext addDirContext = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .build();

        AddDirContext addDirContext2 = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME2)
                .url("localhost")
                .build();

        client.apply(addDirContext);
        client.apply(addDirContext2);

        assertTrue("Dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS));
        assertTrue("Second dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS2));
    }

    @Test
    public void addFullDirContext() throws Exception {
        AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                .type(TEST_KEY_STORE_TYPE)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_STORE_PASSWORD)
                        .build())
                .build();
        client.apply(addKeyStore);
        AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        client.apply(addKeyManager);
        AddServerSSLContext addServerSSLContext = new AddServerSSLContext.Builder(TEST_SERVER_SSL_CONTEXT)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();
        client.apply(addServerSSLContext);
        AddAuthenticationContext addAuthenticationContext
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .build();
        client.apply(addAuthenticationContext);

        AddDirContext addDirContext = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .authenticationLevel(AddDirContext.AuthenticationLevel.STRONG)
                .enableConnectionPooling(false)
                .referralMode(AddDirContext.ReferralMode.THROW)
                .authenticationContext(TEST_AUTHENTICATION_CONTEXT_NAME)
                .connectionTimeout(10)
                .readTimeout(20)
                .module("org.wildfly.security.elytron-private")
                .addProperties(new Property("property1", "value1"),
                        new Property("property2", "value2"))
                .build();

        AddDirContext addDirContext2 = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME2)
                .url("localhost")
                .authenticationLevel(AddDirContext.AuthenticationLevel.STRONG)
                .enableConnectionPooling(false)
                .principal("test-principal")
                .referralMode(AddDirContext.ReferralMode.THROW)
                .connectionTimeout(10)
                .readTimeout(20)
                .module("org.wildfly.security.elytron-private")
                .sslContext(TEST_SERVER_SSL_CONTEXT)
                .addProperties(new Property("property1", "value1"),
                        new Property("property2", "value2"))
                .build();

        client.apply(addDirContext, addDirContext2);
        assertTrue("Dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS));


        final String throwString;
        if (client.version().lessThan(ServerVersion.VERSION_9_0_0)) {
            throwString = "THROW";
        } else {
            // Since WildFly 15, created https://issues.redhat.com/browse/WFLY-13935
            throwString = "throw";
        }

        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "url", "localhost");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "authentication-level", "STRONG");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "enable-connection-pooling", "false");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "referral-mode", throwString);
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "authentication-context", TEST_AUTHENTICATION_CONTEXT_NAME);
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "connection-timeout", "10");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "read-timeout", "20");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "module", "org.wildfly.security.elytron-private");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "properties.property1", "value1");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "properties.property2", "value2");

        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "url", "localhost");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "authentication-level", "STRONG");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "enable-connection-pooling", "false");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "principal", "test-principal");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "referral-mode", throwString);
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "connection-timeout", "10");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "read-timeout", "20");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "module", "org.wildfly.security.elytron-private");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "ssl-context", TEST_SERVER_SSL_CONTEXT);
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "properties.property1", "value1");
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS2, "properties.property2", "value2");

    }

    @Test
    public void addDirContextWithCredentialReference() throws Exception {
        AddDirContext addDirContext = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();

        client.apply(addDirContext);
        assertTrue("Dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS));

        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "credential-reference.clear-text", "somePassword");
    }

    @Test(expected = CommandFailedException.class)
    public void addDirContextNotAllowed() throws Exception {
        AddDirContext addDirContext = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .build();

        AddDirContext addDirContext2 = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .build();

        client.apply(addDirContext);
        assertTrue("Dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS));
        client.apply(addDirContext2);
        fail("Dir Context CreaperTestDirContext already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDirContextAllowed() throws Exception {
        AddDirContext addDirContext = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .build();

        AddDirContext addDirContext2 = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("http://www.example.com/")
                .replaceExisting()
                .build();

        client.apply(addDirContext);
        assertTrue("Dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS));
        client.apply(addDirContext2);
        assertTrue("Dir context should be created", ops.exists(TEST_DIR_CONTEXT_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_DIR_CONTEXT_ADDRESS, "url", "http://www.example.com/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDirContext_nullName() throws Exception {
        new AddDirContext.Builder(null)
                .url("localhost")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDirContext_emptyName() throws Exception {
        new AddDirContext.Builder("")
                .url("localhost")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDirContext_nullUrl() throws Exception {
        new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url(null)
                .build();
        fail("Creating command with null url should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDirContext_emptyUrl() throws Exception {
        new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("")
                .build();
        fail("Creating command with empty url should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDirContext_authenticationContextAndCredentialReference() throws Exception {
        new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
                .url("localhost")
                .authenticationContext(TEST_AUTHENTICATION_CONTEXT_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();
        fail("Creating command with both authenticationContext and credentialReference should throw exception");
    }

}
