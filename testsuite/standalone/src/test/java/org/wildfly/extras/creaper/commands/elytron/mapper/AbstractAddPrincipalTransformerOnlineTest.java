package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public class AbstractAddPrincipalTransformerOnlineTest extends AbstractElytronOnlineTest {

    protected static final String TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestConstantPrincipalTransformer";
    protected static final Address TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
    protected static final String TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestConstantPrincipalTransformer2";
    protected static final Address TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
    protected static final String TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestConstantPrincipalTransformer3";
    protected static final Address TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME);

    protected static final List<String> PRINCIPAL_TRANSFORMERS_1_AND_2
            = Arrays.asList(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME, TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
    protected static final List<String> PRINCIPAL_TRANSFORMERS_2_AND_DIFFERENT
            = Arrays.asList(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2, TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME);

    @BeforeClass
    public static void addPrincipalTransformers() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            AddConstantPrincipalTransformer addConstantPrincipalTransformer
                    = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                    .constant("name1")
                    .build();
            AddConstantPrincipalTransformer addConstantPrincipalTransformer2
                    = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                    .constant("name2")
                    .build();
            AddConstantPrincipalTransformer addConstantPrincipalTransformer3
                    = new AddConstantPrincipalTransformer.Builder(TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_NAME)
                    .constant("name3")
                    .build();
            client.apply(addConstantPrincipalTransformer);
            client.apply(addConstantPrincipalTransformer2);
            client.apply(addConstantPrincipalTransformer3);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @AfterClass
    public static void removePrincipalTransformers() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations ops = new Operations(client);
            ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS);
            ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2);
            ops.removeIfExists(TEST_DIFFERENT_PRINCIPAL_TRANSFORMER_ADDRESS);
            Administration administration = new Administration(client);
            administration.reloadIfRequired();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
