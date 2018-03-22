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
public class AddSimpleRoleDecoderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SIMPLE_ROLE_DECODER_NAME = "CreaperTestSimpleRoleDecoder";
    private static final Address TEST_SIMPLE_ROLE_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("simple-role-decoder", TEST_SIMPLE_ROLE_DECODER_NAME);
    private static final String TEST_SIMPLE_ROLE_DECODER_NAME2 = "CreaperTestSimpleRoleDecoder2";
    private static final Address TEST_SIMPLE_ROLE_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("simple-role-decoder", TEST_SIMPLE_ROLE_DECODER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SIMPLE_ROLE_DECODER_ADDRESS);
        ops.removeIfExists(TEST_SIMPLE_ROLE_DECODER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleRoleDecoder() throws Exception {
        AddSimpleRoleDecoder addSimpleRoleDecoder = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute("groups")
                .build();

        client.apply(addSimpleRoleDecoder);

        assertTrue("Simple role decoder should be created", ops.exists(TEST_SIMPLE_ROLE_DECODER_ADDRESS));
    }

    @Test
    public void addTwoSimpleRoleDecoders() throws Exception {
        AddSimpleRoleDecoder addSimpleRoleDecoder = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute("groups")
                .build();
        AddSimpleRoleDecoder addSimpleRoleDecoder2 = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME2)
                .attribute("users")
                .build();

        client.apply(addSimpleRoleDecoder);
        client.apply(addSimpleRoleDecoder2);

        assertTrue("Simple role decoder should be created", ops.exists(TEST_SIMPLE_ROLE_DECODER_ADDRESS));
        assertTrue("Simple role decoder should be created", ops.exists(TEST_SIMPLE_ROLE_DECODER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistSimpleRoleDecoderNotAllowed() throws Exception {
        AddSimpleRoleDecoder addSimpleRoleDecoder = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute("groups")
                .build();
        AddSimpleRoleDecoder addSimpleRoleDecoder2 = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute("groups")
                .build();

        client.apply(addSimpleRoleDecoder);
        assertTrue("Simple role decoder should be created", ops.exists(TEST_SIMPLE_ROLE_DECODER_ADDRESS));

        client.apply(addSimpleRoleDecoder2);
        fail("Simple role decoder CreaperTestSimpleRoleDecoder already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistSimpleRoleDecoderAllowed() throws Exception {
        AddSimpleRoleDecoder addSimpleRoleDecoder = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute("groups")
                .build();
        AddSimpleRoleDecoder addSimpleRoleDecoder2 = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute("users")
                .replaceExisting()
                .build();

        client.apply(addSimpleRoleDecoder);
        assertTrue("Simple role decoder should be created", ops.exists(TEST_SIMPLE_ROLE_DECODER_ADDRESS));

        client.apply(addSimpleRoleDecoder2);
        assertTrue("Simple role decoder should be created", ops.exists(TEST_SIMPLE_ROLE_DECODER_ADDRESS));

        checkSimpleRoleDecoderAttribute("users");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRoleDecoder_nullName() throws Exception {
        new AddSimpleRoleDecoder.Builder(null)
                .attribute("groups")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRoleDecoder_emptyName() throws Exception {
        new AddSimpleRoleDecoder.Builder("")
                .attribute("groups")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRoleDecoder_nullAttribute() throws Exception {
        new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRoleDecoder_emptyAttribute() throws Exception {
        new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
                .attribute("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    private void checkSimpleRoleDecoderAttribute(String expectedValue) throws IOException {
        checkAttribute(TEST_SIMPLE_ROLE_DECODER_ADDRESS, "attribute", expectedValue);
    }
}
