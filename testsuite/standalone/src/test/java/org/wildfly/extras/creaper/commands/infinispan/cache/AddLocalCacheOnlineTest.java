package org.wildfly.extras.creaper.commands.infinispan.cache;

import java.io.IOException;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;

@RunWith(Arquillian.class)
@RunAsClient
public class AddLocalCacheOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;

    private static final String TEST_CACHE_NAME = UUID.randomUUID().toString();

    private static final Address TEST_CACHE_ADDRESS = Address
            .subsystem("infinispan")
            .and("cache-container", "hibernate")
            .and("local-cache", TEST_CACHE_NAME);

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
        final ModelNodeResult resource = ops.readResource(TEST_CACHE_ADDRESS);
        Assert.assertTrue(resource.isSuccess());
    }

    @Test
    public void addCacheWithMoreArgs() throws CommandFailedException, IOException {
        final String jndiName = "java:/MyAwesomeCache";
        final String module = "org.jboss.awesome";
        AddLocalCache cmd = new AddLocalCache.Builder(TEST_CACHE_NAME)
                .cacheContainer("hibernate")
                .statisticsEnabled(false)
                .jndiName(jndiName)
                .module(module)
                .build();
        client.apply(cmd);
        final ModelNodeResult resource = ops.readResource(TEST_CACHE_ADDRESS);
        Assert.assertTrue(resource.isSuccess());
        Assert.assertEquals(jndiName, ops.readAttribute(TEST_CACHE_ADDRESS, "jndi-name").stringValue());
        Assert.assertEquals(module, ops.readAttribute(TEST_CACHE_ADDRESS, "module").stringValue());
        Assert.assertEquals(false,
                ops.readAttribute(TEST_CACHE_ADDRESS, "statistics-enabled").booleanValue());
    }


}
