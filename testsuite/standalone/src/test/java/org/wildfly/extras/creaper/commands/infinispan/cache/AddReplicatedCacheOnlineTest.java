package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assume;
import org.junit.After;
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
public class AddReplicatedCacheOnlineTest {
    private OnlineManagementClient client;
    private Operations ops;

    private static final String TEST_CACHE_NAME = UUID.randomUUID().toString();

    private static final Address TEST_CACHE_ADDRESS = Address.subsystem("infinispan")
            .and("cache-container", "server")
            .and("replicated-cache", TEST_CACHE_NAME);

    // TODO WF 27 fails with DuplicateServiceException when adding a cache, find the WFLY JIRA
    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeFalse("Adding a cache fails on WildFly 27 with DuplicateServiceException",
                serverVersion.equalTo(ServerVersion.VERSION_20_0_0));
    }

    @Before
    public void connect() throws Exception {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
    }

    @After
    public void after() throws CommandFailedException, IOException, OperationException {
        client.apply(new RemoveCache("server", CacheType.REPLICATED_CACHE, TEST_CACHE_NAME));
        client.close();
    }

    @Test
    public void addCacheWithRequiredArgsOnly() throws CommandFailedException, IOException {
        AddReplicatedCache cmd = new AddReplicatedCache.Builder(TEST_CACHE_NAME)
                .cacheContainer("server")
                .build();
        client.apply(cmd);

        ModelNodeResult resource = ops.readResource(TEST_CACHE_ADDRESS);

        assertTrue(resource.isSuccess());
    }

    @Test
    public void addCacheWithMoreArgs() throws CommandFailedException, IOException {
        AddReplicatedCache cmd = new AddReplicatedCache.Builder(TEST_CACHE_NAME)
                .cacheContainer("server")
                .remoteTimeout(4321L)
                .statisticsEnabled(false)
                .build();
        client.apply(cmd);

        ModelNodeResult resource = ops.readResource(TEST_CACHE_ADDRESS);

        assertTrue(resource.isSuccess());
        assertEquals(4321L, ops.readAttribute(TEST_CACHE_ADDRESS, "remote-timeout").longValue());
        assertEquals(false, ops.readAttribute(TEST_CACHE_ADDRESS, "statistics-enabled").booleanValue());
    }
}
