package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.elytron.dircontext.AddDirContext;
import org.wildfly.extras.creaper.commands.elytron.tls.AddLdapKeyStore.NewItemAttributeBuilder;
import org.wildfly.extras.creaper.commands.elytron.tls.AddLdapKeyStore.NewItemTemplateBuilder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddLdapKeyStoreOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_DIR_CONTEXT_NAME = "CreaperTestDirContext";
    private static final Address TEST_DIR_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS.and("dir-context",
            TEST_DIR_CONTEXT_NAME);
    private static final String TEST_LDAP_KEY_STORE_NAME = "CreaperTestLdapKeyStore";
    private static final String TEST_LDAP_KEY_STORE_NAME2 = "CreaperTestLdapKeyStore2";
    private static final Address TEST_LDAP_KEY_STORE_ADDRESS = SUBSYSTEM_ADDRESS .and("ldap-key-store",
            TEST_LDAP_KEY_STORE_NAME);
    private static final Address TEST_LDAP_KEY_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS .and("ldap-key-store",
            TEST_LDAP_KEY_STORE_NAME2);

    private static final String TEST_SEARCH_PATH = "CN=Users";

    private final AddDirContext addDirContext = new AddDirContext.Builder(TEST_DIR_CONTEXT_NAME)
            .url("ldap://localhost")
            .principal("CN=user")
            .credentialReference(new CredentialRef.CredentialRefBuilder()
                    .clearText("password")
                    .build())
            .authenticationLevel(AddDirContext.AuthenticationLevel.SIMPLE)
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_LDAP_KEY_STORE_ADDRESS);
        ops.removeIfExists(TEST_LDAP_KEY_STORE_ADDRESS2);
        ops.removeIfExists(TEST_DIR_CONTEXT_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleLdapKeyStore() throws Exception {

        client.apply(addDirContext);

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .build();

        client.apply(addLdapKeyStore);
        assertTrue("Ldap key store should be created", ops.exists(TEST_LDAP_KEY_STORE_ADDRESS));
    }

    @Test
    public void addTwoSimpleLdapKeyStores() throws Exception {
        client.apply(addDirContext);

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .build();

        AddLdapKeyStore addLdapKeyStore2 = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME2)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .build();

        client.apply(addLdapKeyStore);
        client.apply(addLdapKeyStore2);

        assertTrue("Ldap key store should be created", ops.exists(TEST_LDAP_KEY_STORE_ADDRESS));
        assertTrue("Second ldap key store should be created", ops.exists(TEST_LDAP_KEY_STORE_ADDRESS2));
    }

    @Test
    public void addFullLdapKeyStore() throws Exception {

        client.apply(addDirContext);

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .filterAlias("filter-alias1")
                .filterCertificate("filter-certificate1")
                .filterIterate("filter-iterate1")
                .searchRecursive(true)
                .searchTimeLimit(1)
                .aliasAttribute("aliasAttribute1")
                .certificateAttribute("certificateAttribute1")
                .certificateChainAttribute("certificateChainAttribute1")
                .certificateChainEncoding("certificateChainEncoding1")
                .certificateType("certificateType1")
                .keyAttribute("someKeystoreAttribute")
                .keyType("someKeyType")
                .newItemTemplate(new NewItemTemplateBuilder()
                        .newItemPath("CN=user")
                        .newItemRdn("DN=Users")
                        .addNewItemAttributes(
                                new NewItemAttributeBuilder()
                                .name("name1")
                                .addValues("value 1", "value2")
                                .build(),
                                new NewItemAttributeBuilder()
                                .name("name2")
                                .addValues("value3", "value4")
                                .build())
                        .build())
                .build();

        client.apply(addLdapKeyStore);

        assertTrue("Ldap key store should be created", ops.exists(TEST_LDAP_KEY_STORE_ADDRESS));

        checkAttribute("dir-context", TEST_DIR_CONTEXT_NAME);
        checkAttribute("search-path", TEST_SEARCH_PATH);
        checkAttribute("filter-alias", "filter-alias1");
        checkAttribute("filter-certificate", "filter-certificate1");
        checkAttribute("filter-iterate", "filter-iterate1");
        checkAttribute("search-recursive", "true");
        checkAttribute("search-time-limit", "1");
        checkAttribute("alias-attribute", "aliasAttribute1");
        checkAttribute("certificate-attribute", "certificateAttribute1");
        checkAttribute("certificate-chain-attribute", "certificateChainAttribute1");
        checkAttribute("certificate-chain-encoding", "certificateChainEncoding1");
        checkAttribute("certificate-type", "certificateType1");
        checkAttribute("key-attribute", "someKeystoreAttribute");
        checkAttribute("key-type", "someKeyType");
        checkAttribute("new-item-template.new-item-path", "CN=user");
        checkAttribute("new-item-template.new-item-rdn", "DN=Users");
        checkAttribute("new-item-template.new-item-attributes[0].name", "name1");
        checkAttribute("new-item-template.new-item-attributes[0].value[0]", "value 1");
        checkAttribute("new-item-template.new-item-attributes[0].value[1]", "value2");
        checkAttribute("new-item-template.new-item-attributes[1].name", "name2");
        checkAttribute("new-item-template.new-item-attributes[1].value[0]", "value3");
        checkAttribute("new-item-template.new-item-attributes[1].value[1]", "value4");
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateLdapKeyStoreNotAllowed() throws Exception {
        client.apply(addDirContext);

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .build();

        AddLdapKeyStore addLdapKeyStore2 = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                 .build();

        client.apply(addLdapKeyStore);
        assertTrue("Ldap key store should be created", ops.exists(TEST_LDAP_KEY_STORE_ADDRESS));
        client.apply(addLdapKeyStore2);
        fail("Ldap key store " + TEST_LDAP_KEY_STORE_NAME + "already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateLdapKeyStoreAllowed() throws Exception {
        client.apply(addDirContext);

        AddLdapKeyStore addLdapKeyStore = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .searchTimeLimit(1000)
                .build();

        AddLdapKeyStore addLdapKeyStore2 = new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .searchTimeLimit(2000)
                .replaceExisting()
                .build();

        client.apply(addLdapKeyStore);
        assertTrue("Ldap realm should be created", ops.exists(TEST_LDAP_KEY_STORE_ADDRESS));
        client.apply(addLdapKeyStore2);
        assertTrue("Ldap realm should be created", ops.exists(TEST_LDAP_KEY_STORE_ADDRESS));
        // check whether it was really rewritten
        checkAttribute("search-time-limit", "2000");

    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_nullName() throws Exception {
        new AddLdapKeyStore.Builder(null)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .build();
        fail("Creating command with null ldap keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_emptyName() throws Exception {
        new AddLdapKeyStore.Builder("")
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .build();
        fail("Creating command with empty ldap keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_nullDirContext() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(null)
                .searchPath(TEST_SEARCH_PATH)
                .build();
        fail("Creating command with null ldap keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_emptyDirContext() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext("")
                .searchPath(TEST_SEARCH_PATH)
                .build();
        fail("Creating command with empty ldap keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_nullSearchPath() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(null)
                .build();
        fail("Creating command with null ldap keystore search path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_emptySearchPath() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath("")
                .build();
        fail("Creating command with empty ldap keystore search path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_nullNewItemPath_newItemTemplate() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .newItemTemplate(new NewItemTemplateBuilder()
                        .newItemPath(null)
                        .newItemRdn("DN=Users")
                        .build())
                .build();
        fail("Creating command with null ldap keystore new-item-template.new-item-path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_emptyNewItemPath_newItemTemplate() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .newItemTemplate(new NewItemTemplateBuilder()
                        .newItemPath("")
                        .newItemRdn("DN=Users")
                        .build())
                .build();
        fail("Creating command with empty ldap keystore new-item-template.new-item-path should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_nullNewItemRdn_newItemTemplate() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .newItemTemplate(new NewItemTemplateBuilder()
                        .newItemPath("CN=user")
                        .newItemRdn(null)
                        .build())
                .build();
        fail("Creating command with null ldap keystore new-item-template.new-item-rdn should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLdapKeyStore_emptyNewItemRdn_newItemTemplate() throws Exception {
        new AddLdapKeyStore.Builder(TEST_LDAP_KEY_STORE_NAME)
                .dirContext(TEST_DIR_CONTEXT_NAME)
                .searchPath(TEST_SEARCH_PATH)
                .newItemTemplate(new NewItemTemplateBuilder()
                        .newItemPath("CN=user")
                        .newItemRdn("")
                        .build())
                .build();
        fail("Creating command with empty ldap keystore new-item-template.new-item-rdn should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_LDAP_KEY_STORE_ADDRESS, attribute, expectedValue);
    }

}
