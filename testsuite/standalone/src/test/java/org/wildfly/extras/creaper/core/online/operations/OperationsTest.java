package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.ManagementVersionPart;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

@RunWith(Arquillian.class)
public class OperationsTest {
    private OnlineManagementClient client;
    private Operations ops;

    private String webSubsystem;
    private Address defaultHostAddress;
    private Address jspConfigurationAddress;
    private Address httpConnectorAddress;
    private String requestCountAttribute;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);

        if (client.serverVersion().lessThan(ManagementVersion.VERSION_2_0_0)) { // AS7, JBoss Web
            webSubsystem = "web";
            defaultHostAddress = Address.subsystem("web").and("virtual-server", "default-host");
            jspConfigurationAddress = Address.subsystem("web").and("configuration", "jsp-configuration");
            httpConnectorAddress = Address.subsystem("web").and("connector", "http");
            requestCountAttribute = "requestCount";
        } else { // WildFly, Undertow
            webSubsystem = "undertow";
            defaultHostAddress = Address.subsystem("undertow").and("server", "default-server").and("host", "default-host");
            jspConfigurationAddress = Address.subsystem("undertow").and("servlet-container", "default").and("setting", "jsp");
            httpConnectorAddress = Address.subsystem("undertow").and("server", "default-server").and("http-listener", "default");
            requestCountAttribute = "request-count";
        }
    }

    @After
    public void close() throws IOException {
        client.close();
    }

    @Test
    public void headers() throws Exception {
        // WildFly 8 doesn't require "reload" after removing a socket binding (why?)
        assumeFalse("This test can't work on WildFly 8 (but works on AS7 and WildFly >= 9)",
                client.serverVersion().inRange(ManagementVersion.VERSION_2_0_0, ManagementVersion.VERSION_2_2_0));

        Operations ops = new Operations(client);
        Administration admin = new Administration(client);

        String socketBindingName = "creaper-test-socket-binding";
        Address socketBindingAddress = Address.of("socket-binding-group", "standard-sockets")
                .and("socket-binding", socketBindingName);
        SingleOperation.DeferredOperation addSocketBinding = new SingleOperation().add(socketBindingAddress,
                Values.of("port", 12345));

        addSocketBinding.invoke(client);
        ModelNodeResult result = ops.remove(socketBindingAddress);

        assertTrue(result.isReloadRequired());

        admin.reload();

        addSocketBinding.invoke(client);
        result = ops.headers(Headers.allowResourceServiceRestart()).remove(socketBindingAddress);

        assertFalse(result.isReloadRequired());
    }

    @Test
    public void whoami() throws IOException {
        ops.whoami().assertSuccess();
    }

    @Test
    public void readAttribute() throws IOException {
        ModelNodeResult result = ops.readAttribute(Address.root(), ManagementVersionPart.MAJOR.attributeName());
        result.assertDefinedValue();
        assertTrue(result.intValue() >= 0);
    }

    @Test
    public void readAttribute_stringList() throws IOException {
        ModelNodeResult result = ops.readAttribute(defaultHostAddress, "alias");
        result.assertDefinedValue();
        List<String> aliases = result.stringListValue();
        assertTrue(aliases.contains("localhost"));
    }

    @Test
    public void readAttribute_notIncludeDefaults() throws IOException {
        ModelNodeResult result = ops.readAttribute(jspConfigurationAddress, "development",
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertNotDefinedValue();
    }

    @Test
    public void writeAttribute_boolean() throws IOException {
        ModelNodeResult result = ops.writeAttribute(jspConfigurationAddress, "display-source-fragment", false);
        result.assertSuccess();

        result = ops.readAttribute(jspConfigurationAddress, "display-source-fragment",
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertFalse(result.booleanValue());
    }

    @Test
    public void writeAttribute_int() throws IOException {
        ModelNodeResult result = ops.writeAttribute(jspConfigurationAddress, "check-interval", 42);
        result.assertSuccess();

        result = ops.readAttribute(jspConfigurationAddress, "check-interval", ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertEquals(42, result.intValue());
    }

    @Test
    public void writeAttribute_long() throws IOException {
        Address address = Address.subsystem("datasources").and("data-source", "ExampleDS");

        ModelNodeResult result = ops.writeAttribute(address, "query-timeout", 42L);
        result.assertSuccess();

        result = ops.readAttribute(address, "query-timeout");
        result.assertDefinedValue();
        assertEquals(42L, result.longValue());
    }

    @Test
    public void writeAttribute_string() throws IOException {
        ModelNodeResult result = ops.writeAttribute(jspConfigurationAddress, "target-vm", "1.6");
        result.assertSuccess();

        result = ops.readAttribute(jspConfigurationAddress, "target-vm", ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertEquals("1.6", result.stringValue());
    }

    @Test
    public void writeAttribute_modelNode() throws IOException {
        ModelNodeResult result = ops.writeAttribute(jspConfigurationAddress, "modification-test-interval",
                new ModelNode(13));
        result.assertSuccess();

        result = ops.readAttribute(jspConfigurationAddress, "modification-test-interval",
                ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertEquals(13, result.intValue());
    }

    @Test
    public void writeListAttribute() throws IOException {
        ModelNodeResult result = ops.writeListAttribute(defaultHostAddress, "alias",
                "localhost", "example.com", "bagr.example.com");
        result.assertSuccess();

        result = ops.readAttribute(defaultHostAddress, "alias");
        result.assertDefinedValue();
        List<String> aliases = result.stringListValue();
        assertTrue(aliases.contains("bagr.example.com"));
    }

    @Test
    public void undefineAttribute() throws IOException {
        ModelNodeResult result = ops.writeAttribute(jspConfigurationAddress, "trim-spaces", true);
        result.assertSuccess();

        result = ops.readAttribute(jspConfigurationAddress, "trim-spaces", ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertTrue(result.booleanValue());

        result = ops.undefineAttribute(jspConfigurationAddress, "trim-spaces");
        result.assertSuccess();

        result = ops.readAttribute(jspConfigurationAddress, "trim-spaces", ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertNotDefinedValue();
    }

    @Test
    public void readResource() throws IOException {
        ModelNodeResult result = ops.readResource(jspConfigurationAddress);
        result.assertDefinedValue();
        assertTrue(result.value().hasDefined("development"));
    }

    @Test
    public void readResource_notIncludeDefaults() throws IOException {
        ModelNodeResult result = ops.readResource(jspConfigurationAddress, ReadResourceOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertFalse(result.value().hasDefined("development"));
    }

    @Test
    public void readResource_recursive() throws IOException {
        ModelNodeResult result = ops.readResource(Address.subsystem(webSubsystem), ReadResourceOption.RECURSIVE);
        result.assertDefinedValue();
        if (client.serverVersion().lessThan(ManagementVersion.VERSION_2_0_0)) {
            assertTrue(result.value().get("configuration", "jsp-configuration").hasDefined("development"));
        } else {
            assertTrue(result.value().get("servlet-container", "default", "setting", "jsp").hasDefined("development"));
        }
    }

    @Test
    public void readResource_recursive_notIncludeDefaults() throws IOException {
        ModelNodeResult result = ops.readResource(Address.subsystem(webSubsystem),
                ReadResourceOption.RECURSIVE, ReadResourceOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        if (client.serverVersion().lessThan(ManagementVersion.VERSION_2_0_0)) {
            assertFalse(result.value().get("configuration", "jsp-configuration").hasDefined("development"));
        } else {
            assertFalse(result.value().get("servlet-container", "default", "setting", "jsp").hasDefined("development"));
        }
    }

    @Test
    public void readResource_includeRuntime() throws IOException {
        // WildFly 8 doesn't have the "request-count" attribute in the "undertow" subsystem
        assumeFalse("This test can't work on WildFly 8 (but works on AS7 and WildFly >= 9)",
                client.serverVersion().inRange(ManagementVersion.VERSION_2_0_0, ManagementVersion.VERSION_2_2_0));

        ModelNodeResult result = ops.readResource(httpConnectorAddress, ReadResourceOption.INCLUDE_RUNTIME);
        result.assertDefinedValue();
        assertTrue("Runtime value should be shown", result.value().hasDefined(requestCountAttribute));
    }

    @Test
    public void readResource_notIncludeRuntime() throws IOException {
        // WildFly 8 doesn't have the "request-count" attribute in the "undertow" subsystem
        assumeFalse("This test can't work on WildFly 8 (but works on AS7 and WildFly >= 9)",
                client.serverVersion().inRange(ManagementVersion.VERSION_2_0_0, ManagementVersion.VERSION_2_2_0));

        ModelNodeResult result = ops.readResource(httpConnectorAddress, ReadResourceOption.NOT_INCLUDE_RUNTIME);
        result.assertDefinedValue();
        assertFalse("Runtime value shouldn't be shown", result.value().hasDefined(requestCountAttribute));
    }

    @Test
    public void readResource_recursive_includeRuntime() throws IOException {
        // WildFly 8 doesn't have the "request-count" attribute in the "undertow" subsystem
        assumeFalse("This test can't work on WildFly 8 (but works on AS7 and WildFly >= 9)",
                client.serverVersion().inRange(ManagementVersion.VERSION_2_0_0, ManagementVersion.VERSION_2_2_0));

        ModelNodeResult result = ops.readResource(Address.subsystem(webSubsystem), ReadResourceOption.INCLUDE_RUNTIME,
                ReadResourceOption.RECURSIVE);
        result.assertDefinedValue();
        if (client.serverVersion().lessThan(ManagementVersion.VERSION_2_0_0)) {
            assertTrue(result.value().get("connector", "http").hasDefined("requestCount"));
        } else {
            assertTrue(result.value().get("server", "default-server", "http-listener", "default").hasDefined("request-count"));
        }
    }

    @Test
    public void readChildrenNames() throws IOException {
        ModelNodeResult result = ops.readChildrenNames(Address.root(), Constants.SUBSYSTEM);
        result.assertDefinedValue();
        assertTrue(result.stringListValue().contains("infinispan"));
    }

    @Test
    public void addRemove_noParameters() throws IOException {
        Address address = Address.subsystem("infinispan").and("cache-container", "foo");

        ModelNodeResult result = ops.add(address);
        result.assertSuccess();

        result = ops.readAttribute(address, "statistics-enabled", ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertNotDefinedValue();

        result = ops.remove(address);
        result.assertSuccess();
    }

    @Test
    public void addRemove_withParameter() throws IOException {
        Address address = Address.subsystem("infinispan").and("cache-container", "foo");

        ModelNodeResult result = ops.add(address, Values.of("statistics-enabled", true));
        result.assertSuccess();

        result = ops.readAttribute(address, "statistics-enabled", ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertTrue(result.booleanValue());

        result = ops.remove(address);
        result.assertSuccess();
    }

    @Test
    public void addRemove_withListParameter() throws IOException {
        Address address = Address.subsystem("infinispan").and("cache-container", "foo");

        ModelNodeResult result = ops.add(address, Values.ofList("aliases", "bar", "baz"));
        result.assertSuccess();

        result = ops.readAttribute(address, "aliases");
        result.assertDefinedValue();
        List<String> aliases = result.stringListValue();
        assertTrue(aliases.contains("bar"));

        result = ops.remove(address);
        result.assertSuccess();
    }

    @Test
    public void addRemove_withMultipleParameters() throws IOException {
        Address address = Address.subsystem("infinispan").and("cache-container", "foo");

        ModelNodeResult result = ops.add(address, Values.of("statistics-enabled", true)
                .andList("aliases", "bar", "baz"));
        result.assertSuccess();

        result = ops.readAttribute(address, "statistics-enabled", ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
        result.assertDefinedValue();
        assertTrue(result.booleanValue());

        result = ops.readAttribute(address, "aliases");
        result.assertDefinedValue();
        List<String> aliases = result.stringListValue();
        assertTrue(aliases.contains("bar"));

        result = ops.remove(address);
        result.assertSuccess();
    }

    @Test
    public void invoke_noParameters() throws IOException {
        ModelNodeResult result = ops.invoke("list-snapshots", Address.root());
        result.assertDefinedValue();
    }

    @Test
    public void invoke_withParameters() throws IOException {
        ModelNodeResult result = ops.invoke("read-children-names", Address.root(),
                Values.of("child-type", "subsystem"));
        result.assertDefinedValue();
        assertTrue(result.stringListValue().contains("infinispan"));
    }

    // ---

    @Test
    public void batch() throws IOException {
        ModelNodeResult result = ops.batch(new Batch()
                .add(Address.subsystem("infinispan").and("cache-container", "foo"),
                        Values.of("statistics-enabled", true))
                .add(Address.subsystem("infinispan").and("cache-container", "bar"),
                        Values.of("statistics-enabled", true))
        );
        result.assertSuccess();

        result = ops.batch(new Batch()
                .remove(Address.subsystem("infinispan").and("cache-container", "foo"))
                .remove(Address.subsystem("infinispan").and("cache-container", "bar"))
        );
        result.assertSuccess();
    }

    @Test
    public void exists() throws IOException, OperationException {
        assertTrue(ops.exists(Address.root()));
        assertTrue(ops.exists(Address.subsystem("infinispan")));
        assertTrue(ops.exists(Address.subsystem("infinispan").and("cache-container", "web")));
        assertFalse(ops.exists(Address.subsystem("infinispan").and("cache-container", "nonexisting-cache-container")));
        assertFalse(ops.exists(Address.of("foo", "bar")));
    }

    @Test
    public void removeIfExists() throws IOException, OperationException {
        Address address = Address.subsystem("infinispan").and("cache-container", "xyz");

        boolean result = ops.removeIfExists(address);
        assertFalse(result);

        ops.add(address);

        result = ops.removeIfExists(address);
        assertTrue(result);
    }
}
