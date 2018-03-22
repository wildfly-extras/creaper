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
public class AddRegexValidatingPrincipalTransformerOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestRegexValidatingPrincipalTransformer";
    private static final Address TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("regex-validating-principal-transformer", TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME);
    private static final String TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestRegexValidatingPrincipalTransformer2";
    private static final Address TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("regex-validating-principal-transformer", TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleRegexValidatingPrincipalTransformer() throws Exception {
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .build();

        client.apply(addRegexValidatingPrincipalTransformer);

        assertTrue("Regex validating principal transformer should be created",
                ops.exists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS));
    }

    @Test
    public void addTwoRegexValidatingPrincipalTransformers() throws Exception {
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .build();
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer2
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME2)
                .pattern("test-pattern2")
                .build();

        client.apply(addRegexValidatingPrincipalTransformer);
        client.apply(addRegexValidatingPrincipalTransformer2);

        assertTrue("Regex validating principal transformer should be created",
                ops.exists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS));
        assertTrue("Regex validating principal transformer should be created",
                ops.exists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS2));
    }

    @Test
    public void addFullRegexValidatingPrincipalTransformer() throws Exception {
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .match(true)
                .build();

        client.apply(addRegexValidatingPrincipalTransformer);

        assertTrue("Regex validating principal transformer should be created",
                ops.exists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkRegexValidatingPrincipalTransformerAttribute("pattern", "test-pattern");
        checkRegexValidatingPrincipalTransformerAttribute("match", "true");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistRegexValidatingPrincipalTransformerNotAllowed() throws Exception {
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .build();
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer2
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern2")
                .build();

        client.apply(addRegexValidatingPrincipalTransformer);
        assertTrue("Regex validating principal transformer should be created",
                ops.exists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addRegexValidatingPrincipalTransformer2);
        fail("Regex validating principal transformer CreaperTestRegexValidatingPrincipalTransformer already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistRegexValidatingPrincipalTransformerAllowed() throws Exception {
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .build();
        AddRegexValidatingPrincipalTransformer addRegexValidatingPrincipalTransformer2
                = new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern2")
                .replaceExisting()
                .build();

        client.apply(addRegexValidatingPrincipalTransformer);
        assertTrue("Regex validating principal transformer should be created",
                ops.exists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addRegexValidatingPrincipalTransformer2);
        assertTrue("Regex validating principal transformer should be created",
                ops.exists(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkRegexValidatingPrincipalTransformerAttribute("pattern", "test-pattern2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexValidatingPrincipalTransformer_nullName() throws Exception {
        new AddRegexValidatingPrincipalTransformer.Builder(null)
                .pattern("test-pattern")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexValidatingPrincipalTransformer_emptyName() throws Exception {
        new AddRegexValidatingPrincipalTransformer.Builder("")
                .pattern("test-pattern")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexValidatingPrincipalTransformer_nullPattern() throws Exception {
        new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern(null)
                .build();
        fail("Creating command with null pattern should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexValidatingPrincipalTransformer_emptyPattern() throws Exception {
        new AddRegexValidatingPrincipalTransformer.Builder(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("")
                .build();
        fail("Creating command with empty pattern should throw exception");
    }

    private void checkRegexValidatingPrincipalTransformerAttribute(String attr, String expected) throws IOException {
        checkAttribute(TEST_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS, attr, expected);
    }
}
