package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

@RunWith(Parameterized.class)
public class GroovyXmlTransformElementOrderingTest {
    // universal (both domain.xml and host.xml)
    // most of it is actually shared with GroovyXmlTransformStandaloneTest, which proves that the Subtree concept works

    private static final String ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "    </extensions>\n"
            + "    <management></management>\n"
            + "</server>";

    private static final String SYSTEM_PROPERTIES_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "    </extensions>\n"
            + "    <system-properties>\n"
            + "        <property name=\"foo\" value=\"bar\"/>\n"
            + "    </system-properties>\n"
            + "    <management></management>\n"
            + "</server>";
    private static final String PATHS_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "    </extensions>\n"
            + "    <paths>\n"
            + "        <path name=\"foobar\" path=\"foobar\" relative-to=\"jboss.home.dir\"/>\n"
            + "    </paths>\n"
            + "    <management></management>\n"
            + "</server>";
    private static final String SYSTEM_PROPERTIES_AND_PATHS_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <extensions>\n"
            + "        <extension module=\"org.jboss.as.ee\"/>\n"
            + "    </extensions>\n"
            + "    <system-properties>\n"
            + "        <property name=\"foo\" value=\"bar\"/>\n"
            + "    </system-properties>\n"
            + "    <paths>\n"
            + "        <path name=\"foobar\" path=\"foobar\" relative-to=\"jboss.home.dir\"/>\n"
            + "    </paths>\n"
            + "    <management></management>\n"
            + "</server>";

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                {"system-properties", ORIGINAL, SYSTEM_PROPERTIES_EXPECTED,
                        ExampleSubtreeSystemPropertiesTransformation.class},
                {"paths", ORIGINAL, PATHS_EXPECTED, ExampleSubtreePathsTransformation.class},
                {"system-properties & paths", ORIGINAL, SYSTEM_PROPERTIES_AND_PATHS_EXPECTED,
                        ExampleSubtreeSystemPropertiesAndPathsTransformation.class},
                {"paths & system-properties", ORIGINAL, SYSTEM_PROPERTIES_AND_PATHS_EXPECTED,
                        ExampleSubtreePathsAndSystemPropertiesTransformation.class},
        });
    }

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Parameterized.Parameter(0)
    public String testName; // unused, but JUnit insists on that

    @Parameterized.Parameter(1)
    public String originalXml;

    @Parameterized.Parameter(2)
    public String expectedXml;

    @Parameterized.Parameter(3)
    public Class<? extends OfflineCommand> transformationCommand;

    @BeforeClass
    public static void setUpXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void transform() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(originalXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(originalXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(transformationCommand.newInstance());
        assertXmlIdentical(expectedXml, Files.toString(cfg, Charsets.UTF_8));
    }
}
