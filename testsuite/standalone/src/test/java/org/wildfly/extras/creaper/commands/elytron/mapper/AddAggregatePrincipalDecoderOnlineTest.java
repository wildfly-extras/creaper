package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.io.IOException;
import java.util.List;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddAggregatePrincipalDecoderOnlineTest extends AbstractAddPrincipalDecoderOnlineTest {

    private static final String TEST_AGGREGATE_PRINCIPAL_DECODER_NAME = "CreaperTestAggregatePrincipalDecoder";
    private static final Address TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("aggregate-principal-decoder", TEST_AGGREGATE_PRINCIPAL_DECODER_NAME);
    private static final String TEST_AGGREGATE_PRINCIPAL_DECODER_NAME2 = "CreaperTestAggregatePrincipalDecoder2";
    private static final Address TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("aggregate-principal-decoder", TEST_AGGREGATE_PRINCIPAL_DECODER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS);
        ops.removeIfExists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addAggregatePrincipalDecoder() throws Exception {
        AddAggregatePrincipalDecoder addAggregatePricipalDecoder
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        client.apply(addAggregatePricipalDecoder);

        assertTrue("Aggregate-principal-decoder should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS));
        checkAggregatePrincipalDecoderAttribute("principal-decoders", PRINCIPAL_DECODERS_1_AND_2);
    }

    @Test
    public void addTwoAggregatePrincipalDecoders() throws Exception {
        AddAggregatePrincipalDecoder addAggregatePricipalDecoder
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        AddAggregatePrincipalDecoder addAggregatePricipalDecoder2
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME2)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME2, TEST_DIFFERENT_PRINCIPAL_DECODER_NAME)
                .build();

        client.apply(addAggregatePricipalDecoder);
        client.apply(addAggregatePricipalDecoder2);

        assertTrue("Aggregate-principal-decoder should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS));
        assertTrue("Aggregate-principal-decoder should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAggregatePrincipalDecodersNotAllowed() throws Exception {
        AddAggregatePrincipalDecoder addAggregatePricipalDecoder
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        AddAggregatePrincipalDecoder addAggregatePricipalDecoder2
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME2, TEST_DIFFERENT_PRINCIPAL_DECODER_NAME)
                .build();

        client.apply(addAggregatePricipalDecoder);
        assertTrue("Aggregate-principal-decoder should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS));

        client.apply(addAggregatePricipalDecoder2);
        fail("Aggregate-principal-decoder CreaperTestAggregatePrincipalDecoder already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistAggregatePrincipalDecodersAllowed() throws Exception {
        AddAggregatePrincipalDecoder addAggregatePricipalDecoder
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        AddAggregatePrincipalDecoder addAggregatePricipalDecoder2
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME2, TEST_DIFFERENT_PRINCIPAL_DECODER_NAME)
                .replaceExisting()
                .build();

        client.apply(addAggregatePricipalDecoder);
        assertTrue("Aggregate-principal-decoder should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS));

        client.apply(addAggregatePricipalDecoder2);
        assertTrue("Aggregate-principal-decoder should be created",
                ops.exists(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS));
        checkAggregatePrincipalDecoderAttribute("principal-decoders", PRINCIPAL_DECODERS_2_AND_DIFFERENT);
    }

    @Test(expected = CommandFailedException.class)
    public void addAggregatePrincipalDecoderWithoutConfiguredPrincipalsDecoders() throws Exception {
        AddAggregatePrincipalDecoder addAggregatePricipalDecoder
                = new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, "NotConfiguredPrincipalDecoder")
                .build();

        client.apply(addAggregatePricipalDecoder);
        fail("Aggregate-principal-decoder shouldn't be added when using unconfigured principal decoder");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalDecoder_nullName() throws Exception {
        new AddAggregatePrincipalDecoder.Builder(null)
            .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
            .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalDecoder_emptyName() throws Exception {
        new AddAggregatePrincipalDecoder.Builder("")
            .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
            .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalDecoder_nullPrincipalDecoders() throws Exception {
        new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
            .principalDecoders(null)
            .build();
        fail("Creating command with null principal-decoders should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalDecoder_emptyPrincipalDecoders() throws Exception {
        new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
            .principalDecoders("")
            .build();
        fail("Creating command with empty principal-decoders should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregatePrincipalDecoder_onePrincipalDecoder() throws Exception {
        new AddAggregatePrincipalDecoder.Builder(TEST_AGGREGATE_PRINCIPAL_DECODER_NAME)
            .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
            .build();
        fail("Creating command with only one principal-decoder should throw exception");
    }

    private void checkAggregatePrincipalDecoderAttribute(String attr, List<String> expected) throws IOException {
        checkAttribute(TEST_AGGREGATE_PRINCIPAL_DECODER_ADDRESS, attr, expected);
    }

}
