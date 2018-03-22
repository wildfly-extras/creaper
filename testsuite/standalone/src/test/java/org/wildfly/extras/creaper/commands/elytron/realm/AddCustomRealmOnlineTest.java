package org.wildfly.extras.creaper.commands.elytron.realm;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddCustomRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_REALM_NAME = "CreaperTestAddCustomRealm";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_nullName() throws Exception {
        new AddCustomRealm.Builder(null)
                .module("someModule")
                .className("someClassName");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRealm_emptyName() throws Exception {
        new AddCustomRealm.Builder("")
                .module("someModule")
                .className("someClassName");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_noClassName() throws Exception {
        new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_emptyClassName() throws Exception {
        new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
                .module("someModule")
                .className("")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_noModule() throws Exception {
        new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_emptyModule() throws Exception {
        new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
                .module("")
                .className("someClassName")
                .build();
        fail("Creating command with empty module should throw exception");
    }

}
