package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddCustomRealmMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_REALM_MAPPER_NAME = "CreaperTestAddCustomRealmMapper";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_nullName() throws Exception {
        new AddCustomRealmMapper.Builder(null)
                .module("someModule")
                .className("someClassName");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRealmMapper_emptyName() throws Exception {
        new AddCustomRealmMapper.Builder("")
                .module("someModule")
                .className("someClassName");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_noClassName() throws Exception {
        new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_emptyClassName() throws Exception {
        new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
                .module("someModule")
                .className("")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_noModule() throws Exception {
        new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_emptyModule() throws Exception {
        new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
                .module("")
                .className("someClassName")
                .build();
        fail("Creating command with empty module should throw exception");
    }

}
