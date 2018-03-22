package org.wildfly.extras.creaper.commands.elytron.tls;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public abstract class AbstractAddSSLContextOnlineTest extends AbstractElytronOnlineTest {

    protected static final String TEST_KEY_STORE_NAME = "CreaperTestKeyStore";
    private static final String TEST_KEY_STORE_NAME2 = "CreaperTestKeyStore2";
    private static final Address TEST_KEY_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("key-store", TEST_KEY_STORE_NAME);
    private static final Address TEST_KEY_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-store", TEST_KEY_STORE_NAME2);
    private static final String TEST_KEY_STORE_TYPE = "JKS";
    private static final String TEST_KEY_STORE_PASSWORD = "password";
    private static final String TEST_KEY_PASSWORD = "password";

    protected static final String TEST_KEY_MNGR_NAME = "CreaperTestKeyManager";
    private static final String TEST_KEY_MNGR_NAME2 = "CreaperTestKeyManager2";
    private static final Address TEST_KEY_MNGR_ADDRESS = SUBSYSTEM_ADDRESS.and("key-manager", TEST_KEY_MNGR_NAME);
    private static final Address TEST_KEY_MNGR_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-manager", TEST_KEY_MNGR_NAME2);
    private static final String TEST_KEY_MANAGER_ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();

    protected static final String TRUST_MNGR_NAME = "CreaperTestTrustManager";
    private static final String TRUST_MNGR_NAME2 = "CreaperTestTrustManager2";
    private static final Address TRUST_MNGR_ADDRESS = SUBSYSTEM_ADDRESS.and("trust-manager", TRUST_MNGR_NAME);
    private static final Address TRUST_MNGR_ADDRESS2 = SUBSYSTEM_ADDRESS.and("trust-manager", TRUST_MNGR_NAME2);
    private static final String TRUST_MANAGER_ALGORITHM = TrustManagerFactory.getDefaultAlgorithm();


    @BeforeClass
    public static void addDependentResources() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                    .type(TEST_KEY_STORE_TYPE)
                    .credentialReference(new CredentialRef.CredentialRefBuilder()
                            .clearText(TEST_KEY_STORE_PASSWORD)
                            .build())
                    .build();
            AddKeyStore addKeyStore2 = new AddKeyStore.Builder(TEST_KEY_STORE_NAME2)
                    .type(TEST_KEY_STORE_TYPE)
                    .credentialReference(new CredentialRef.CredentialRefBuilder()
                            .clearText(TEST_KEY_STORE_PASSWORD)
                            .build())
                    .build();

            AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                    .algorithm(TEST_KEY_MANAGER_ALGORITHM)
                    .keyStore(TEST_KEY_STORE_NAME)
                    .credentialReference(new CredentialRef.CredentialRefBuilder()
                            .clearText(TEST_KEY_PASSWORD)
                            .build())
                    .build();
            AddKeyManager addKeyManager2 = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME2)
                    .algorithm(TEST_KEY_MANAGER_ALGORITHM)
                    .keyStore(TEST_KEY_STORE_NAME2)
                    .credentialReference(new CredentialRef.CredentialRefBuilder()
                            .clearText(TEST_KEY_PASSWORD)
                            .build())
                    .build();

            AddTrustManager addTrustManager = new AddTrustManager.Builder(TRUST_MNGR_NAME)
                    .algorithm(TRUST_MANAGER_ALGORITHM)
                    .keyStore(TEST_KEY_STORE_NAME)
                    .build();
            AddTrustManager addTrustManager2 = new AddTrustManager.Builder(TRUST_MNGR_NAME2)
                    .algorithm(TRUST_MANAGER_ALGORITHM)
                    .keyStore(TEST_KEY_STORE_NAME2)
                    .build();

            client.apply(addKeyStore);
            client.apply(addKeyStore2);
            client.apply(addKeyManager);
            client.apply(addKeyManager2);
            client.apply(addTrustManager);
            client.apply(addTrustManager2);

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @AfterClass
    public static void removeDependentResources() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations ops = new Operations(client);
            Administration administration = new Administration(client);
            ops.removeIfExists(TRUST_MNGR_ADDRESS);
            ops.removeIfExists(TRUST_MNGR_ADDRESS2);
            ops.removeIfExists(TEST_KEY_MNGR_ADDRESS);
            ops.removeIfExists(TEST_KEY_MNGR_ADDRESS2);
            ops.removeIfExists(TEST_KEY_STORE_ADDRESS);
            ops.removeIfExists(TEST_KEY_STORE_ADDRESS2);
            administration.reloadIfRequired();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

}
