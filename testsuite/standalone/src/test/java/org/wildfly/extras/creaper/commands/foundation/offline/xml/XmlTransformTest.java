package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

// retained for testing basic backwards compatibility; the XmlTransform class is deprecated
@Deprecated
public class XmlTransformTest {
    private static final String ORIGINAL = "<server xmlns='urn:jboss:domain:1.7'><foo/></server>";
    private static final String EXPECTED = "<server xmlns='urn:jboss:domain:1.7'><bar/></server>";
    private static final String EXPECTED_PARAMETERIZED = "<server xmlns='urn:jboss:domain:1.7'><bar param=\"foobar\"/></server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File cfg;

    @Before
    public void setUpConfigurationFile() throws IOException {
        this.cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ORIGINAL, cfg, Charsets.UTF_8);
    }

    @Test
    public void groovyTransform() throws IOException, CommandFailedException, SAXException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );

        assertXmlIdentical(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new OldExampleTransformation());

        assertXmlIdentical(EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void groovyParameterizedTransform() throws IOException, CommandFailedException, SAXException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );

        assertXmlIdentical(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new OldExampleParameterizedTransformation("foobar"));

        assertXmlIdentical(EXPECTED_PARAMETERIZED, Files.toString(cfg, Charsets.UTF_8));
    }
}
