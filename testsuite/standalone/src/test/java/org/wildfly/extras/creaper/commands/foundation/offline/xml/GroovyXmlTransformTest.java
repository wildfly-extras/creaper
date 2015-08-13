package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
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
    private static final String ORIGINAL = "<root><foo/></root>";
    private static final String EXPECTED = "<root><bar/></root>";
    private static final String EXPECTED_PARAMETERIZED = "<root><bar param=\"foobar\"/></root>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File cfg;
    private OfflineManagementClient client;

    @BeforeClass
    public static void setUpXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
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
