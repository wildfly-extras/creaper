package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddCustomPrincipalDecoderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME = "CreaperTestAddCustomPrincipalDecoder";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_nullName() throws Exception {
        new AddCustomPrincipalDecoder.Builder(null)
                .module("someModule")
                .className("someClassName");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomPrincipalDecoder_emptyName() throws Exception {
        new AddCustomPrincipalDecoder.Builder("")
                .module("someModule")
                .className("someClassName");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_noClassName() throws Exception {
        new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_emptyClassName() throws Exception {
        new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
                .module("someModule")
                .className("")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_noModule() throws Exception {
        new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_emptyModule() throws Exception {
        new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
                .module("")
                .className("someClassName")
                .build();
        fail("Creating command with empty module should throw exception");
    }

}
