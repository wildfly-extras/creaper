package org.wildfly.extras.creaper.core.offline;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ServerVersion;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OfflineServerVersionTest {
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

    private static final String EAP7_LOGGING = "3.0";

    private static final String EAP8_EE = "6.0";

    private static final String WFLY27_ROOT = "20.0";
    private static final String WFLY28_ROOT = "21.0";
    private static final String WFLY29_ROOT = "22.0";
    private static final String WFLY30_ROOT = "23.0";
    private static final String WFLY31_ROOT = "24.0";
    private static final String WFLY32_ROOT = "25.0";
    private static final String WFLY33_ROOT = "26.0";

    private static final String COMMUNITY = "community";
    private static final String PREVIEW = "preview";
    private static final String EXPERIMENTAL = "experimental";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void discoverStandaloneXml_wfly27() throws IOException {
        test(ServerVersion.VERSION_20_0_0, STANDALONE_XML, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_wfly28() throws IOException {
        test(ServerVersion.VERSION_21_0_0, STANDALONE_XML, WFLY28_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_wfly29() throws IOException {
        test(ServerVersion.VERSION_22_0_0, STANDALONE_XML, WFLY29_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_wfly30() throws IOException {
        test(ServerVersion.VERSION_23_0_0, STANDALONE_XML, WFLY30_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_wfly31() throws IOException {
        test(ServerVersion.VERSION_24_0_0, STANDALONE_XML, WFLY31_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_wfly32() throws IOException {
        test(ServerVersion.VERSION_25_0_0, STANDALONE_XML, WFLY32_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_wfly33() throws IOException {
        test(ServerVersion.VERSION_26_0_0, STANDALONE_XML, WFLY33_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    public void discoverStandaloneXml_community() throws IOException {
        test(ServerVersion.VERSION_20_0_0, STANDALONE_XML, COMMUNITY, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_experimental() throws IOException {
        test(ServerVersion.VERSION_20_0_0, STANDALONE_XML, EXPERIMENTAL, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverStandaloneXml_preview() throws IOException {
        test(ServerVersion.VERSION_20_0_0, STANDALONE_XML, PREVIEW, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_wfly27() throws IOException {
        test(ServerVersion.VERSION_20_0_0, HOST_XML, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_wfly28() throws IOException {
        test(ServerVersion.VERSION_21_0_0, HOST_XML, WFLY28_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_wfly29() throws IOException {
        test(ServerVersion.VERSION_22_0_0, HOST_XML, WFLY29_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_wfly30() throws IOException {
        test(ServerVersion.VERSION_23_0_0, HOST_XML, WFLY30_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_wfly31() throws IOException {
        test(ServerVersion.VERSION_24_0_0, HOST_XML, WFLY31_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_wfly32() throws IOException {
        test(ServerVersion.VERSION_25_0_0, HOST_XML, WFLY32_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_wfly33() throws IOException {
        test(ServerVersion.VERSION_26_0_0, HOST_XML, WFLY33_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    public void discoverHostXml_community() throws IOException {
        test(ServerVersion.VERSION_20_0_0, HOST_XML, COMMUNITY, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_preview() throws IOException {
        test(ServerVersion.VERSION_20_0_0, HOST_XML, PREVIEW, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverHostXml_experimental() throws IOException {
        test(ServerVersion.VERSION_20_0_0, HOST_XML, EXPERIMENTAL, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_wfly27() throws IOException {
        test(ServerVersion.VERSION_20_0_0, DOMAIN_XML, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_wfly28() throws IOException {
        test(ServerVersion.VERSION_21_0_0, DOMAIN_XML, WFLY28_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_wfly29() throws IOException {
        test(ServerVersion.VERSION_22_0_0, DOMAIN_XML, WFLY29_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_wfly30() throws IOException {
        test(ServerVersion.VERSION_23_0_0, DOMAIN_XML, WFLY30_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_wfly31() throws IOException {
        test(ServerVersion.VERSION_24_0_0, DOMAIN_XML, WFLY31_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_wfly32() throws IOException {
        test(ServerVersion.VERSION_25_0_0, DOMAIN_XML, WFLY32_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_wfly33() throws IOException {
        test(ServerVersion.VERSION_26_0_0, DOMAIN_XML, WFLY33_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    public void discoverDomainXml_community() throws IOException {
        test(ServerVersion.VERSION_20_0_0, DOMAIN_XML, COMMUNITY, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_preview() throws IOException {
        test(ServerVersion.VERSION_20_0_0, DOMAIN_XML, PREVIEW, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    @Test
    public void discoverDomainXml_experimental() throws IOException {
        test(ServerVersion.VERSION_20_0_0, DOMAIN_XML, EXPERIMENTAL, WFLY27_ROOT, EAP7_LOGGING, EAP8_EE);
    }

    private void test(ServerVersion expected, String xmlPattern,
                      String rootVersion, String loggingVersion, String eeVersion) throws IOException {
        test(expected, xmlPattern, null, rootVersion, loggingVersion, eeVersion);
    }

    private void test(ServerVersion expected, String xmlPattern, String stability,
                      String rootVersion, String loggingVersion, String eeVersion) throws IOException {
        String version = stability == null ? rootVersion : stability + ":" + rootVersion;
        String xml = xmlPattern
                .replace("%ROOT_VERSION%", version)
                .replace("%LOGGING_VERSION%", loggingVersion)
                .replace("%EE_VERSION%", eeVersion);

        File configurationFile = tmp.newFile("test.xml");
        Files.write(xml, configurationFile, Charsets.UTF_8);

        ServerVersion actual = OfflineServerVersion.discover(configurationFile);
        assertEquals(expected, actual);
    }
}
