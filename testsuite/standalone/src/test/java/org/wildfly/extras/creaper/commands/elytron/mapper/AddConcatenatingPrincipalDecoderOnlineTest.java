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
public class AddConcatenatingPrincipalDecoderOnlineTest extends AbstractAddPrincipalDecoderOnlineTest {

    private static final String TEST_CONCATENATING_PRINCIPAL_DECODER_NAME = "CreaperTestConcatenatingPrincipalDecoder";
    private static final Address TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("concatenating-principal-decoder", TEST_CONCATENATING_PRINCIPAL_DECODER_NAME);
    private static final String TEST_CONCATENATING_PRINCIPAL_DECODER_NAME2 = "CreaperTestConcatenatingPrincipalDecoder2";
    private static final Address TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("concatenating-principal-decoder", TEST_CONCATENATING_PRINCIPAL_DECODER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS);
        ops.removeIfExists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addConcatenatingPrincipalDecoder() throws Exception {
        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .joiner(".")
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        client.apply(addConcatenatingPricipalDecoder);

        assertTrue("Concatenating-principal-decoder should be created",
                ops.exists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS));
        checkConcatenatingPrincipalDecoderAttribute("joiner", ".");
        checkConcatenatingPrincipalDecoderAttribute("principal-decoders", PRINCIPAL_DECODERS_1_AND_2);
    }

    @Test
    public void addTwoConcatenatingPrincipalDecoders() throws Exception {
        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder2
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME2)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        client.apply(addConcatenatingPricipalDecoder);
        client.apply(addConcatenatingPricipalDecoder2);

        assertTrue("Concatenating-principal-decoder should be created",
                ops.exists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS));
        assertTrue("Concatenating-principal-decoder should be created",
                ops.exists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConcatenatingPrincipalDecodersNotAllowed() throws Exception {
        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder2
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        client.apply(addConcatenatingPricipalDecoder);
        assertTrue("Concatenating-principal-decoder should be created",
                ops.exists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS));

        client.apply(addConcatenatingPricipalDecoder2);
        fail("Concatenating-principal-decoder CreaperTestConcatenatingPrincipalDecoder already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistConcatenatingPrincipalDecodersAllowed() throws Exception {
        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();

        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder2
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .joiner("::")
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME2, TEST_DIFFERENT_PRINCIPAL_DECODER_NAME)
                .replaceExisting()
                .build();

        client.apply(addConcatenatingPricipalDecoder);
        assertTrue("Concatenating-principal-decoder should be created",
                ops.exists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS));

        client.apply(addConcatenatingPricipalDecoder2);
        assertTrue("Concatenating-principal-decoder should be created",
                ops.exists(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS));
        checkConcatenatingPrincipalDecoderAttribute("joiner", "::");
        checkConcatenatingPrincipalDecoderAttribute("principal-decoders", PRINCIPAL_DECODERS_2_AND_DIFFERENT);
    }

    @Test(expected = CommandFailedException.class)
    public void addConcatenatingPrincipalDecoderWithoutConfiguredPrincipalsDecoders() throws Exception {
        AddConcatenatingPrincipalDecoder addConcatenatingPricipalDecoder
                = new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .joiner(".")
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, "NotConfiguredPrincipalDecoder")
                .build();

        client.apply(addConcatenatingPricipalDecoder);
        fail("Concatenating-principal-decoder shouldn't be added when using unconfigured principal decoder");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConcatenatingPrincipalDecoder_nullName() throws Exception {
        new AddConcatenatingPrincipalDecoder.Builder(null)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConcatenatingPrincipalDecoder_emptyName() throws Exception {
        new AddConcatenatingPrincipalDecoder.Builder("")
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConcatenatingPrincipalDecoder_nullPrincipalDecoders() throws Exception {
        new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .principalDecoders(null)
                .build();
        fail("Creating command with null principal-decoders should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConcatenatingPrincipalDecoder_emptyPrincipalDecoders() throws Exception {
        new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .principalDecoders("")
                .build();
        fail("Creating command with empty principal-decoders should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConcatenatingPrincipalDecoder_onePrincipalDecoder() throws Exception {
        new AddConcatenatingPrincipalDecoder.Builder(TEST_CONCATENATING_PRINCIPAL_DECODER_NAME)
                .principalDecoders(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .build();
        fail("Creating command with only one principal-decoder should throw exception");
    }

    private void checkConcatenatingPrincipalDecoderAttribute(String attr, String expected) throws IOException {
        checkAttribute(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS, attr, expected);
    }

    private void checkConcatenatingPrincipalDecoderAttribute(String attr, List<String> expected) throws IOException {
        checkAttribute(TEST_CONCATENATING_PRINCIPAL_DECODER_ADDRESS, attr, expected);
    }
}
