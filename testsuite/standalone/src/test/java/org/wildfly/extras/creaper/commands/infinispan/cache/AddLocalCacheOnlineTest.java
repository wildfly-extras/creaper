package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AddLocalCacheOnlineTest {
    private OnlineManagementClient client;
    private Operations ops;

    private static final String TEST_CACHE_NAME = UUID.randomUUID().toString();

    private static final Address TEST_CACHE_ADDRESS = Address.subsystem("infinispan")
            .and("cache-container", "hibernate")
            .and("local-cache", TEST_CACHE_NAME);

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeFalse("The command is not compatible with WildFly 27 and above,"
                        + " see https://github.com/wildfly-extras/creaper/issues/218.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_20_0_0));
    }

    @Before
    public void connect() throws Exception {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
    }

    @After
    public void after() throws CommandFailedException, IOException, OperationException {
        client.apply(new RemoveCache("hibernate", CacheType.LOCAL_CACHE, TEST_CACHE_NAME));
        client.close();
    }

    @Test
    public void addCacheWithRequiredArgsOnly() throws CommandFailedException, IOException {
        AddLocalCache cmd = new AddLocalCache.Builder(TEST_CACHE_NAME)
                .cacheContainer("hibernate")
                .build();
        client.apply(cmd);

        ModelNodeResult resource = ops.readResource(TEST_CACHE_ADDRESS);

        assertTrue(resource.isSuccess());
    }

    @Test
    public void addCacheWithMoreArgs() throws CommandFailedException, IOException {
        String jndiName = "java:/MyAwesomeCache";
        String module = "org.hibernate.infinispan";
        AddLocalCache cmd = new AddLocalCache.Builder(TEST_CACHE_NAME)
                .cacheContainer("hibernate")
                .statisticsEnabled(false)
                .jndiName(jndiName)
                .module(module)
                .build();
        client.apply(cmd);

        ModelNodeResult resource = ops.readResource(TEST_CACHE_ADDRESS);

        assertTrue(resource.isSuccess());
        assertEquals(jndiName, ops.readAttribute(TEST_CACHE_ADDRESS, "jndi-name").stringValue());
        assertEquals(module, ops.readAttribute(TEST_CACHE_ADDRESS, "module").stringValue());
        assertEquals(false, ops.readAttribute(TEST_CACHE_ADDRESS, "statistics-enabled").booleanValue());
    }
}
