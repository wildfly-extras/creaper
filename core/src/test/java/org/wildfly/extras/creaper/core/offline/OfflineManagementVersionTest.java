package org.wildfly.extras.creaper.core.offline;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ManagementVersion;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OfflineManagementVersionTest {
    private static final String STANDALONE_XML = ""
            + "<server xmlns=\"urn:jboss:domain:%ROOT_VERSION%\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:logging:%LOGGING_VERSION%\">\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:ee:%EE_VERSION%\"/>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String HOST_XML = ""
            + "<host name=\"master\" xmlns=\"urn:jboss:domain:%ROOT_VERSION%\">\n"
            + "</host>\n";

    private static final String DOMAIN_XML = ""
            + "<domain xmlns=\"urn:jboss:domain:%ROOT_VERSION%\">\n"
            + "    <profiles>\n"
            + "        <profile name=\"default\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:%LOGGING_VERSION%\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:ee:%EE_VERSION%\"/>\n"
            + "        </profile>\n"
            + "        <profile name=\"ha\">\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:logging:%LOGGING_VERSION%\"/>\n"
            + "            <subsystem xmlns=\"urn:jboss:domain:ee:%EE_VERSION%\"/>\n"
            + "        </profile>\n"
            + "    </profiles>\n"
            + "</domain>";

    private static final String EAP6_ROOT_VERSION = "1.7";
    private static final String EAP6_LOGGING_VERSION = "1.5";
    private static final String EAP6_EE_VERSION = "1.2";

    private static final String EAP7_ROOT_VERSION = "4.0";
    private static final String EAP7_LOGGING_VERSION = "3.0";
    private static final String EAP7_EE_VERSION = "4.0";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void discoverStandaloneXml_eap6() throws IOException {
        test(ManagementVersion.VERSION_1_7_0, STANDALONE_XML, EAP6_ROOT_VERSION, EAP6_LOGGING_VERSION, EAP6_EE_VERSION);
    }

    @Test
    public void discoverStandaloneXml_eap7() throws IOException {
        test(ManagementVersion.VERSION_4_0_0, STANDALONE_XML, EAP7_ROOT_VERSION, EAP7_LOGGING_VERSION, EAP7_EE_VERSION);
    }

    @Test
    public void discoverHostXml_eap6() throws IOException {
        test(ManagementVersion.VERSION_1_7_0, HOST_XML, EAP6_ROOT_VERSION, EAP6_LOGGING_VERSION, EAP6_EE_VERSION);
    }

    @Test
    public void discoverHostXml_eap7() throws IOException {
        test(ManagementVersion.VERSION_4_0_0, HOST_XML, EAP7_ROOT_VERSION, EAP7_LOGGING_VERSION, EAP7_EE_VERSION);
    }

    @Test
    public void discoverDomainXml_eap6() throws IOException {
        test(ManagementVersion.VERSION_1_7_0, DOMAIN_XML, EAP6_ROOT_VERSION, EAP6_LOGGING_VERSION, EAP6_EE_VERSION);
    }

    @Test
    public void discoverDomainXml_eap7() throws IOException {
        test(ManagementVersion.VERSION_4_0_0, DOMAIN_XML, EAP7_ROOT_VERSION, EAP7_LOGGING_VERSION, EAP7_EE_VERSION);
    }

    private void test(ManagementVersion expected, String xmlPattern,
                      String rootVersion, String loggingVersion, String eeVersion) throws IOException {
        String xml = xmlPattern
                .replace("%ROOT_VERSION%", rootVersion)
                .replace("%LOGGING_VERSION%", loggingVersion)
                .replace("%EE_VERSION%", eeVersion);

        File configurationFile = tmp.newFile("test.xml");
        Files.write(xml, configurationFile, Charsets.UTF_8);

        ManagementVersion actual = OfflineManagementVersion.discover(configurationFile);
        assertEquals(expected, actual);
    }
}
