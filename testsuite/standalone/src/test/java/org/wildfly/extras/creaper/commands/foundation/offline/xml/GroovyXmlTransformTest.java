package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.wildfly.extras.creaper.XmlAssert;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class GroovyXmlTransformTest {
    private static final String ORIGINAL = "<server xmlns='urn:jboss:domain:1.7'><foo/></server>";
    private static final String EXPECTED = "<server xmlns='urn:jboss:domain:1.7'><bar/></server>";
    private static final String EXPECTED_PARAMETERIZED = "<server xmlns='urn:jboss:domain:1.7'><bar param=\"foobar\"/></server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File cfg;
    private OfflineManagementClient client;

    @BeforeClass
    public static void setUpXmlUnit() {
        XmlAssert.setIgnoreWhitespace(true);
    }

    @Before
    public void setUpConfigurationFile() throws IOException {
        cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ORIGINAL, cfg, Charsets.UTF_8);

        client = ManagementClient.offline(OfflineOptions.standalone().configurationFile(cfg).build());
    }

    @Test
    public void entireFileTransform() throws IOException, SAXException, CommandFailedException {
        assertXmlIdentical(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new ExampleEntireFileTransformation());
        assertXmlIdentical(EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void simpleTransform() throws IOException, SAXException, CommandFailedException {
        assertXmlIdentical(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new ExampleSimpleTransformation());
        assertXmlIdentical(EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void parameterizedTransform() throws IOException, SAXException, CommandFailedException {
        assertXmlIdentical(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new ExampleParameterizedTransformation("foobar"));
        assertXmlIdentical(EXPECTED_PARAMETERIZED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void subtreeTransform() throws IOException, SAXException, CommandFailedException {
        assertXmlIdentical(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new ExampleSubtreeTransformation());
        assertXmlIdentical(EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void subtreeParameterizedTransform() throws IOException, SAXException, CommandFailedException {
        assertXmlIdentical(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new ExampleSubtreeParameterizedTransformation("foobar"));
        assertXmlIdentical(EXPECTED_PARAMETERIZED, Files.toString(cfg, Charsets.UTF_8));
    }
}
