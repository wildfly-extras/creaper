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
public class AddAggregatePrincipalTransformerOnlineTest extends AbstractAddPrincipalTransformerOnlineTest {

    private static final String TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestAggregatePrincipalTransformer";
    private static final Address TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("aggregate-principal-transformer", TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME);
    private static final String TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestAggregatePrincipalTransformer2";
    private static final Address TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("aggregate-principal-transformer", TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addAggregatePrincipalTransformer() throws Exception {
        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        client.apply(addAggregatePrincipalTransformer);

        assertTrue("Aggregate-principal-transformer should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkAttribute(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS, "principal-transformers",
                PRINCIPAL_TRANSFORMERS_1_AND_2);
    }

    @Test
    public void addTwoAggregatePrincipalTransformers() throws Exception {
        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer2
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME2)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2,
                        TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME)
                .build();

        client.apply(addAggregatePrincipalTransformer);
        client.apply(addAggregatePrincipalTransformer2);

        assertTrue("Aggregate-principal-transformer should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS));
        assertTrue("Aggregate-principal-transformer should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAggregatePrincipalTransformersNotAllowed() throws Exception {
        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer2
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2,
                        TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME)
                .build();

        client.apply(addAggregatePrincipalTransformer);
        assertTrue("Aggregate-principal-transformer should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addAggregatePrincipalTransformer2);
        fail("Aggregate-principal-transformer CreaperTestAggregatePrincipalTransformer already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistAggregatePrincipalTransformersAllowed() throws Exception {
        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();

        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer2
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2,
                        TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME)
                .replaceExisting()
                .build();

        client.apply(addAggregatePrincipalTransformer);
        assertTrue("Aggregate-principal-transformer should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addAggregatePrincipalTransformer2);
        assertTrue("Aggregate-principal-transformer should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkAttribute(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS, "principal-transformers",
                PRINCIPAL_TRANSFORMERS_2_AND_DIFFERENT);
    }

    @Test(expected = CommandFailedException.class)
    public void addAggregatePrincipalTransformerWithoutConfiguredPrincipalTransformers() throws Exception {
        AddAggregatePrincipalTransformer addAggregatePrincipalTransformer
                = new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME, "NotConfiguredPrincipalTransformer")
                .build();

        client.apply(addAggregatePrincipalTransformer);
        fail("Aggregate-principal-transformer shouldn't be added when using unconfigured principal transformer");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalTransformer_nullName() throws Exception {
        new AddAggregatePrincipalTransformer.Builder(null)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalTransformer_emptyName() throws Exception {
        new AddAggregatePrincipalTransformer.Builder("")
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME,
                        TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalTransformer_nullPrincipalTransformers() throws Exception {
        new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(null)
                .build();
        fail("Creating command with null principal-transformers should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalTransformer_emptyPrincipalTransformers() throws Exception {
        new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers("")
                .build();
        fail("Creating command with empty principal-transformers should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalTransformer_onePrincipalTransformer() throws Exception {
        new AddAggregatePrincipalTransformer.Builder(TEST_AGGREGATE_PRINCIPAL_TRANSFORMER_NAME)
                .principalTransformers(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .build();
        fail("Creating command with only one principal-transformer should throw exception");
    }

}
