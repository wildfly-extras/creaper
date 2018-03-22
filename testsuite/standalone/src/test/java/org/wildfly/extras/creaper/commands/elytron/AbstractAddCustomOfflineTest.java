package org.wildfly.extras.creaper.commands.elytron;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

import java.io.File;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public abstract class AbstractAddCustomOfflineTest {

    protected static final String PARENT_TYPE = "PARENT_TYPE";
    protected static final String CUSTOM_TYPE = "CUSTOM_TYPE";
    protected static final String CUSTOM_NAME = "CUSTOM_NAME";
    protected static final String CLASS_NAME = "CLASS_NAME";
    protected static final String MODULE_NAME = "MODULE_NAME";

    private static final String SUBSYSTEM_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_MAPPERS_EMPTY = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <" + PARENT_TYPE + ">\n"
            + "            </" + PARENT_TYPE + ">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SIMPLE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <" + PARENT_TYPE + ">\n"
            + "              <" + CUSTOM_TYPE + " name=\"" + CUSTOM_NAME + "\" class-name=\"" + CLASS_NAME + "\" module=\"" + MODULE_NAME + "\">"
            + "                <configuration>"
            + "                  <property name=\"alias1\" value=\"value1\"/>"
            + "                  <property name=\"alias2\" value=\"value2\"/>"
            + "                </configuration>"
            + "              </" + CUSTOM_TYPE + ">"
            + "            </" + PARENT_TYPE + ">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <" + PARENT_TYPE + ">\n"
            + "              <" + CUSTOM_TYPE + " name=\"" + CUSTOM_NAME + "\" class-name=\"" + CLASS_NAME + "\" module=\"" + MODULE_NAME + "\">"
            + "                <configuration>"
            + "                  <property name=\"alias123\" value=\"value123\"/>"
            + "                </configuration>"
            + "              </" + CUSTOM_TYPE + ">"
            + "            </" + PARENT_TYPE + ">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SECOND_CUSTOM_REALM_MAPPER = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <" + PARENT_TYPE + ">\n"
            + "              <" + CUSTOM_TYPE + " name=\"" + CUSTOM_NAME + "\" class-name=\"" + CLASS_NAME + "\" module=\"" + MODULE_NAME + "\">"
            + "                <configuration>"
            + "                  <property name=\"alias1\" value=\"value1\"/>"
            + "                  <property name=\"alias2\" value=\"value2\"/>"
            + "                </configuration>"
            + "              </" + CUSTOM_TYPE + ">"
            + "              <" + CUSTOM_TYPE + " name=\"" + CUSTOM_NAME + "_SUFFIX2\" class-name=\"" + CLASS_NAME + "\" module=\"" + MODULE_NAME + "\">"
            + "                <configuration>"
            + "                  <property name=\"alias3\" value=\"value3\"/>"
            + "                </configuration>"
            + "              </" + CUSTOM_TYPE + ">"
            + "            </" + PARENT_TYPE + ">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_FULL = ""
            + "<server xmlns=\"urn:jboss:domain:5.0\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:wildfly:elytron:1.0\">\n"
            + "            <" + PARENT_TYPE + ">\n"
            + "              <" + CUSTOM_TYPE + " name=\"" + CUSTOM_NAME + "\" class-name=\"" + CLASS_NAME + "\" module=\"" + MODULE_NAME + "\">"
            + "                <configuration>"
            + "                  <property name=\"alias1\" value=\"value1\"/>"
            + "                  <property name=\"alias2\" value=\"value2\"/>"
            + "                </configuration>"
            + "              </" + CUSTOM_TYPE + ">"
            + "            </" + PARENT_TYPE + ">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void addSimpleToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(convertSubsystem(SUBSYSTEM_EMPTY), cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AbstractAddCustom addCustom = getBuilder(getAddCustomName())
            .className(getClassName())
            .module(getModuleName())
                .addConfiguration("alias1", "value1")
                .addConfiguration("alias2", "value2")
                .build();

        assertXmlIdentical(convertSubsystem(SUBSYSTEM_EMPTY), Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCustom);
        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SIMPLE), Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSimpleToMappersEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(convertSubsystem(SUBSYSTEM_MAPPERS_EMPTY), cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AbstractAddCustom addCustom = getBuilder(getAddCustomName())
            .className(getClassName())
            .module(getModuleName())
             .addConfiguration("alias1", "value1")
             .addConfiguration("alias2", "value2")
            .build();

        assertXmlIdentical(convertSubsystem(SUBSYSTEM_MAPPERS_EMPTY), Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCustom);
        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SIMPLE), Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(convertSubsystem(SUBSYSTEM_SIMPLE), cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AbstractAddCustom addCustom = getBuilder(getAddCustomName())
            .className(getClassName())
            .module(getModuleName())
            .addConfiguration("alias1", "value1")
            .addConfiguration("alias2", "value2")
            .build();

        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SIMPLE), Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCustom);

        fail("Custom realm mapper customRoleMapper already exists in configuration, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(convertSubsystem(SUBSYSTEM_SIMPLE), cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AbstractAddCustom addCustom = getBuilder(getAddCustomName())
            .className(getClassName())
            .module(getModuleName())
            .addConfiguration("alias123", "value123")
            .replaceExisting()
            .build();

        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SIMPLE), Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCustom);
        assertXmlIdentical(convertSubsystem(SUBSYSTEM_EXPECTED_REPLACE), Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideNonExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(convertSubsystem(SUBSYSTEM_SIMPLE), cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AbstractAddCustom addCustom = getBuilder(getAddCustomName() + "_SUFFIX2")
            .className(getClassName())
            .module(getModuleName())
            .addConfiguration("alias3", "value3")
            .replaceExisting()
            .build();

        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SIMPLE), Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCustom);
        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SECOND_CUSTOM_REALM_MAPPER), Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addSecondCustomRoleMapper() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(convertSubsystem(SUBSYSTEM_SIMPLE), cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AbstractAddCustom addCustom = getBuilder(getAddCustomName() + "_SUFFIX2")
            .className(getClassName())
            .module(getModuleName())
            .addConfiguration("alias3", "value3")
            .build();

        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SIMPLE), Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCustom);
        assertXmlIdentical(convertSubsystem(SUBSYSTEM_SECOND_CUSTOM_REALM_MAPPER), Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addFullToEmpty() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(convertSubsystem(SUBSYSTEM_EMPTY), cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AbstractAddCustom addCustom = getBuilder(getAddCustomName())
            .className(getClassName())
            .module(getModuleName())
            .addConfiguration("alias1", "value1")
            .addConfiguration("alias2", "value2")
            .build();

        assertXmlIdentical(convertSubsystem(SUBSYSTEM_EMPTY), Files.toString(cfg, Charsets.UTF_8));
        client.apply(addCustom);
        assertXmlIdentical(convertSubsystem(SUBSYSTEM_FULL), Files.toString(cfg, Charsets.UTF_8));
    }

    public abstract String convertSubsystem(String subsystemString);

    public abstract String getAddCustomName();

    public AbstractAddCustom.Builder<AbstractAddCustom.Builder> getBuilder(String name) {
        return (AbstractAddCustom.Builder<AbstractAddCustom.Builder>) getBuilderObject(name);
    }

    public abstract Object getBuilderObject(String name);

    public abstract String getModuleName();

    public abstract String getClassName();
}
