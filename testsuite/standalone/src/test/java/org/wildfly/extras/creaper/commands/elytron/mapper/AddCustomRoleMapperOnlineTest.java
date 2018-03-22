package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddCustomRoleMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_ROLE_MAPPER_NAME = "CreaperTestAddCustomRoleMapper";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_nullName() throws Exception {
        new AddCustomRoleMapper.Builder(null)
                .module("someModule")
                .className("someClassName");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRoleMapper_emptyName() throws Exception {
        new AddCustomRoleMapper.Builder("")
                .module("someModule")
                .className("someClassName");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_noClassName() throws Exception {
        new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_emptyClassName() throws Exception {
        new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
                .module("someModule")
                .className("")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_noModule() throws Exception {
        new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_emptyModule() throws Exception {
        new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
                .module("")
                .className("someClassName")
                .build();
        fail("Creating command with empty module should throw exception");
    }

}
