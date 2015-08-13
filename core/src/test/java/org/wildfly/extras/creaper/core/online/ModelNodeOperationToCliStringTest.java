package org.wildfly.extras.creaper.core.online;

import org.jboss.dmr.ModelNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModelNodeOperationToCliStringTest {
    @Test
    public void whoami() {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.WHOAMI);
        op.get(Constants.OP_ADDR).setEmptyList();
        assertEquals("/:whoami", ModelNodeOperationToCliString.convert(op));
    }

    @Test
    public void readResource() {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default").add(Constants.SUBSYSTEM, "web");
        assertEquals("/profile=default/subsystem=web:read-resource", ModelNodeOperationToCliString.convert(op));
    }

    @Test
    public void add() {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.ADD);
        op.get(Constants.OP_ADDR).setEmptyList().add("foo", "bar").add("baz", "quux");
        op.get(Constants.NAME).set("abc");
        op.get(Constants.OPERATION_HEADERS, "xxx").set("zzz");
        assertEquals("/foo=bar/baz=quux:add(name=abc)", ModelNodeOperationToCliString.convert(op));
    }

    @Test
    public void deploy() {
        ModelNode op = ModelNode.fromString("{\"operation\" => \"composite\",\"address\" => [],\"steps\" => [{\"operation\" => \"add\",\"address\" => {\"deployment\" => \"CLIWebservicesWsdlIT.war\"},\"content\" => [{\"bytes\" => bytes { 0x50, 0x4b, 0x03, 0x04, 0x14, 0x00, 0x08, 0x08, 0x08, 0x00, 0xfd, 0x52 }}]},{\"operation\" => \"deploy\",\"address\" => {\"deployment\" => \"CLIWebservicesWsdlIT.war\"}}]}");
        assertEquals("composite: /deployment=CLIWebservicesWsdlIT.war:add(content=[{bytes => <bytes>}]), /deployment=CLIWebservicesWsdlIT.war:deploy",
                ModelNodeOperationToCliString.convert(op));
    }
}
