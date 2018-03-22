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
public class AddRegexPrincipalTransformerOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestRegexPrincipalTransformer";
    private static final Address TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("regex-principal-transformer", TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME);
    private static final String TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestRegexPrincipalTransformer2";
    private static final Address TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("regex-principal-transformer", TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addRegexPrincipalTransformer() throws Exception {
        AddRegexPrincipalTransformer addRegexPrincipalTransformer
                = new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .replacement("test-replacement")
                .replaceAll(true)
                .build();

        client.apply(addRegexPrincipalTransformer);

        assertTrue("Regex principal transformer should be created",
                ops.exists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkRegexPrincipalTransformerAttribute("pattern", "test-pattern");
        checkRegexPrincipalTransformerAttribute("replacement", "test-replacement");
        checkRegexPrincipalTransformerAttribute("replace-all", "true");
    }

    @Test
    public void addTwoRegexPrincipalTransformers() throws Exception {
        AddRegexPrincipalTransformer addRegexPrincipalTransformer
                = new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .replacement("test-replacement")
                .build();
        AddRegexPrincipalTransformer addRegexPrincipalTransformer2
                = new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME2)
                .pattern("test-pattern2")
                .replacement("test-replacement2")
                .build();

        client.apply(addRegexPrincipalTransformer);
        client.apply(addRegexPrincipalTransformer2);

        assertTrue("Regex principal transformer should be created",
                ops.exists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS));
        assertTrue("Regex principal transformer should be created",
                ops.exists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistRegexPrincipalTransformerNotAllowed() throws Exception {
        AddRegexPrincipalTransformer addRegexPrincipalTransformer
                = new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .replacement("test-replacement")
                .build();
        AddRegexPrincipalTransformer addRegexPrincipalTransformer2
                = new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern2")
                .replacement("test-replacement2")
                .build();

        client.apply(addRegexPrincipalTransformer);
        assertTrue("Regex principal transformer should be created",
                ops.exists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addRegexPrincipalTransformer2);
        fail("Regex principal transformer CreaperTestRegexPrincipalTransformer already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistRegexPrincipalTransformerAllowed() throws Exception {
        AddRegexPrincipalTransformer addRegexPrincipalTransformer
                = new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .replacement("test-replacement")
                .build();
        AddRegexPrincipalTransformer addRegexPrincipalTransformer2
                = new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern2")
                .replacement("test-replacement2")
                .replaceExisting()
                .build();

        client.apply(addRegexPrincipalTransformer);
        assertTrue("Regex principal transformer should be created",
                ops.exists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS));

        client.apply(addRegexPrincipalTransformer2);
        assertTrue("Regex principal transformer should be created",
                ops.exists(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS));
        checkRegexPrincipalTransformerAttribute("pattern", "test-pattern2");
        checkRegexPrincipalTransformerAttribute("replacement", "test-replacement2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexPrincipalTransformer_nullName() throws Exception {
        new AddRegexPrincipalTransformer.Builder(null)
                .pattern("test-pattern")
                .replacement("test-replacement")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexPrincipalTransformer_emptyName() throws Exception {
        new AddRegexPrincipalTransformer.Builder("")
                .pattern("test-pattern")
                .replacement("test-replacement")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexPrincipalTransformer_nullPattern() throws Exception {
        new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern(null)
                .replacement("test-replacement")
                .build();
        fail("Creating command with null pattern should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexPrincipalTransformer_emptyPattern() throws Exception {
        new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("")
                .replacement("test-replacement")
                .build();
        fail("Creating command with empty pattern should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addRegexPrincipalTransformer_nullReplacement() throws Exception {
        new AddRegexPrincipalTransformer.Builder(TEST_REGEX_PRINCIPAL_TRANSFORMER_NAME)
                .pattern("test-pattern")
                .replacement(null)
                .build();
        fail("Creating command with null replacement should throw exception");
    }

    private void checkRegexPrincipalTransformerAttribute(String attr, String expected) throws IOException {
        checkAttribute(TEST_REGEX_PRINCIPAL_TRANSFORMER_ADDRESS, attr, expected);
    }
}
