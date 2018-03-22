package org.wildfly.extras.creaper.commands.elytron.credfactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddKerberosSecurityFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String KRB_NAME = "CreaperTestKerberosSecurityFactory";
    private static final String KRB_NAME2 = "CreaperTestKerberosSecurityFactory2";
    private static final String KRB_PRINCIPAL = "principal1";
    private static final String KRB_PRINCIPAL2 = "principal2";
    private static final String KRB_PATH = "/path/to/keytab";
    private static final String KRB_OIDS = "1.2.840.113554.1.2.2";
    private static final Address KRB_ADDRESS = SUBSYSTEM_ADDRESS.and("kerberos-security-factory", KRB_NAME);
    private static final Address KRB_ADDRESS2 = SUBSYSTEM_ADDRESS.and("kerberos-security-factory", KRB_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(KRB_ADDRESS);
        ops.removeIfExists(KRB_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleKerberosSecurityFactory() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();
        assertFalse("The kerberos security factory should not exist", ops.exists(KRB_ADDRESS));
        client.apply(addKerberosSecurityFactory);
        assertTrue("Kerberos security factory should be created", ops.exists(KRB_ADDRESS));
    }

    @Test
    public void addTwoSimpleKerberosSecurityFactory() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();
        AddKerberosSecurityFactory addKerberosSecurityFactory2 = new AddKerberosSecurityFactory.Builder(KRB_NAME2)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();

        assertFalse("The kerberos security factory should not exist", ops.exists(KRB_ADDRESS));
        assertFalse("The kerberos security factory should not exist", ops.exists(KRB_ADDRESS2));

        client.apply(addKerberosSecurityFactory);
        client.apply(addKerberosSecurityFactory2);

        assertTrue("Kerberos security factory should be created", ops.exists(KRB_ADDRESS));
        assertTrue("Kerberos security factory should be created", ops.exists(KRB_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateKerberosSecurityFactoryNotAllowed() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();
        AddKerberosSecurityFactory addKerberosSecurityFactory2 = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();

        client.apply(addKerberosSecurityFactory);
        assertTrue("The kerberos security factory should be created", ops.exists(KRB_ADDRESS));

        client.apply(addKerberosSecurityFactory2);
        fail("The kerberos security factory is already configured, exception should be thrown");
    }

    @Test
    public void addDuplicateKerberosSecurityFactoryAllowed() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();
        AddKerberosSecurityFactory addKerberosSecurityFactory2 = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL2)
                .path(KRB_PATH)
                .replaceExisting()
                .build();

        client.apply(addKerberosSecurityFactory);
        assertTrue("The kerberos security factory should be created", ops.exists(KRB_ADDRESS));

        client.apply(addKerberosSecurityFactory2);
        assertTrue("The kerberos security factory should be created", ops.exists(KRB_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(KRB_ADDRESS, "principal", KRB_PRINCIPAL2);
    }

    @Test
    public void addFullKerberosSecurityFactory() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .mechanismOIDs(KRB_OIDS, "1.2.840.48018.1.2.2", "1.3.6.1.5.5.2")
                .mechanismNames("KRB5", "SPNEGO")
                .minimumRemainingLifetime(1)
                .relativeTo("jboss.server.config.dir")
                .requestLifetime(2)
                .debug(true)
                .server(false)
                .obtainKerberosTicket(true)
                .wrapGssCredential(true)
                .required(false)
                .addOption("a", "b")
                .addOption("debug", "false")
                .build();
        client.apply(addKerberosSecurityFactory);
        assertTrue("Kerberos security factory should be created", ops.exists(KRB_ADDRESS));

        checkAttribute(KRB_ADDRESS, "principal", KRB_PRINCIPAL);
        checkAttribute(KRB_ADDRESS, "path", KRB_PATH);
        checkAttribute(KRB_ADDRESS, "mechanism-oids", Arrays.asList(KRB_OIDS, "1.2.840.48018.1.2.2", "1.3.6.1.5.5.2"));
        checkAttribute(KRB_ADDRESS, "mechanism-names", Arrays.asList("KRB5", "SPNEGO"));
        checkAttribute(KRB_ADDRESS, "minimum-remaining-lifetime", "1");
        checkAttribute(KRB_ADDRESS, "relative-to", "jboss.server.config.dir");
        checkAttribute(KRB_ADDRESS, "request-lifetime", "2");
        checkAttribute(KRB_ADDRESS, "debug", "true");
        checkAttribute(KRB_ADDRESS, "server", "false");
        checkAttribute(KRB_ADDRESS, "obtain-kerberos-ticket", "true");
        checkAttribute(KRB_ADDRESS, "wrap-gss-credential", "true");
        checkAttribute(KRB_ADDRESS, "required", "false");
        checkAttributeObject(KRB_ADDRESS, "options", "debug", "false");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKerberosSecurityFactory_nullName() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(null)
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();
        fail("Creating command with null kerberos security factory name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKerberosSecurityFactory_emptyName() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder("")
                .principal(KRB_PRINCIPAL)
                .path(KRB_PATH)
                .build();
        fail("Creating command with empty kerberos security factory name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKerberosSecurityFactory_nullPath() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path(null)
                .build();
        fail("Creating command with null kerberos security factory path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKerberosSecurityFactory_emptyPath() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(KRB_PRINCIPAL)
                .path("")
                .build();
        fail("Creating command with empty kerberos security factory path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKerberosSecurityFactory_nullPrincipal() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal(null)
                .path(KRB_PATH)
                .build();
        fail("Creating command with null kerberos security factory principal should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKerberosSecurityFactory_emptyPrincipal() throws Exception {
        AddKerberosSecurityFactory addKerberosSecurityFactory = new AddKerberosSecurityFactory.Builder(KRB_NAME)
                .principal("")
                .path(KRB_PATH)
                .build();
        fail("Creating command with empty kerberos security factory principal should throw exception");
    }

}
