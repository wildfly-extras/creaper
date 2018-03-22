package org.wildfly.extras.creaper.commands.elytron.authenticationclient;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyManager;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyStore;
import org.wildfly.extras.creaper.commands.elytron.tls.AddServerSSLContext;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddAuthenticationContextOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_AUTHENTICATION_CONTEXT_NAME = "CreaperTestAuthenticationContext";
    private static final Address TEST_AUTHENTICATION_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS
            .and("authentication-context", TEST_AUTHENTICATION_CONTEXT_NAME);
    private static final String TEST_AUTHENTICATION_CONTEXT_NAME2 = "CreaperTestAuthenticationContext2";
    private static final Address TEST_AUTHENTICATION_CONTEXT_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("authentication-context", TEST_AUTHENTICATION_CONTEXT_NAME2);

    private static final String TEST_AUTHENTICATION_CONFIGURATION_NAME = "CreaperTestAuthenticationConfiguration";
    private static final Address TEST_AUTHENTICATION_CONFIGURATION_ADDRESS = SUBSYSTEM_ADDRESS
            .and("authentication-configuration", TEST_AUTHENTICATION_CONFIGURATION_NAME);

    private static final String TEST_SERVER_SSL_CONTEXT_NAME = "CreaperTestSslContext";
    private static final Address TEST_SERVER_SSL_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS
            .and("server-ssl-context", TEST_SERVER_SSL_CONTEXT_NAME);

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
        ops.removeIfExists(TEST_AUTHENTICATION_CONTEXT_ADDRESS);
        ops.removeIfExists(TEST_AUTHENTICATION_CONTEXT_ADDRESS2);
        ops.removeIfExists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS);
        ops.removeIfExists(TEST_SERVER_SSL_CONTEXT_ADDRESS);
        ops.removeIfExists(TEST_KEY_MNGR_NAME_ADDRESS);
        ops.removeIfExists(TEST_KEY_STORE_NAME_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleAuthenticationContext() throws Exception {
        AddAuthenticationContext addAuthenticationContext
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .build();

        client.apply(addAuthenticationContext);

        assertTrue("Authentication Context should be created", ops.exists(TEST_AUTHENTICATION_CONTEXT_ADDRESS));
    }

    @Test
    public void addTwoAuthenticationContexts() throws Exception {
        AddAuthenticationContext addAuthenticationContext
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .build();

        AddAuthenticationContext addAuthenticationContext2
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME2)
                .build();

        client.apply(addAuthenticationContext);
        client.apply(addAuthenticationContext2);

        assertTrue("Authentication Context should be created", ops.exists(TEST_AUTHENTICATION_CONTEXT_ADDRESS));
        assertTrue("Second Authentication Context should be created",
                ops.exists(TEST_AUTHENTICATION_CONTEXT_ADDRESS2));
    }

    @Test
    public void addFullAuthenticationContext() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();
        client.apply(addAuthenticationConfiguration);

        AddAuthenticationContext addAuthenticationContext2
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME2)
                .build();
        client.apply(addAuthenticationContext2);

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

        AddServerSSLContext addServerSSLContext = new AddServerSSLContext.Builder(TEST_SERVER_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();
        client.apply(addServerSSLContext);

        AddAuthenticationContext addAuthenticationContext
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .extend(TEST_AUTHENTICATION_CONTEXT_NAME2)
                .addMatchRules(new AddAuthenticationContext.MatchRuleBuilder()
                        .authenticationConfiguration(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                        .sslContext(TEST_SERVER_SSL_CONTEXT_NAME)
                        .matchAbstractType("someAbstractType1")
                        .matchAbstractTypeAuthority("someAbstractTypeAuthority1")
                        .matchHost("someHost1")
                        .matchLocalSecurityDomain("someLocalSecurityDomain1")
                        .matchNoUser(true)
                        .matchPath("somePath1")
                        .matchPort(12345)
                        .matchProtocol("someProtocol1")
                        .matchUrn("someUrn1")
                        .build(),
                        new AddAuthenticationContext.MatchRuleBuilder()
                        .authenticationConfiguration(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                        .sslContext(TEST_SERVER_SSL_CONTEXT_NAME)
                        .matchAbstractType("someAbstractType2")
                        .matchAbstractTypeAuthority("someAbstractTypeAuthority2")
                        .matchHost("someHost2")
                        .matchLocalSecurityDomain("someLocalSecurityDomain2")
                        .matchPath("somePath2")
                        .matchPort(12346)
                        .matchProtocol("someProtocol2")
                        .matchUrn("someUrn2")
                        .matchUser("someUser2")
                        .build())
                .build();
        client.apply(addAuthenticationContext);

        assertTrue("Authentication Context should be created", ops.exists(TEST_AUTHENTICATION_CONTEXT_ADDRESS));

        checkAttribute("extends", TEST_AUTHENTICATION_CONTEXT_NAME2);
        checkAttribute("match-rules[0].authentication-configuration", TEST_AUTHENTICATION_CONFIGURATION_NAME);
        checkAttribute("match-rules[0].ssl-context", TEST_SERVER_SSL_CONTEXT_NAME);
        checkAttribute("match-rules[0].match-abstract-type", "someAbstractType1");
        checkAttribute("match-rules[0].match-abstract-type-authority", "someAbstractTypeAuthority1");
        checkAttribute("match-rules[0].match-host", "someHost1");
        checkAttribute("match-rules[0].match-local-security-domain", "someLocalSecurityDomain1");
        checkAttribute("match-rules[0].match-no-user", "true");
        checkAttribute("match-rules[0].match-path", "somePath1");
        checkAttribute("match-rules[0].match-port", "12345");
        checkAttribute("match-rules[0].match-protocol", "someProtocol1");
        checkAttribute("match-rules[0].match-urn", "someUrn1");
        checkAttribute("match-rules[1].authentication-configuration", TEST_AUTHENTICATION_CONFIGURATION_NAME);
        checkAttribute("match-rules[1].ssl-context", TEST_SERVER_SSL_CONTEXT_NAME);
        checkAttribute("match-rules[1].match-abstract-type", "someAbstractType2");
        checkAttribute("match-rules[1].match-abstract-type-authority", "someAbstractTypeAuthority2");
        checkAttribute("match-rules[1].match-host", "someHost2");
        checkAttribute("match-rules[1].match-local-security-domain", "someLocalSecurityDomain2");
        checkAttribute("match-rules[1].match-path", "somePath2");
        checkAttribute("match-rules[1].match-port", "12346");
        checkAttribute("match-rules[1].match-protocol", "someProtocol2");
        checkAttribute("match-rules[1].match-urn", "someUrn2");
        checkAttribute("match-rules[1].match-user", "someUser2");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAuthenticationContextNotAllowed() throws Exception {
        AddAuthenticationContext addAuthenticationContext
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .addMatchRules(new AddAuthenticationContext.MatchRuleBuilder()
                        .matchHost("someHost1")
                        .build())
                .build();

        AddAuthenticationContext addAuthenticationContext2
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .addMatchRules(new AddAuthenticationContext.MatchRuleBuilder()
                        .matchHost("someHost2")
                        .build())
                .build();

        client.apply(addAuthenticationContext);
        assertTrue("Authentication Context should be created", ops.exists(TEST_AUTHENTICATION_CONTEXT_ADDRESS));
        client.apply(addAuthenticationContext2);
        fail("Authentication Context CreaperTestAuthenticationContext already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistAuthenticationContextAllowed() throws Exception {
        AddAuthenticationContext addAuthenticationContext
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .addMatchRules(new AddAuthenticationContext.MatchRuleBuilder()
                        .matchHost("someHost1")
                        .build())
                .build();

        AddAuthenticationContext addAuthenticationContext2
                = new AddAuthenticationContext.Builder(TEST_AUTHENTICATION_CONTEXT_NAME)
                .addMatchRules(new AddAuthenticationContext.MatchRuleBuilder()
                        .matchHost("someHost2")
                        .build())
                .replaceExisting()
                .build();

        client.apply(addAuthenticationContext);
        assertTrue("Authentication Context should be created", ops.exists(TEST_AUTHENTICATION_CONTEXT_ADDRESS));
        client.apply(addAuthenticationContext2);
        checkAttribute("match-rules[0].match-host", "someHost2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationContext_nullName() throws Exception {
        new AddAuthenticationContext.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationContext_emptyName() throws Exception {
        new AddAuthenticationContext.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_AUTHENTICATION_CONTEXT_ADDRESS, attribute, expectedValue);
    }
}
