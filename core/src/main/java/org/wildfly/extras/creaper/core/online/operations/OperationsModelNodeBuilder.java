package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.Constants;

final class OperationsModelNodeBuilder implements SharedCommonOperations<ModelNode> {
    private final Values headers;

    OperationsModelNodeBuilder() {
        this(null);
    }

    private OperationsModelNodeBuilder(Values headers) {
        this.headers = headers;
    }

    OperationsModelNodeBuilder withHeaders(Values headers) {
        if (this.headers != null) {
            throw new IllegalStateException("Headers were already set to " + this.headers
                    + ", can't set headers again: " + headers);
        }

        return new OperationsModelNodeBuilder(headers);
    }

    /** This <b>must</b> be used for creating a new {@code ModelNode} that will represent a management operation. */
    private ModelNode newOp() {
        ModelNode op = new ModelNode();
        if (headers != null) {
            headers.addToModelNode(op.get(Constants.OPERATION_HEADERS));
        }
        return op;
    }

    // ---

    @Override
    public ModelNode whoami() {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.WHOAMI);
        op.get(Constants.OP_ADDR).setEmptyList();
        return op;
    }

    @Override
    public ModelNode readAttribute(Address address, String attributeName, ReadAttributeOption... options) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.READ_ATTRIBUTE_OPERATION);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        op.get(Constants.NAME).set(attributeName);
        for (ReadAttributeOption option : options) {
            if ((option instanceof ReadAttributeOptionInternal)) { // only allow internal implementations
                ((ReadAttributeOptionInternal) option).modifyReadAttributeOperation(op);
            }
        }
        return op;
    }

    @Override
    public ModelNode writeAttribute(Address address, String attributeName, boolean attributeValue) {
        return writeAttribute(address, attributeName, new ModelNode(attributeValue));
    }

    @Override
    public ModelNode writeAttribute(Address address, String attributeName, int attributeValue) {
        return writeAttribute(address, attributeName, new ModelNode(attributeValue));
    }

    @Override
    public ModelNode writeAttribute(Address address, String attributeName, long attributeValue) {
        return writeAttribute(address, attributeName, new ModelNode(attributeValue));
    }

    @Override
    public ModelNode writeAttribute(Address address, String attributeName, String attributeValue) {
        return writeAttribute(address, attributeName, new ModelNode(attributeValue));
    }

    @Override
    public ModelNode writeAttribute(Address address, String attributeName, ModelNode attributeValue) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.WRITE_ATTRIBUTE_OPERATION);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        op.get(Constants.NAME).set(attributeName);
        op.get(Constants.VALUE).set(attributeValue);
        return op;
    }

    @Override
    public ModelNode writeListAttribute(Address address, String attributeName, boolean... attributeValue) {
        ModelNode listAttributeValue = new ModelNode().setEmptyList();
        for (boolean singleValue : attributeValue) {
            listAttributeValue.add(singleValue);
        }

        return writeAttribute(address, attributeName, listAttributeValue);
    }

    @Override
    public ModelNode writeListAttribute(Address address, String attributeName, int... attributeValue) {
        ModelNode listAttributeValue = new ModelNode().setEmptyList();
        for (int singleValue : attributeValue) {
            listAttributeValue.add(singleValue);
        }

        return writeAttribute(address, attributeName, listAttributeValue);
    }

    @Override
    public ModelNode writeListAttribute(Address address, String attributeName, long... attributeValue) {
        ModelNode listAttributeValue = new ModelNode().setEmptyList();
        for (long singleValue : attributeValue) {
            listAttributeValue.add(singleValue);
        }

        return writeAttribute(address, attributeName, listAttributeValue);
    }

    @Override
    public ModelNode writeListAttribute(Address address, String attributeName, String... attributeValue) {
        ModelNode listAttributeValue = new ModelNode().setEmptyList();
        for (String singleValue : attributeValue) {
            listAttributeValue.add(singleValue);
        }

        return writeAttribute(address, attributeName, listAttributeValue);
    }

    @Override
    public ModelNode writeListAttribute(Address address, String attributeName, ModelNode... attributeValue) {
        ModelNode listAttributeValue = new ModelNode().setEmptyList();
        for (ModelNode singleValue : attributeValue) {
            listAttributeValue.add(singleValue);
        }

        return writeAttribute(address, attributeName, listAttributeValue);
    }

    @Override
    public ModelNode undefineAttribute(Address address, String attributeName) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.UNDEFINE_ATTRIBUTE_OPERATION);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        op.get(Constants.NAME).set(attributeName);
        return op;
    }

    @Override
    public ModelNode readResource(Address address, ReadResourceOption... options) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.READ_RESOURCE_OPERATION);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        for (ReadResourceOption option : options) {
            if ((option instanceof ReadResourceOptionInternal)) { // only allow internal implementations
                ((ReadResourceOptionInternal) option).modifyReadResourceOperation(op);
            }
        }
        return op;
    }

    @Override
    public ModelNode readChildrenNames(Address address, String childType) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.READ_CHILDREN_NAMES_OPERATION);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        op.get(Constants.CHILD_TYPE).set(childType);
        return op;
    }

    @Override
    public ModelNode add(Address address) {
        return add(address, Values.NONE);
    }

    @Deprecated
    @Override
    public ModelNode add(Address address, Parameters parameters) {
        return add(address, parameters.toValues());
    }

    @Override
    public ModelNode add(Address address, Values parameters) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.ADD);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        parameters.addToModelNode(op);
        return op;
    }

    @Override
    public ModelNode remove(Address address) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(Constants.REMOVE_OPERATION);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        return op;
    }

    @Override
    public ModelNode invoke(String operationName, Address address) {
        return invoke(operationName, address, Values.NONE);
    }

    @Deprecated
    @Override
    public ModelNode invoke(String operationName, Address address, Parameters parameters) {
        return invoke(operationName, address, parameters.toValues());
    }

    @Override
    public ModelNode invoke(String operationName, Address address, Values parameters) {
        ModelNode op = newOp();
        op.get(Constants.OP).set(operationName);
        op.get(Constants.OP_ADDR).set(address.toModelNode());
        parameters.addToModelNode(op);
        return op;
    }
}
