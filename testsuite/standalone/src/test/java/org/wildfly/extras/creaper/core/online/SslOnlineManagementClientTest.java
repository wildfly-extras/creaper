package org.wildfly.extras.creaper.core.online;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.wildfly.extras.creaper.commands.foundation.offline.ConfigurationFileBackup;
import org.wildfly.extras.creaper.commands.management.AddHttpManagementInterface;
import org.wildfly.extras.creaper.commands.management.AddHttpsManagementSecurityRealm;
import org.wildfly.extras.creaper.commands.management.AddNativeManagementInterface;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.wildfly.extras.creaper.security.KeyPairAndCertificate;
import org.wildfly.extras.creaper.test.ManualTests;
import org.wildfly.extras.creaper.test.WildFlyTests;

/**
 * This test <b>needs</b> a manually-controlled Arquillian container.
 *
 * <p>WildFly test. For AS7, there is no way to have SSL only secured management interface and to manage the server
 * with Arquillian.</p>
 *
 * @see org.wildfly.extras.creaper.core.online.operations.admin.ReloadWhenRestartRequiredTest
 */
@RunAsClient
@Category({ManualTests.class, WildFlyTests.class})
public class SslOnlineManagementClientTest extends OnlineManagementClientTest {

    private static final String SECURITY_REALM = "SslSecuredTestRealm";

    private static final String SERVER_KEY_ALIAS = "serverKeyAlias";
    private static final String SERVER_KEY_PASSWORD = "serverKeyPassword";
    private static final String SERVER_CERTIFICATE_ALIAS = "serverCertificateAlias";
    private static final String SERVER_CERTIFICATE_PASSWORD = "serverCertificatePassword";
    private static final String CLIENT_KEY_ALIAS = "clientKeyAlias";
    private static final String CLIENT_KEY_PASSWORD = "clientKeyPassword";
    private static final String CLIENT_CERTIFICATE_ALIAS = "clientCertificateAlias";
    private static final String CLIENT_CERTIFICATE_PASSWORD = "clientCertificatePassword";

    private static final ConfigurationFileBackup CONFIGURATION_BACKUP = new ConfigurationFileBackup();
    private static OfflineManagementClient offlineClient;

    private static File serverKeyStoreFile;
    private static File serverTrustStoreFile;
    private static File clientKeyStoreFile;
    private static File clientTrustStoreFile;
    private static SslOptions sslOptions;


    @ArquillianResource
    private ContainerController controller;


    @BeforeClass
    public static void prepareAndConfigure() throws IOException, CommandFailedException, GeneralSecurityException {
        offlineClient = ManagementClient.offline(OfflineOptions.standalone().rootDirectory(new File("target/jboss-as"))
                        .configurationFile("standalone.xml").build());


        offlineClient.apply(CONFIGURATION_BACKUP.backup());

        prepareKeyAndTrustStoreFiles();
        prepareSslOptions();
        configureServerManagementInterfaces();
    }

    @AfterClass
    public static void restoreConfiguration() throws CommandFailedException {
        offlineClient.apply(CONFIGURATION_BACKUP.restore());
    }

    @AfterClass
    public static void deleteKeyAndTrustStoreFiles() {
        serverKeyStoreFile.delete();
        serverTrustStoreFile.delete();
        clientKeyStoreFile.delete();
        clientTrustStoreFile.delete();
    }

    @Before
    @Override
    public void connect() throws IOException {
        if (this.controller.isStarted(ManualTests.ARQUILLIAN_CONTAINER_MGMT_PROTOCOL_REMOTE)) {
            client = ManagementClient.online(OnlineOptions.standalone().localDefault().ssl(sslOptions).build());
        }
    }

    @Test
    @InSequence(Integer.MIN_VALUE)
    public void startServerAtFirst() throws IOException, CommandFailedException, GeneralSecurityException {
        this.controller.start(ManualTests.ARQUILLIAN_CONTAINER_MGMT_PROTOCOL_REMOTE);
    }

    @Test
    @InSequence(Integer.MAX_VALUE)
    public void stopServerInTheEnd() throws IOException, CommandFailedException {
        this.controller.stop(ManualTests.ARQUILLIAN_CONTAINER_MGMT_PROTOCOL_REMOTE);
    }

    @Test(expected = IllegalStateException.class)
    @Override
    public void clientConfiguredForDomain() throws IOException {
        ManagementClient.online(OnlineOptions.domain().build().localDefault().ssl(sslOptions).build());
    }


    private static void prepareKeyAndTrustStoreFiles() throws GeneralSecurityException, IOException {
        final KeyPairAndCertificate serverKeyPair = KeyPairAndCertificate.generateSelfSigned("test-creaper-server");
        serverKeyStoreFile = serverKeyPair.toTmpKeyStoreFile(SERVER_KEY_ALIAS, SERVER_KEY_PASSWORD);
        clientTrustStoreFile = serverKeyPair.toTmpTrustStoreFile(SERVER_CERTIFICATE_ALIAS, SERVER_CERTIFICATE_PASSWORD);

        final KeyPairAndCertificate clientKeyPair = KeyPairAndCertificate.generateSelfSigned("test-creaper-client");
        clientKeyStoreFile = clientKeyPair.toTmpKeyStoreFile(CLIENT_KEY_ALIAS, CLIENT_KEY_PASSWORD);
        serverTrustStoreFile = clientKeyPair.toTmpTrustStoreFile(CLIENT_CERTIFICATE_ALIAS, CLIENT_CERTIFICATE_PASSWORD);
    }

    private static void prepareSslOptions() {
        sslOptions = new SslOptions.Builder()
                .keyStore(clientKeyStoreFile)
                .keyStorePassword(CLIENT_KEY_PASSWORD)
                .key(CLIENT_KEY_ALIAS, CLIENT_KEY_PASSWORD)
                .trustStore(clientTrustStoreFile)
                .trustStorePassword(SERVER_CERTIFICATE_PASSWORD)
                .build();
    }

    private static void configureServerManagementInterfaces() throws CommandFailedException {
        AddHttpsManagementSecurityRealm addHttpsSecurityRealm
                = new AddHttpsManagementSecurityRealm.Builder(SECURITY_REALM)
                        .keystorePath(serverKeyStoreFile.getAbsolutePath())
                        .keystorePassword(SERVER_KEY_PASSWORD)
                        .keystoreProvider(KeyStoreType.DEFAULT_TYPE.typeName().toUpperCase())
                        .keyAlias(SERVER_KEY_ALIAS)
                        .truststorePath(serverTrustStoreFile.getAbsolutePath())
                        .truststorePassword(CLIENT_CERTIFICATE_PASSWORD)
                        .truststoreProvider(KeyStoreType.DEFAULT_TYPE.typeName().toUpperCase())
                        .build();

        AddNativeManagementInterface addNativeInterface = new AddNativeManagementInterface
                .Builder("ManagementRealm", "management-http").replaceExisting().build();

        AddHttpManagementInterface addHttpsInterface = new AddHttpManagementInterface.Builder(SECURITY_REALM)
                .secureSocketBinding("management-https").httpUpgradeEnabled(true).replaceExisting().build();

        offlineClient.apply(addHttpsSecurityRealm, addNativeInterface, addHttpsInterface);
    }
}
