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

public class AbstractAddPrincipalDecoderOnlineTest extends AbstractElytronOnlineTest {

    protected static final String TEST_CONSTANT_PRINCIPAL_DECODER_NAME = "CreaperTestConstantPrincipalDecoder";
    protected static final Address TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-decoder", TEST_CONSTANT_PRINCIPAL_DECODER_NAME);
    protected static final String TEST_CONSTANT_PRINCIPAL_DECODER_NAME2 = "CreaperTestConstantPrincipalDecoder2";
    protected static final Address TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-principal-decoder", TEST_CONSTANT_PRINCIPAL_DECODER_NAME2);
    protected static final String TEST_DIFFERENT_PRINCIPAL_DECODER_NAME = "CreaperTestConstantPrincipalDecoder3";
    protected static final Address TEST_DIFFERENT_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-decoder", TEST_DIFFERENT_PRINCIPAL_DECODER_NAME);

    protected static final List<String> PRINCIPAL_DECODERS_1_AND_2
            = Arrays.asList(TEST_CONSTANT_PRINCIPAL_DECODER_NAME, TEST_CONSTANT_PRINCIPAL_DECODER_NAME2);
    protected static final List<String> PRINCIPAL_DECODERS_2_AND_DIFFERENT
            = Arrays.asList(TEST_CONSTANT_PRINCIPAL_DECODER_NAME2, TEST_DIFFERENT_PRINCIPAL_DECODER_NAME);

    @BeforeClass
    public static void addPrincipalDecoders() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            AddConstantPrincipalDecoder addConstantPrincipalDecoder
                    = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                    .constant("role1")
                    .build();
            AddConstantPrincipalDecoder addConstantPrincipalDecoder2
                    = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME2)
                    .constant("role2")
                    .build();
            AddConstantPrincipalDecoder addConstantPrincipalDecoder3
                    = new AddConstantPrincipalDecoder.Builder(TEST_DIFFERENT_PRINCIPAL_DECODER_NAME)
                    .constant("role3")
                    .build();
            client.apply(addConstantPrincipalDecoder);
            client.apply(addConstantPrincipalDecoder2);
            client.apply(addConstantPrincipalDecoder3);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @AfterClass
    public static void removePrincipalDecoders() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations ops = new Operations(client);
            ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS);
            ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS2);
            ops.removeIfExists(TEST_DIFFERENT_PRINCIPAL_DECODER_ADDRESS);
            new Administration(client).reloadIfRequired();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
