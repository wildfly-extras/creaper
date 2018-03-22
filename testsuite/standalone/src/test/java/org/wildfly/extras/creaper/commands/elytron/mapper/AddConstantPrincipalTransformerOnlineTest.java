package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddConstantPrincipalTransformerOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestConstantPrincipalTransformer";
    private static final Address TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
    private static final String TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestConstantPrincipalTransformer2";
    private static final Address TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addConstantPrincipalTransformer() throws Exception {
        AddConstantPrincipalTransformer addConstantPrincipalTransformer
                = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant("name1")
                .build();

        client.apply(addConstantPrincipalTransformer);

        assertTrue("Constant principal transformer should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkConstantPrincipalTransformerConstant("name1");
    }

    @Test
    public void addTwoConstantPrincipalTransformers() throws Exception {
        AddConstantPrincipalTransformer addConstantPrincipalTransformer
                = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant("name1")
                .build();
        AddConstantPrincipalTransformer addConstantPrincipalTransformer2
                = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .constant("name2")
                .build();

        client.apply(addConstantPrincipalTransformer);
        client.apply(addConstantPrincipalTransformer2);

        assertTrue("Constant principal transformer should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS));
        assertTrue("Constant principal transformer should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConstantPrincipalTransformerNotAllowed() throws Exception {
        AddConstantPrincipalTransformer addConstantPrincipalTransformer
                = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant("name1")
                .build();
        AddConstantPrincipalTransformer addConstantPrincipalTransformer2
                = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant("name1")
                .build();

        client.apply(addConstantPrincipalTransformer);
        assertTrue("Constant principal transformer should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addConstantPrincipalTransformer2);
        fail("Constant principal transformer CreaperTestConstantPrincipalTransformer already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistConstantPrincipalTransformerAllowed() throws Exception {
        AddConstantPrincipalTransformer addConstantPrincipalTransformer
                = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant("name1")
                .build();
        AddConstantPrincipalTransformer addConstantPrincipalTransformer2
                = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant("name2")
                .replaceExisting()
                .build();

        client.apply(addConstantPrincipalTransformer);
        assertTrue("Constant principal transformer should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkConstantPrincipalTransformerConstant("name1");

        client.apply(addConstantPrincipalTransformer2);
        assertTrue("Constant principal transformer should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkConstantPrincipalTransformerConstant("name2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalTransformer_nullName() throws Exception {
        new AddConstantPrincipalTransformer.Builder(null)
                .constant("name1")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalTransformer_emptyName() throws Exception {
        new AddConstantPrincipalTransformer.Builder("")
                .constant("name1")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalTransformer_nullConstant() throws Exception {
        new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant(null)
                .build();
        fail("Creating command with null constant should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalTransformer_emptyConstant() throws Exception {
        new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .constant("")
                .build();
        fail("Creating command with empty constant should throw exception");
    }

    private void checkConstantPrincipalTransformerConstant(String expectedValue) throws IOException {
        checkAttribute(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS, "constant", expectedValue);
    }
}
