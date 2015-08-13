package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Builds a list of management operations that will be executed as a batch (composite). Doesn't actually provide
 * a way to execute the batch, this is handled by {@link Operations#batch(Batch)}.</p>
 *
 * <p>The builder is fluent. That is, all methods return {@code this}, which allows creating a batch in a single
 * statement:</p>
 *
 * <pre>
 * Batch batch = new Batch()
 *         .writeAttribute(Address.subsystem("web").and("configuration", "jsp-configuration"),
 *                 "development", true)
 *         .writeAttribute(Address.subsystem("web").and("configuration", "jsp-configuration"),
 *                 "display-source-fragment", true);
 * </pre>
 *
 * @see SingleOperation
 */
public final class Batch implements SharedCommonOperations<Batch> {
    private final List<ModelNode> operations = new ArrayList<ModelNode>();
    private final OperationsModelNodeBuilder builder = new OperationsModelNodeBuilder();

    @Override
    public Batch whoami() throws IOException {
        operations.add(builder.whoami());
        return this;
    }

    @Override
    public Batch readAttribute(Address address, String attributeName, ReadAttributeOption... options) {
        operations.add(builder.readAttribute(address, attributeName, options));
        return this;
    }

    @Override
    public Batch writeAttribute(Address address, String attributeName, boolean attributeValue) {
        operations.add(builder.writeAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeAttribute(Address address, String attributeName, int attributeValue) {
        operations.add(builder.writeAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeAttribute(Address address, String attributeName, long attributeValue) {
        operations.add(builder.writeAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeAttribute(Address address, String attributeName, String attributeValue) {
        operations.add(builder.writeAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeAttribute(Address address, String attributeName, ModelNode attributeValue) {
        operations.add(builder.writeAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeListAttribute(Address address, String attributeName, boolean... attributeValue) {
        operations.add(builder.writeListAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeListAttribute(Address address, String attributeName, int... attributeValue) {
        operations.add(builder.writeListAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeListAttribute(Address address, String attributeName, long... attributeValue) {
        operations.add(builder.writeListAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeListAttribute(Address address, String attributeName, String... attributeValue) {
        operations.add(builder.writeListAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch writeListAttribute(Address address, String attributeName, ModelNode... attributeValue) {
        operations.add(builder.writeListAttribute(address, attributeName, attributeValue));
        return this;
    }

    @Override
    public Batch undefineAttribute(Address address, String attributeName) {
        operations.add(builder.undefineAttribute(address, attributeName));
        return this;
    }

    @Override
    public Batch readResource(Address address, ReadResourceOption... options) {
        operations.add(builder.readResource(address, options));
        return this;
    }

    @Override
    public Batch readChildrenNames(Address address, String childType) {
        operations.add(builder.readChildrenNames(address, childType));
        return this;
    }

    @Override
    public Batch add(Address address) {
        operations.add(builder.add(address));
        return this;
    }

    @Deprecated
    @Override
    public Batch add(Address address, Parameters parameters) {
        return add(address, parameters.toValues());
    }

    @Override
    public Batch add(Address address, Values parameters) {
        operations.add(builder.add(address, parameters));
        return this;
    }

    @Override
    public Batch remove(Address address) {
        operations.add(builder.remove(address));
        return this;
    }

    @Override
    public Batch invoke(String operationName, Address address) {
        operations.add(builder.invoke(operationName, address));
        return this;
    }

    @Deprecated
    @Override
    public Batch invoke(String operationName, Address address, Parameters parameters) {
        return invoke(operationName, address, parameters.toValues());
    }

    @Override
    public Batch invoke(String operationName, Address address, Values parameters) {
        operations.add(builder.invoke(operationName, address, parameters));
        return this;
    }

    // ---

    ModelNode toModelNode() {
        ModelNode composite = new ModelNode();
        composite.get(Constants.OP).set(Constants.COMPOSITE);
        composite.get(Constants.OP_ADDR).setEmptyList();
        ModelNode steps = composite.get(Constants.STEPS);
        for (ModelNode operation : operations) {
            steps.add(operation);
        }
        return composite;
    }
}
