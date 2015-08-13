package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * Verifies that this assumption always holds: {@code new OperationBuilder(op).build().getOperation() == op}. This is
 * required for {@link AdjustOperationForDomain#adjust(org.jboss.as.controller.client.Operation)} to work.
 */
public class ControllerClientAssumptionTest {
    @Test
    public void empty() {
        ModelNode op = new ModelNode();
        assertSame(op, new OperationBuilder(op).build().getOperation());
    }

    @Test
    public void whoami() {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.WHOAMI);
        op.get(Constants.OP_ADDR).setEmptyList();
        assertSame(op, new OperationBuilder(op).build().getOperation());
    }

    @Test
    public void readResource() {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default").add(Constants.SUBSYSTEM, "web");
        assertSame(op, new OperationBuilder(op).build().getOperation());
    }

    @Test
    public void add() {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.ADD);
        op.get(Constants.OP_ADDR).setEmptyList().add("foo", "bar").add("baz", "quux");
        op.get(Constants.NAME).set("abc");
        op.get(Constants.OPERATION_HEADERS, "xxx").set("zzz");
        assertSame(op, new OperationBuilder(op).build().getOperation());
    }
}
