package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.junit.Test;
import org.wildfly.extras.creaper.core.online.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AddressTest {
    @Test
    public void rootAddress() {
        Address emptyAddress = Address.root();
        ModelNode modelNode = emptyAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertFalse(modelNode.hasDefined(0));

        assertEquals("/", emptyAddress.toString());
        assertNull(emptyAddress.getLastPairValue());
    }

    @Test
    public void singleElementAddress() {
        Address singleElementAddress = Address.of("a", "b");
        ModelNode modelNode = singleElementAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertFalse(modelNode.hasDefined(1));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals("a", firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("b", firstElement.asProperty().getValue().asString());

        assertEquals("/a=b", singleElementAddress.toString());
        assertEquals("b", singleElementAddress.getLastPairValue());
    }

    @Test
    public void twoElementsAddress() {
        Address twoElementsAddress = Address.of("a", "b").and("c", "d");
        ModelNode modelNode = twoElementsAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertTrue(modelNode.hasDefined(1));
        assertFalse(modelNode.hasDefined(2));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals("a", firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("b", firstElement.asProperty().getValue().asString());

        ModelNode secondElement = modelNode.get(1);
        assertEquals(ModelType.PROPERTY, secondElement.getType());
        assertEquals("c", secondElement.asProperty().getName());
        assertEquals(ModelType.STRING, secondElement.asProperty().getValue().getType());
        assertEquals("d", secondElement.asProperty().getValue().asString());

        assertEquals("/a=b/c=d", twoElementsAddress.toString());
        assertEquals("d", twoElementsAddress.getLastPairValue());
    }

    @Test
    public void extensionAddress() {
        Address singleElementAddress = Address.extension("org.jboss.as.logging");
        ModelNode modelNode = singleElementAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertFalse(modelNode.hasDefined(1));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals(Constants.EXTENSION, firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("org.jboss.as.logging", firstElement.asProperty().getValue().asString());

        assertEquals("/extension=org.jboss.as.logging", singleElementAddress.toString());
        assertEquals("org.jboss.as.logging", singleElementAddress.getLastPairValue());
    }

    @Test
    public void profileAddress() {
        Address singleElementAddress = Address.profile("default");
        ModelNode modelNode = singleElementAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertFalse(modelNode.hasDefined(1));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals(Constants.PROFILE, firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("default", firstElement.asProperty().getValue().asString());

        assertEquals("/profile=default", singleElementAddress.toString());
        assertEquals("default", singleElementAddress.getLastPairValue());
    }

    @Test
    public void hostAddress() {
        Address singleElementAddress = Address.host("master");
        ModelNode modelNode = singleElementAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertFalse(modelNode.hasDefined(1));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals(Constants.HOST, firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("master", firstElement.asProperty().getValue().asString());

        assertEquals("/host=master", singleElementAddress.toString());
        assertEquals("master", singleElementAddress.getLastPairValue());
    }

    @Test
    public void subsystemAddress() {
        Address singleElementAddress = Address.subsystem("foo");
        ModelNode modelNode = singleElementAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertFalse(modelNode.hasDefined(1));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals(Constants.SUBSYSTEM, firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("foo", firstElement.asProperty().getValue().asString());

        assertEquals("/subsystem=foo", singleElementAddress.toString());
        assertEquals("foo", singleElementAddress.getLastPairValue());
    }

    @Test
    public void coreServiceAddress() {
        Address singleElementAddress = Address.coreService("management");
        ModelNode modelNode = singleElementAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertFalse(modelNode.hasDefined(1));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals(Constants.CORE_SERVICE, firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("management", firstElement.asProperty().getValue().asString());

        assertEquals("/core-service=management", singleElementAddress.toString());
        assertEquals("management", singleElementAddress.getLastPairValue());
    }

    @Test
    public void deploymentAddress() {
        Address singleElementAddress = Address.deployment("simple.war");
        ModelNode modelNode = singleElementAddress.toModelNode();

        assertTrue(modelNode.isDefined());
        assertEquals(ModelType.LIST, modelNode.getType());
        assertTrue(modelNode.hasDefined(0));
        assertFalse(modelNode.hasDefined(1));

        ModelNode firstElement = modelNode.get(0);
        assertEquals(ModelType.PROPERTY, firstElement.getType());
        assertEquals(Constants.DEPLOYMENT, firstElement.asProperty().getName());
        assertEquals(ModelType.STRING, firstElement.asProperty().getValue().getType());
        assertEquals("simple.war", firstElement.asProperty().getValue().asString());

        assertEquals("/deployment=simple.war", singleElementAddress.toString());
        assertEquals("simple.war", singleElementAddress.getLastPairValue());
    }
}
