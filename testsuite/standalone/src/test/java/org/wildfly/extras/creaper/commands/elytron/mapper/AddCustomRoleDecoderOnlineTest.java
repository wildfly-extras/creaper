package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddCustomRoleDecoderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_ROLE_DECODER_NAME = "CreaperTestAddCustomRoleDecoder";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_nullName() throws Exception {
        new AddCustomRoleDecoder.Builder(null)
                .module("someModule")
                .className("someClassName");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRoleDecoder_emptyName() throws Exception {
        new AddCustomRoleDecoder.Builder("")
                .module("someModule")
                .className("someClassName");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_noClassName() throws Exception {
        new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_emptyClassName() throws Exception {
        new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
                .module("someModule")
                .className("")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_noModule() throws Exception {
        new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_emptyModule() throws Exception {
        new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
                .module("")
                .className("someClassName")
                .build();
        fail("Creating command with empty module should throw exception");
    }
}
