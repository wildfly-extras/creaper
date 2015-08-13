package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

// retained for testing basic backwards compatibility; the XmlTransform class is deprecated
public class XmlTransformTest {
    private static final String ORIGINAL = "<root><foo/></root>";
    private static final String EXPECTED = "<root><bar/></root>";
    private static final String EXPECTED_PARAMETERIZED = "<root><bar param=\"foobar\"/></root>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File cfg;

    @Before
    public void setUpConfigurationFile() throws IOException {
        this.cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ORIGINAL, cfg, Charsets.UTF_8);
    }

    @Test
    public void groovyTransform() throws IOException, CommandFailedException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );

        assertEquals(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new OldExampleTransformation());

        assertEquals(EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void groovyParameterizedTransform() throws IOException, CommandFailedException {
        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build()
        );

        assertEquals(ORIGINAL, Files.toString(cfg, Charsets.UTF_8));

        client.apply(new OldExampleParameterizedTransformation("foobar"));

        assertEquals(EXPECTED_PARAMETERIZED, Files.toString(cfg, Charsets.UTF_8));
    }
}
