package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddClientSSLContextOnlineTest extends AbstractAddSSLContextOnlineTest {

    private static final String CLIENT_SSL_CONTEXT_PROTOCOL = "TLSv1.2";
    private static final String CLIENT_SSL_CONTEXT_NAME = "CreaperTestCLientSSLContext";
    private static final String CLIENT_SSL_CONTEXT_NAME2 = "CreaperTestCLientSSLContext2";
    private static final Address CLIENT_SSL_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS.and("client-ssl-context",
            CLIENT_SSL_CONTEXT_NAME);
    private static final Address CLIENT_SSL_CONTEXT_ADDRESS2 = SUBSYSTEM_ADDRESS.and("client-ssl-context",
            CLIENT_SSL_CONTEXT_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(CLIENT_SSL_CONTEXT_ADDRESS);
        ops.removeIfExists(CLIENT_SSL_CONTEXT_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleClientSSLContext() throws Exception {
        AddClientSSLContext addClientSSLContext = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME)
                .build();
        assertFalse("The client ssl context should not exist", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));
        client.apply(addClientSSLContext);
        assertTrue("Client ssl context should be created", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));
    }

    @Test
    public void addTwoSimpleClientSSLContexts() throws Exception {
        AddClientSSLContext addClientSSLContext = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME)
                .build();
        AddClientSSLContext addClientSSLContext2 = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME2)
                .build();

        assertFalse("The client ssl context should not exist", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));
        assertFalse("The client ssl context should not exist", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS2));

        client.apply(addClientSSLContext);
        client.apply(addClientSSLContext2);

        assertTrue("Client SSL context should be created", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));
        assertTrue("Client SSL context should be created", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateClientSSLContextNotAllowed() throws Exception {
        AddClientSSLContext addClientSSLContext = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME)
                .build();
        AddClientSSLContext addClientSSLContext2 = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME)
                .build();

        client.apply(addClientSSLContext);
        assertTrue("The client ssl context should be created", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));

        client.apply(addClientSSLContext2);
        fail("Client ssl context is already configured, exception should be thrown");
    }

    @Test
    public void addDuplicateClientSSLContexAllowed() throws Exception {
        AddClientSSLContext addClientSSLContext = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME)
                .protocols("TLSv1.2")
                .build();
        AddClientSSLContext addClientSSLContext2 = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME)
                .protocols("TLSv1.1")
                .replaceExisting()
                .build();

        client.apply(addClientSSLContext);
        assertTrue("The client ssl context should be created", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));

        client.apply(addClientSSLContext2);
        assertTrue("The cleint ssl context should be created", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(CLIENT_SSL_CONTEXT_ADDRESS, "protocols", Arrays.asList("TLSv1.1"));
    }

    @Test
    public void addFullClientSSLContext() throws Exception {
        AddClientSSLContext addClientSSLContext = new AddClientSSLContext.Builder(CLIENT_SSL_CONTEXT_NAME)
                .cipherSuiteFilter("ALL")
                .keyManager(TEST_KEY_MNGR_NAME)
                .trustManager(TRUST_MNGR_NAME)
                .protocols(CLIENT_SSL_CONTEXT_PROTOCOL)
                .build();
        client.apply(addClientSSLContext);
        assertTrue("The client ssl context should be created", ops.exists(CLIENT_SSL_CONTEXT_ADDRESS));

        checkAttribute("cipher-suite-filter", "ALL");
        checkAttribute("key-manager", TEST_KEY_MNGR_NAME);
        checkAttribute("trust-manager", TRUST_MNGR_NAME);
        checkAttribute("protocols", Arrays.asList(CLIENT_SSL_CONTEXT_PROTOCOL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addClientSSLContext_nullName() throws Exception {
        new AddClientSSLContext.Builder(null)
            .build();
        fail("Creating command with null client SSL context name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addClientSSLContext_emptyName() throws Exception {
        new AddClientSSLContext.Builder("")
            .build();
        fail("Creating command with empty client ssl context name should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(CLIENT_SSL_CONTEXT_ADDRESS, attribute, expectedValue);
    }

    private void checkAttribute(String attribute, List<String> expected) throws IOException {
        checkAttribute(CLIENT_SSL_CONTEXT_ADDRESS, attribute, expected);
    }

}
