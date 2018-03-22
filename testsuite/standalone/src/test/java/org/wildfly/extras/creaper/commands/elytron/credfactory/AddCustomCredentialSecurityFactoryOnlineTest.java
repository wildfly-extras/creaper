package org.wildfly.extras.creaper.commands.elytron.credfactory;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;


@RunWith(Arquillian.class)
public class AddCustomCredentialSecurityFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME = "CreaperTestAddCustomCredentialSecurityFactory";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_nullName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(null)
                .className("someClassName")
                .module("someModule");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomCredentialSecurityFactory_emptyName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder("")
                .className("someClassName")
                .module("someModule");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_noClassName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_emptyClassName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
                .className("")
                .module("someModule")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_noModule() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_emptyModule() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
                .className("someClassName")
                .module("")
                .build();
        fail("Creating command with empty module should throw exception");
    }

}
