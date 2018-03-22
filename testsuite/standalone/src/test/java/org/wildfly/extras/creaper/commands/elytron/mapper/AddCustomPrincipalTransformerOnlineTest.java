package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;

@RunWith(Arquillian.class)
public class AddCustomPrincipalTransformerOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestAddCustomPrincipalTransformer";

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_nullName() throws Exception {
        new AddCustomPrincipalTransformer.Builder(null)
                .module("someModule")
                .className("someClassName");
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomPrincipalTransformer_emptyName() throws Exception {
        new AddCustomPrincipalTransformer.Builder("")
                .module("someModule")
                .className("someClassName");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_noClassName() throws Exception {
        new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .module("someModule")
                .build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_emptyClassName() throws Exception {
        new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .module("someModule")
                .className("")
                .build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_noModule() throws Exception {
        new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className("someClassName")
                .build();
        fail("Creating command with no module should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_emptyModule() throws Exception {
        new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .module("")
                .className("someClassName")
                .build();
        fail("Creating command with empty module should throw exception");
    }

}
