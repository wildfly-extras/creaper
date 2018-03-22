package org.wildfly.extras.creaper.commands.elytron.realm;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddCustomModifiableRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME = "CreaperTestAddCustomModifiableRealm";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_nullName() throws Exception {
        new AddCustomModifiableRealm.Builder(null)
                .module("someModule")
                .className("someClassName");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomModifiableRealm_emptyName() throws Exception {
        new AddCustomModifiableRealm.Builder("")
                .module("someModule")
                .className("someClassName");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_noClassName() throws Exception {
        new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_emptyClassName() throws Exception {
        new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
                .module("someModule")
                .className("")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_noModule() throws Exception {
        new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_emptyModule() throws Exception {
        new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
                .module("")
                .className("someClassName")
                .build();
        fail("Creating command with empty module should throw exception");
    }

}
