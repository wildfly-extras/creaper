package org.wildfly.extras.creaper.core.online;

import org.jboss.dmr.ModelNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AdjustOperationForDomainTest {
    @Test
    public void modelNodeOperation_standalone() {
        OnlineOptions options = OnlineOptions.standalone().localDefault().build();
        AdjustOperationForDomain adjust = new AdjustOperationForDomain(options);

        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        ModelNode adjustedOp = new ModelNode();
        adjustedOp.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.SUBSYSTEM, "web");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.SUBSYSTEM, "web");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.CORE_SERVICE, "management");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.CORE_SERVICE, "management");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.INTERFACE, "public");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.INTERFACE, "public");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.SOCKET_BINDING_GROUP, "standard-sockets");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.SOCKET_BINDING_GROUP, "standard-sockets");
        assertEquals(adjustedOp, adjust.adjust(op));
    }

    @Test
    public void modelNodeOperation_domain() {
        OnlineOptions options = OnlineOptions.domain().forHost("master").forProfile("default").build()
                .localDefault().build();
        AdjustOperationForDomain adjust = new AdjustOperationForDomain(options);

        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        ModelNode adjustedOp = new ModelNode();
        adjustedOp.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.SUBSYSTEM, "web");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default")
                .add(Constants.SUBSYSTEM, "web");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.CORE_SERVICE, "management");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.HOST, "master")
                .add(Constants.CORE_SERVICE, "management");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.INTERFACE, "public");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.INTERFACE, "public");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.SOCKET_BINDING_GROUP, "standard-sockets");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.SOCKET_BINDING_GROUP, "standard-sockets");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default").add(Constants.SUBSYSTEM, "web");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default").add(Constants.SUBSYSTEM, "web");
        assertEquals(adjustedOp, adjust.adjust(op));

        op.get(Constants.OP_ADDR).setEmptyList().add(Constants.HOST, "master")
                .add(Constants.CORE_SERVICE, "management");
        adjustedOp.get(Constants.OP_ADDR).setEmptyList().add(Constants.HOST, "master")
                .add(Constants.CORE_SERVICE, "management");
        assertEquals(adjustedOp, adjust.adjust(op));
    }

    @Test
    public void batch_modelNodeOperation_domain() {
        OnlineOptions options = OnlineOptions.domain().forHost("master").forProfile("default").build()
                .localDefault().build();
        AdjustOperationForDomain adjust = new AdjustOperationForDomain(options);

        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.COMPOSITE);
        op.get(Constants.OP_ADDR).setEmptyList();
        op.get(Constants.STEPS).setEmptyList();

        ModelNode adjustedOp = new ModelNode();
        adjustedOp.get(Constants.OP).set(Constants.COMPOSITE);
        adjustedOp.get(Constants.OP_ADDR).setEmptyList();
        adjustedOp.get(Constants.STEPS).setEmptyList();

        ModelNode step = new ModelNode();
        ModelNode adjustedStep = new ModelNode();
        step.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        step.get(Constants.OP_ADDR).setEmptyList().add(Constants.SUBSYSTEM, "web");
        adjustedStep.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        adjustedStep.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default")
                .add(Constants.SUBSYSTEM, "web");
        op.get(Constants.STEPS).add(step);
        adjustedOp.get(Constants.STEPS).add(adjustedStep);

        step = new ModelNode();
        adjustedStep = new ModelNode();
        step.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        step.get(Constants.OP_ADDR).setEmptyList().add(Constants.CORE_SERVICE, "management");
        adjustedStep.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        adjustedStep.get(Constants.OP_ADDR).setEmptyList().add(Constants.HOST, "master")
                .add(Constants.CORE_SERVICE, "management");
        op.get(Constants.STEPS).add(step);
        adjustedOp.get(Constants.STEPS).add(adjustedStep);

        step = new ModelNode();
        adjustedStep = new ModelNode();
        step.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        step.get(Constants.OP_ADDR).setEmptyList().add(Constants.INTERFACE, "public");
        adjustedStep.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        adjustedStep.get(Constants.OP_ADDR).setEmptyList().add(Constants.INTERFACE, "public");
        op.get(Constants.STEPS).add(step);
        adjustedOp.get(Constants.STEPS).add(adjustedStep);

        step = new ModelNode();
        adjustedStep = new ModelNode();
        step.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        step.get(Constants.OP_ADDR).setEmptyList().add(Constants.SOCKET_BINDING_GROUP, "standard-sockets");
        adjustedStep.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        adjustedStep.get(Constants.OP_ADDR).setEmptyList().add(Constants.SOCKET_BINDING_GROUP, "standard-sockets");
        op.get(Constants.STEPS).add(step);
        adjustedOp.get(Constants.STEPS).add(adjustedStep);

        step = new ModelNode();
        adjustedStep = new ModelNode();
        step.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        step.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default").add(Constants.SUBSYSTEM, "web");
        adjustedStep.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        adjustedStep.get(Constants.OP_ADDR).setEmptyList().add(Constants.PROFILE, "default")
                .add(Constants.SUBSYSTEM, "web");
        op.get(Constants.STEPS).add(step);
        adjustedOp.get(Constants.STEPS).add(adjustedStep);

        step = new ModelNode();
        adjustedStep = new ModelNode();
        step.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        step.get(Constants.OP_ADDR).setEmptyList().add(Constants.HOST, "master")
                .add(Constants.CORE_SERVICE, "management");
        adjustedStep.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        adjustedStep.get(Constants.OP_ADDR).setEmptyList().add(Constants.HOST, "master")
                .add(Constants.CORE_SERVICE, "management");
        op.get(Constants.STEPS).add(step);
        adjustedOp.get(Constants.STEPS).add(adjustedStep);

        assertEquals(adjustedOp, adjust.adjust(op));
    }

    @Test
    public void stringOperation_standalone() {
        OnlineOptions options = OnlineOptions.standalone().localDefault().build();
        AdjustOperationForDomain adjust = new AdjustOperationForDomain(options);

        assertEquals("/subsystem=web:read-resource", adjust.adjust("/subsystem=web:read-resource"));
        assertEquals("/core-service=management:read-resource", adjust.adjust("/core-service=management:read-resource"));
        assertEquals("/interface=public:read-resource", adjust.adjust("/interface=public:read-resource"));
        assertEquals("/socket-binding-group=standard-sockets:read-resource",
                adjust.adjust("/socket-binding-group=standard-sockets:read-resource"));
    }

    @Test
    public void stringOperation_domain() {
        OnlineOptions options = OnlineOptions.domain().forHost("master").forProfile("default").build()
                .localDefault().build();
        AdjustOperationForDomain adjust = new AdjustOperationForDomain(options);

        assertEquals("/profile=default/subsystem=web:read-resource", adjust.adjust("/subsystem=web:read-resource"));
        assertEquals("/host=master/core-service=management:read-resource",
                adjust.adjust("/core-service=management:read-resource"));
        assertEquals("/interface=public:read-resource", adjust.adjust("/interface=public:read-resource"));
        assertEquals("/socket-binding-group=standard-sockets:read-resource",
                adjust.adjust("/socket-binding-group=standard-sockets:read-resource"));

        assertEquals("/profile=default/subsystem=web:read-resource",
                adjust.adjust("/profile=default/subsystem=web:read-resource"));
        assertEquals("/host=master/core-service=management:read-resource",
                adjust.adjust("/host=master/core-service=management:read-resource"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownProfile() {
        OnlineOptions options = OnlineOptions.domain().build().localDefault().build();
        AdjustOperationForDomain adjust = new AdjustOperationForDomain(options);

        adjust.adjust("/subsystem=web:read-resource");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownHost() {
        OnlineOptions options = OnlineOptions.domain().build().localDefault().build();
        AdjustOperationForDomain adjust = new AdjustOperationForDomain(options);

        adjust.adjust("/core-service=management:read-resource");
    }
}
