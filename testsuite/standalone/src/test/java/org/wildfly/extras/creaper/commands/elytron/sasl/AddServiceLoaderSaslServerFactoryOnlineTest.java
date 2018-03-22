package org.wildfly.extras.creaper.commands.elytron.sasl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddServiceLoaderSaslServerFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_FACTORY_NAME = "CreaperTestServiceLoaderSaslServerFactory";
    private static final Address TEST_SERVER_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("service-loader-sasl-server-factory", TEST_SERVER_FACTORY_NAME);
    private static final String TEST_SERVER_FACTORY_NAME2 = "CreaperTestServiceLoaderSaslServerFactory2";
    private static final Address TEST_SERVER_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("service-loader-sasl-server-factory", TEST_SERVER_FACTORY_NAME2);

    private static final String ELYTRON_MODULE = "org.wildfly.security.elytron";
    private static final String ELYTRON_SUBSYTEM_MODULE = "org.wildfly.extension.elytron";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleServiceLoaderSaslServerFactory() throws Exception {
        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .build();

        client.apply(addServiceLoaderSaslServerFactory);

        assertTrue("Service loader sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));

    }

    @Test
    public void addTwoServiceLoaderSaslServerFactories() throws Exception {
        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .build();

        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory2
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME2)
                .build();

        client.apply(addServiceLoaderSaslServerFactory);
        client.apply(addServiceLoaderSaslServerFactory2);

        assertTrue("Service loader sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        assertTrue("Second service loader sasl server factory should be created",
                ops.exists(TEST_SERVER_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullServiceLoaderSaslServerFactory() throws Exception {
        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .module(ELYTRON_MODULE)
                .build();

        client.apply(addServiceLoaderSaslServerFactory);

        assertTrue("Service loader sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "module", ELYTRON_MODULE);

    }

    @Test(expected = CommandFailedException.class)
    public void addExistServiceLoaderSaslServerFactoryNotAllowed() throws Exception {
        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .module(ELYTRON_MODULE)
                .build();

        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory2
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .module(ELYTRON_SUBSYTEM_MODULE)
                .build();

        client.apply(addServiceLoaderSaslServerFactory);
        assertTrue("Service loader sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addServiceLoaderSaslServerFactory2);
        fail("Service loader sasl server factory CreaperTestServiceLoaderSaslServerFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistServiceLoaderSaslServerFactoryAllowed() throws Exception {
        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .module(ELYTRON_MODULE)
                .build();

        AddServiceLoaderSaslServerFactory addServiceLoaderSaslServerFactory2
                = new AddServiceLoaderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .module(ELYTRON_SUBSYTEM_MODULE)
                .replaceExisting()
                .build();

        client.apply(addServiceLoaderSaslServerFactory);
        assertTrue("Service loader sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addServiceLoaderSaslServerFactory2);
        assertTrue("Service loader sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "module", ELYTRON_SUBSYTEM_MODULE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServiceLoaderSaslServerFactory_nullName() throws Exception {
        new AddServiceLoaderSaslServerFactory.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServiceLoaderSaslServerFactory_emptyName() throws Exception {
        new AddServiceLoaderSaslServerFactory.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

}
