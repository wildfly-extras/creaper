package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.commands.elytron.mapper.AbstractAddPrincipalTransformerOnlineTest.TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME;

@RunWith(Arquillian.class)
public class AddChainedPrincipalTransformerOnlineTest extends AbstractAddPrincipalTransformerOnlineTest {

    private static final String TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestChainedPrincipalTransformer";
    private static final Address TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("chained-principal-transformer", TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME);
    private static final String TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestChainedPrincipalTransformer2";
    private static final Address TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("chained-principal-transformer", TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addChainedPrincipalTransformer() throws Exception {
        AddChainedPrincipalTransformer addChainedPrincipalTransformer
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        client.apply(addChainedPrincipalTransformer);

        assertTrue("Chained-principal-transformer should be created",
                ops.exists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkAttribute(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS, "principal-transformers",
                PRINCIPAL_TRANSFORMERS_1_AND_2);
    }

    @Test
    public void addTwoChainedPrincipalTransformers() throws Exception {
        AddChainedPrincipalTransformer addChainedPrincipalTransformer
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        AddChainedPrincipalTransformer addChainedPrincipalTransformer2
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME2)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2,
                        TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME)
                .build();

        client.apply(addChainedPrincipalTransformer);
        client.apply(addChainedPrincipalTransformer2);

        assertTrue("Chained-principal-transformer should be created",
                ops.exists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS));
        assertTrue("Chained-principal-transformer should be created",
                ops.exists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistChainedPrincipalTransformersNotAllowed() throws Exception {
        AddChainedPrincipalTransformer addChainedPrincipalTransformer
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        AddChainedPrincipalTransformer addChainedPrincipalTransformer2
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2,
                        TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME)
                .build();

        client.apply(addChainedPrincipalTransformer);
        assertTrue("Chained-principal-transformer should be created",
                ops.exists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addChainedPrincipalTransformer2);
        fail("Chained-principal-transformer CreaperTestChainedPrincipalTransformer already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistChainedPrincipalTransformersAllowed() throws Exception {
        AddChainedPrincipalTransformer addChainedPrincipalTransformer
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        AddChainedPrincipalTransformer addChainedPrincipalTransformer2
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2,
                        TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME)
                .replaceExisting()
                .build();

        client.apply(addChainedPrincipalTransformer);
        assertTrue("Chained-principal-transformer should be created",
                ops.exists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addChainedPrincipalTransformer2);
        assertTrue("Chained-principal-transformer should be created",
                ops.exists(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkAttribute(TEST_CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS, "principal-transformers",
                PRINCIPAL_TRANSFORMERS_2_AND_DIFFERENT);
    }

    @Test(expected = CommandFailedException.class)
    public void addChainedPrincipalTransformerWithoutConfiguredPrincipalTransformers() throws Exception {
        AddChainedPrincipalTransformer addChainedPrincipalTransformer
                = new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME, "NotConfiguredPrincipalTransformer")
                .build();

        client.apply(addChainedPrincipalTransformer);
        fail("Chained-principal-transformer shouldn't be added when using unconfigured principal transformer");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addChainedPrincipalTransformer_nullName() throws Exception {
        new AddChainedPrincipalTransformer.Builder(null)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addChainedPrincipalTransformer_emptyName() throws Exception {
        new AddChainedPrincipalTransformer.Builder("")
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addChainedPrincipalTransformer_nullPrincipalTransformers() throws Exception {
        new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(null)
                .build();
        fail("Creating command with null principal-transformers should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addChainedPrincipalTransformer_emptyPrincipalTransformers() throws Exception {
        new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers("")
                .build();
        fail("Creating command with empty principal-transformers should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addChainedPrincipalTransformer_onePrincipalTransformer() throws Exception {
        new AddChainedPrincipalTransformer.Builder(TEST_CHAINED_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .build();
        fail("Creating command with only one principal-transformer should throw exception");
    }

}
