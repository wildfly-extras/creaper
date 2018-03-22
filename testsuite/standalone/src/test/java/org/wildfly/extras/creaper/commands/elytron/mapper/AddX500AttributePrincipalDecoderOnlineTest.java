package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddX500AttributePrincipalDecoderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME = "CreaperTestX500AttributePrincipalDecoder";
    private static final Address TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("x500-attribute-principal-decoder", TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME);
    private static final String TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME2 = "CreaperTestX500AttributePrincipalDecoder2";
    private static final Address TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("x500-attribute-principal-decoder", TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME2);

    private static final String OID1 = "test-oid1";
    private static final String OID2 = "test-oid2";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS);
        ops.removeIfExists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleX500AttributePrincipalDecoder() throws Exception {
        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid(OID1)
                .build();

        client.apply(addX500AttributePricipalDecoder);

        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS));
    }

    @Test
    public void addTwoX500AttributePrincipalDecoders() throws Exception {
        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid(OID1)
                .build();

        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder2
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME2)
                .oid(OID2)
                .build();

        client.apply(addX500AttributePricipalDecoder);
        client.apply(addX500AttributePricipalDecoder2);

        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS));
        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS2));
    }

    @Test
    public void addFullX500AttributePrincipalDecoder_oid() throws Exception {
        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid(OID1)
                .joiner(":")
                .startSegment(2)
                .maximumSegments(20)
                .reverse(false)
                .convert(true)
                .addRequiredOids(OID1, OID2)
                .build();

        client.apply(addX500AttributePricipalDecoder);

        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS));
        checkX500AttributePrincipalDecoderAttribute("oid", OID1);
        checkX500AttributePrincipalDecoderAttribute("joiner", ":");
        checkX500AttributePrincipalDecoderAttribute("start-segment", "2");
        checkX500AttributePrincipalDecoderAttribute("maximum-segments", "20");
        checkX500AttributePrincipalDecoderAttribute("reverse", "false");
        checkX500AttributePrincipalDecoderAttribute("convert", "true");
        checkX500AttributePrincipalDecoderAttribute("maximum-segments", "20");
        checkX500AttributePrincipalDecoderAttribute("required-oids", Arrays.asList(new String[]{OID1, OID2}));
    }

    @Test
    public void addFullX500AttributePrincipalDecoder_attributeName() throws Exception {
        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .attributeName("cn")
                .joiner(":")
                .startSegment(2)
                .maximumSegments(20)
                .reverse(false)
                .convert(true)
                .build();

        client.apply(addX500AttributePricipalDecoder);

        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS));
        checkX500AttributePrincipalDecoderAttribute("attribute-name", "cn");
        checkX500AttributePrincipalDecoderAttribute("joiner", ":");
        checkX500AttributePrincipalDecoderAttribute("start-segment", "2");
        checkX500AttributePrincipalDecoderAttribute("maximum-segments", "20");
        checkX500AttributePrincipalDecoderAttribute("reverse", "false");
        checkX500AttributePrincipalDecoderAttribute("convert", "true");
        checkX500AttributePrincipalDecoderAttribute("maximum-segments", "20");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistX500AttributePrincipalDecodersNotAllowed() throws Exception {
        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid(OID1)
                .build();

        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder2
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid(OID2)
                .build();

        client.apply(addX500AttributePricipalDecoder);
        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS));

        client.apply(addX500AttributePricipalDecoder2);
        fail("x500-attribute-principal-decoder CreaperTestX500AttributePrincipalDecoder already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistX500AttributePrincipalDecodersAllowed() throws Exception {
        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid(OID1)
                .addRequiredOids(OID1)
                .build();

        AddX500AttributePrincipalDecoder addX500AttributePricipalDecoder2
                = new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid(OID2)
                .addRequiredOids(OID2)
                .replaceExisting()
                .build();

        client.apply(addX500AttributePricipalDecoder);
        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS));
        checkX500AttributePrincipalDecoderAttribute("oid", OID1);
        checkX500AttributePrincipalDecoderAttribute("required-oids", Arrays.asList(OID1));

        client.apply(addX500AttributePricipalDecoder2);
        assertTrue("x500-attribute-principal-decoder should be created",
                ops.exists(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS));
        checkX500AttributePrincipalDecoderAttribute("oid", OID2);
        checkX500AttributePrincipalDecoderAttribute("required-oids", Arrays.asList(OID2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addX500AttributePrincipalDecoder_nullName() throws Exception {
        new AddX500AttributePrincipalDecoder.Builder(null)
                .oid(OID1)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addX500AttributePrincipalDecoder_emptyName() throws Exception {
        new AddX500AttributePrincipalDecoder.Builder("")
                .oid(OID1)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addX500AttributePrincipalDecoder_NoOidAttributeName() throws Exception {
        new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .build();
        fail("Creating command with none oid or attribute-name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addX500AttributePrincipalDecoder_bothOidAttributeName() throws Exception {
        new AddX500AttributePrincipalDecoder.Builder(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_NAME)
                .oid("2.5.4.3")
                .attributeName("dc")
                .build();
        fail("Creating command with empty oid should throw exception");
    }

    private void checkX500AttributePrincipalDecoderAttribute(String attr, String expected) throws IOException {
        checkAttribute(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS, attr, expected);
    }

    private void checkX500AttributePrincipalDecoderAttribute(String attr, List<String> expected) throws IOException {
        checkAttribute(TEST_X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS, attr, expected);
    }
}
