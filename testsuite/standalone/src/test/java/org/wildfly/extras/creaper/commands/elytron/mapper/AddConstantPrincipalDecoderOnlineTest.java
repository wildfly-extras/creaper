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
public class AddConstantPrincipalDecoderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CONSTANT_PRINCIPAL_DECODER_NAME = "CreaperTestConstantPrincipalDecoder";
    private static final Address TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-decoder", TEST_CONSTANT_PRINCIPAL_DECODER_NAME);
    private static final String TEST_CONSTANT_PRINCIPAL_DECODER_NAME2 = "CreaperTestConstantPrincipalDecoder2";
    private static final Address TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-principal-decoder", TEST_CONSTANT_PRINCIPAL_DECODER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addConstantPrincipalDecoder() throws Exception {
        AddConstantPrincipalDecoder addConstantPrincipalDecoder
                = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant("role1")
                .build();

        client.apply(addConstantPrincipalDecoder);

        assertTrue("Constant principal decoder should be created", ops.exists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS));
    }

    @Test
    public void addTwoConstantPrincipalDecoders() throws Exception {
        AddConstantPrincipalDecoder addConstantPrincipalDecoder
                = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant("role1")
                .build();
        AddConstantPrincipalDecoder addConstantPrincipalDecoder2
                = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                .constant("role2")
                .build();

        client.apply(addConstantPrincipalDecoder);
        client.apply(addConstantPrincipalDecoder2);

        assertTrue("Constant principal decoder should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS));
        assertTrue("Constant principal decoder should be created",
                ops.exists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConstantPrincipalDecoderNotAllowed() throws Exception {
        AddConstantPrincipalDecoder addConstantPrincipalDecoder
                = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant("role1")
                .build();
        AddConstantPrincipalDecoder addConstantPrincipalDecoder2
                = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant("role1")
                .build();

        client.apply(addConstantPrincipalDecoder);
        assertTrue("Constant principal decoder should be created", ops.exists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS));

        client.apply(addConstantPrincipalDecoder2);
        fail("Constant principal decoder CreaperTestConstantPrincipalDecoder already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistConstantPrincipalDecoderAllowed() throws Exception {
        AddConstantPrincipalDecoder addConstantPrincipalDecoder
                = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant("role1")
                .build();
        AddConstantPrincipalDecoder addConstantPrincipalDecoder2
                = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant("role2")
                .replaceExisting()
                .build();

        client.apply(addConstantPrincipalDecoder);
        assertTrue("Constant principal decoder should be created", ops.exists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS));

        client.apply(addConstantPrincipalDecoder2);
        assertTrue("Constant principal decoder should be created", ops.exists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS));

        checkConstantPrincipalDecoderConstant("role2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalDecoder_nullName() throws Exception {
        new AddConstantPrincipalDecoder.Builder(null)
                .constant("role1")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalDecoder_emptyName() throws Exception {
        new AddConstantPrincipalDecoder.Builder("")
                .constant("role1")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalDecoder_nullConstant() throws Exception {
        new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPrincipalDecoder_emptyConstant() throws Exception {
        new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .constant("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    private void checkConstantPrincipalDecoderConstant(String expectedValue) throws IOException {
        checkAttribute(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS, "constant", expectedValue);
    }
}
