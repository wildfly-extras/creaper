package org.wildfly.extras.creaper.commands.undertow;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.CreateServerSSLContext;
import org.wildfly.extras.creaper.commands.socketbindings.AddSocketBinding;
import org.wildfly.extras.creaper.commands.socketbindings.RemoveSocketBinding;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.security.KeyPairAndCertificate;
import org.wildfly.extras.creaper.test.WildFlyTests;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@Category(WildFlyTests.class)
@RunWith(Arquillian.class)
public class AddUndertowListenerOnlineTest {
    private static final String TEST_SOCKET_BINDING = "creaper-test";
    private static final String TEST_LISTENER_NAME = "test-listener";
    private static final String TEST_PASSWORD = "p4sSw0rD!";

    private static final Address DEFAULT_SERVER_ADDRESS = Address.subsystem("undertow")
            .and("server", UndertowConstants.DEFAULT_SERVER_NAME);

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private OnlineManagementClient client;
    private Operations ops;
    private Administration admin;

    @Before
    public void connect() throws Exception {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        assumeTrue("The test requires Undertow that supports HTTP/2 options on listener, which is available since WildFly 9",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_3_0_0));
        ops = new Operations(client);
        admin = new Administration(client);

        client.apply(new AddSocketBinding.Builder(TEST_SOCKET_BINDING).port(12345).build());
        admin.reloadIfRequired();
    }

    @After
    public void close() throws Exception {
        client.apply(new RemoveSocketBinding(TEST_SOCKET_BINDING));
        admin.reloadIfRequired();

        client.close();
    }

    @Test
    public void addHttpConnector_commandSucceeds() throws Exception {
        client.apply(new AddUndertowListener.HttpBuilder(TEST_LISTENER_NAME, TEST_SOCKET_BINDING).build());

        assertTrue(ops.exists(DEFAULT_SERVER_ADDRESS.and("http-listener", TEST_LISTENER_NAME)));
        ops.readAttribute(DEFAULT_SERVER_ADDRESS.and("http-listener", TEST_LISTENER_NAME), "socket-binding")
                .assertSuccess();

        client.apply(new RemoveUndertowListener.Builder(UndertowListenerType.HTTP_LISTENER, TEST_LISTENER_NAME)
                .forDefaultServer());
        admin.reloadIfRequired();
        assertFalse(ops.exists(DEFAULT_SERVER_ADDRESS.and("http-listener", TEST_LISTENER_NAME)));
    }

    @Test
    public void addHttpsConnector_commandSucceeds() throws Exception {
        Assume.assumeFalse("Legacy security was removed in WildFly 25.",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0));
        String alias = "creaper";
        File keystoreFile = tmp.newFile();
        KeyStore keyStore = KeyPairAndCertificate.generateSelfSigned("Creaper").toKeyStore(alias, TEST_PASSWORD);
        keyStore.store(new FileOutputStream(keystoreFile), TEST_PASSWORD.toCharArray());

        String realmName = "CreaperRealm";

        client.apply(new AddHttpsSecurityRealm.Builder(realmName)
                .keystorePath(keystoreFile.getAbsolutePath())
                .keystorePassword(TEST_PASSWORD)
                .alias(alias)
                .truststorePath(keystoreFile.getAbsolutePath())
                .truststorePassword(TEST_PASSWORD)
                .build());

        client.apply(new AddUndertowListener.HttpsBuilder(TEST_LISTENER_NAME, TEST_SOCKET_BINDING)
                .securityRealm(realmName)
                .build());

        assertTrue(ops.exists(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME)));
        ops.readAttribute(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME), "socket-binding")
                .assertSuccess();

        client.apply(new RemoveUndertowListener.Builder(UndertowListenerType.HTTPS_LISTENER, TEST_LISTENER_NAME)
                .forDefaultServer());
        admin.reloadIfRequired();
        assertFalse(ops.exists(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME)));

        client.apply(new RemoveHttpsSecurityRealm(realmName));
        admin.reloadIfRequired();
    }

    @Test
    public void addHttpsConnectorElytron_commandSucceeds() throws Exception {
        // requires Elytron which is available since WildFly 11
        Assume.assumeTrue("Elytron is available since WildFly 11.",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_5_0_0));

        String alias = "creaper";
        File keystoreFile = tmp.newFile();
        KeyStore keyStore = KeyPairAndCertificate.generateSelfSigned("Creaper").toKeyStore(alias, TEST_PASSWORD);
        keyStore.store(new FileOutputStream(keystoreFile), TEST_PASSWORD.toCharArray());

        String sslContextName = "CreaperSslContext";

        client.apply(new CreateServerSSLContext.Builder(sslContextName)
                .keyStorePath(keystoreFile.getAbsolutePath())
                .keyStorePassword(TEST_PASSWORD)
                .keyStoreAlias(alias)
                .keyPassword(TEST_PASSWORD)
                .trustStorePath(keystoreFile.getAbsolutePath())
                .trustStorePassword(TEST_PASSWORD)
                .build());

        client.apply(new AddUndertowListener.HttpsBuilder(TEST_LISTENER_NAME, TEST_SOCKET_BINDING)
                .sslContext(sslContextName)
                .build());

        assertTrue(ops.exists(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME)));
        ops.readAttribute(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME), "socket-binding")
                .assertSuccess();

        client.apply(new RemoveUndertowListener.Builder(UndertowListenerType.HTTPS_LISTENER, TEST_LISTENER_NAME)
                .forDefaultServer());
        admin.reloadIfRequired();
        assertFalse(ops.exists(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME)));

        ops.remove(Address.subsystem("elytron").and("server-ssl-context", sslContextName)).assertSuccess();
        admin.reloadIfRequired();
    }

    @Test
    public void addSecurityRealm_withoutTruststore_commandSucceeds() throws Exception {
        Assume.assumeFalse("Legacy security was removed in WildFly 25.",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0));
        String alias = "creaper";
        File keystoreFile = tmp.newFile();
        KeyStore keyStore = KeyPairAndCertificate.generateSelfSigned("Creaper").toKeyStore(alias, TEST_PASSWORD);
        keyStore.store(new FileOutputStream(keystoreFile), TEST_PASSWORD.toCharArray());

        String realmName = "CreaperRealm";

        client.apply(new AddHttpsSecurityRealm.Builder(realmName)
                .keystorePath(keystoreFile.getAbsolutePath())
                .keystorePassword(TEST_PASSWORD)
                .alias(alias)
                .build());
        assertTrue(ops.exists(Address.coreService("management").and("security-realm", realmName)));

        client.apply(new RemoveHttpsSecurityRealm(realmName));
        admin.reloadIfRequired();
    }


    @Test
    public void addAjpConnector_commandSucceeds() throws Exception {
        client.apply(new AddUndertowListener.AjpBuilder(TEST_LISTENER_NAME, TEST_SOCKET_BINDING).build());

        assertTrue(ops.exists(DEFAULT_SERVER_ADDRESS.and("ajp-listener", TEST_LISTENER_NAME)));
        ops.readAttribute(DEFAULT_SERVER_ADDRESS.and("ajp-listener", TEST_LISTENER_NAME), "socket-binding")
                .assertSuccess();

        client.apply(new RemoveUndertowListener.Builder(UndertowListenerType.AJP_LISTENER, TEST_LISTENER_NAME)
                .forDefaultServer());
        admin.reloadIfRequired();
        assertFalse(ops.exists(DEFAULT_SERVER_ADDRESS.and("ajp-listener", TEST_LISTENER_NAME)));
    }
}
