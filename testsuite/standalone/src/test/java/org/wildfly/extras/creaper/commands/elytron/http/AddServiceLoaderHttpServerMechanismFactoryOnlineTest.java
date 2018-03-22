package org.wildfly.extras.creaper.commands.elytron.http;

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
public class AddServiceLoaderHttpServerMechanismFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME
            = "CreaperTestServiceLoaderHttpServerMechanismFactory";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("service-loader-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME);
    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME2
            = "CreaperTestServiceLoaderHttpServerMechanismFactory2";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("service-loader-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME2);

    private static final String ELYTRON_MODULE = "org.wildfly.security.elytron";
    private static final String ELYTRON_SUBSYTEM_MODULE = "org.wildfly.extension.elytron";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleServiceLoaderHttpServerMechanismFactory() throws Exception {
        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        client.apply(addServiceLoaderHttpServerMechanismFactory);

        assertTrue("Service loader http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));

    }

    @Test
    public void addTwoServiceLoaderHttpServerMechanismFactories() throws Exception {
        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory2
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME2)
                .build();

        client.apply(addServiceLoaderHttpServerMechanismFactory);
        client.apply(addServiceLoaderHttpServerMechanismFactory2);

        assertTrue("Service loader http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        assertTrue("Second service loader http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullServiceLoaderHttpServerMechanismFactory() throws Exception {
        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .module(ELYTRON_MODULE)
                .build();

        client.apply(addServiceLoaderHttpServerMechanismFactory);

        assertTrue("Service loader http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "module", ELYTRON_MODULE);

    }

    @Test(expected = CommandFailedException.class)
    public void addExistServiceLoaderHttpServerMechanismFactoryNotAllowed() throws Exception {
        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .module(ELYTRON_MODULE)
                .build();

        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory2
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .module(ELYTRON_SUBSYTEM_MODULE)
                .build();

        client.apply(addServiceLoaderHttpServerMechanismFactory);
        assertTrue("Service loader http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addServiceLoaderHttpServerMechanismFactory2);
        fail("Service loader http server mechanism factory CreaperTestServiceLoaderHttpServerMechanismFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistServiceLoaderHttpServerMechanismFactoryAllowed() throws Exception {
        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .module(ELYTRON_MODULE)
                .build();

        AddServiceLoaderHttpServerMechanismFactory addServiceLoaderHttpServerMechanismFactory2
                = new AddServiceLoaderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .module(ELYTRON_SUBSYTEM_MODULE)
                .replaceExisting()
                .build();

        client.apply(addServiceLoaderHttpServerMechanismFactory);
        assertTrue("Service loader http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addServiceLoaderHttpServerMechanismFactory2);
        assertTrue("Service loader http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "module", ELYTRON_SUBSYTEM_MODULE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServiceLoaderHttpServerMechanismFactory_nullName() throws Exception {
        new AddServiceLoaderHttpServerMechanismFactory.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServiceLoaderHttpServerMechanismFactory_emptyName() throws Exception {
        new AddServiceLoaderHttpServerMechanismFactory.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }
}
