package org.wildfly.extras.creaper.commands.security;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddSecurityDomainOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_DOMAIN_NAME = "creaperSecDomain";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS
            = Address.subsystem("security").and("security-domain", TEST_SECURITY_DOMAIN_NAME);

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addSimpleSecurityDomain() throws Exception {
        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME).build();
        client.apply(addSecurityDomain);

        assertTrue("The security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
    }

    @Test
    public void addFullSecurityDomain() throws Exception {
        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .cacheType("default")
                .build();
        client.apply(addSecurityDomain);

        assertTrue("The security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));

        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "cache-type");
        readAttribute.assertSuccess("Read operation for cache-type failed");
        Assert.assertEquals("Read operation for cache-type return wrong value", "default", readAttribute.stringValue());
    }

    @Test(expected = CommandFailedException.class)
    public void addExistSecurityDomainNotAllowed() throws Exception {
        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME).build();
        client.apply(addSecurityDomain);
        client.apply(addSecurityDomain);
        fail("Security domain creaperSecDomain already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistSecurityDomainAllowed() throws Exception {
        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .replaceExisting()
                .build();
        AddSecurityDomain addSecurityDomain2 = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .cacheType("default")
                .replaceExisting()
                .build();

        client.apply(addSecurityDomain);
        assertTrue("The security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
        client.apply(addSecurityDomain2);
        assertTrue("The security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));

        ModelNodeResult readAttribute = ops.readAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "cache-type");
        readAttribute.assertSuccess("Read operation for cache-type failed");
        Assert.assertEquals("Read operation for cache-type return wrong value", "default", readAttribute.stringValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_nullName() throws Exception {
        new AddSecurityDomain.Builder(null).build();
        fail("Creating command with null security domain name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_emptyName() throws Exception {
        new AddSecurityDomain.Builder("").build();
        fail("Creating command with empty security domain name should throw exception");
    }
}
