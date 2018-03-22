package org.wildfly.extras.creaper.commands.elytron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public abstract class AbstractElytronOnlineTest {

    protected OnlineManagementClient client;
    protected Operations ops;
    protected Administration administration;

    protected static final Address SUBSYSTEM_ADDRESS = Address.subsystem("elytron");

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeTrue("Elytron is available since WildFly 11.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_5_0_0));
    }

    @Before
    public void setupCreaperForTest() throws IOException {
        client = createManagementClient();
        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void tearDownCreaperForTest() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    protected static OnlineManagementClient createManagementClient() throws IOException {
        return ManagementClient.online(OnlineOptions.standalone().localDefault().build());
    }

    protected void removeAllElytronChildrenType(final String childrenType) throws IOException, OperationException {
        Operations ops = new Operations(client);
        ModelNodeResult result = ops.readChildrenNames(SUBSYSTEM_ADDRESS, childrenType);
        List<String> realmNames = result.stringListValue();

        for (String realmName : realmNames) {
            final Address realmAddress = SUBSYSTEM_ADDRESS.and(childrenType, realmName);

            ops.removeIfExists(realmAddress);
        }
    }

    protected void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

    protected void checkAttribute(Address address, String attribute, List<String> expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return unexpected value", expectedValue,
                readAttribute.stringListValue());
    }

    protected void checkAttributeObject(Address address, String attribute, String objectProperty, String expectedValue)
        throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
            readAttribute.asObject().get(Constants.RESULT).get(objectProperty).asString());
    }

    protected void checkAttributeProperties(Address address, String attribute, List<Property> expectedValues)
        throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        ModelNode result = readAttribute.get(Constants.RESULT);
        List<Property> propertyList = result.asPropertyList();

        if (propertyList.size() != expectedValues.size()) {
            fail("Configuration properties size must be same as expected values size. Was [" + propertyList.size()
                + "] and matches [" + expectedValues.size() + "]");
        }

        int numberOfMatches = 0;
        for (Property property : propertyList) {
            for (Property expected : expectedValues) {
                if (property.getName().equals(expected.getName()) && property.getValue().equals(expected.getValue())) {
                    numberOfMatches++;
                }
            }
        }

        if (propertyList.size() != numberOfMatches) {
            fail("Configuration properties size must be same as number of matches. Was [" + propertyList.size()
                + "] and matches [" + numberOfMatches + "]");
        }
    }

    /**
     *
     * @param namePrefix - prefix of JAR name
     * @param classes - classes which will be added to JAR
     * @return - JAR file
     * @throws IOException - exception
     */
    protected static File createJar(String namePrefix, Class<?>... classes) throws IOException {
        File testJar = File.createTempFile(namePrefix, ".jar");
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
            .addClasses(classes);
        jar.as(ZipExporter.class).exportTo(testJar, true);
        return testJar;
    }
}
