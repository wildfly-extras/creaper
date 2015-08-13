package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.wildfly.extras.creaper.core.online.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    }
}
