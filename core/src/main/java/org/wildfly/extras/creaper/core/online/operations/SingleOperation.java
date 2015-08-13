package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;

/**
 * <p>Serves as a factory of {@link DeferredOperation DeferredOperation}s. A deferred operation is a single
 * management operation that can be passed around and later invoked. Useful when it's needed to work with operations
 * like with values (store into variables etc.). It is actually very similar to the {@link Batch} class, except that
 * a batch contains multiple operations, which has some unpleasant effects for the single-operation use case
 * (logging, working with results etc.).</p>
 *
 * <pre>
 * SingleOperation s = new SingleOperation();
 * DeferredOperation op = s.whoami();
 * ... pass "op" around ...
 * op.invoke(client);
 * </pre>
 */
public final class SingleOperation implements SharedCommonOperations<SingleOperation.DeferredOperation> {
    private final OperationsModelNodeBuilder builder;

    public SingleOperation() {
        this(new OperationsModelNodeBuilder());
    }

    private SingleOperation(OperationsModelNodeBuilder builder) {
        this.builder = builder;
    }

    /**
     * <p>Returns a new {@code SingleOperation} object that will add the {@code headers} to all operations it builds.
     * Calling the {@code headers()} method on the resulting object again results in an exception. It is expected
     * to be used in two ways:</p>
     *
     * <ol>
     *     <li>As an {@code SingleOperation} object for all management operations in given scenario,
     *         if the same headers should always be added.</li>
     *     <li>As a one-off {@code SingleOperation} object for a single management operation that should
     *         have the given headers.</li>
     * </ol>
     *
     * <p>Both styles feature minimal syntactic overhead; the second one will needlessly allocate,
     * but that shouldn't be a problem (there are other inherent sources of excessive allocation).</p>
     */
    public SingleOperation headers(Values headers) {
        return new SingleOperation(builder.withHeaders(headers));
    }

    @Override
    public DeferredOperation whoami() {
        return new DeferredOperation(builder.whoami());
    }

    @Override
    public DeferredOperation readAttribute(Address address, String attributeName, ReadAttributeOption... options) {
        return new DeferredOperation(builder.readAttribute(address, attributeName, options));
    }

    @Override
    public DeferredOperation writeAttribute(Address address, String attributeName, boolean attributeValue) {
        return new DeferredOperation(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeAttribute(Address address, String attributeName, int attributeValue) {
        return new DeferredOperation(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeAttribute(Address address, String attributeName, long attributeValue) {
        return new DeferredOperation(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeAttribute(Address address, String attributeName, String attributeValue) {
        return new DeferredOperation(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeAttribute(Address address, String attributeName, ModelNode attributeValue) {
        return new DeferredOperation(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeListAttribute(Address address, String attributeName, boolean... attributeValue) {
        return new DeferredOperation(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeListAttribute(Address address, String attributeName, int... attributeValue) {
        return new DeferredOperation(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeListAttribute(Address address, String attributeName, long... attributeValue) {
        return new DeferredOperation(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeListAttribute(Address address, String attributeName, String... attributeValue) {
        return new DeferredOperation(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation writeListAttribute(Address address, String attributeName, ModelNode... attributeValue) {
        return new DeferredOperation(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public DeferredOperation undefineAttribute(Address address, String attributeName) {
        return new DeferredOperation(builder.undefineAttribute(address, attributeName));
    }

    @Override
    public DeferredOperation readResource(Address address, ReadResourceOption... options) {
        return new DeferredOperation(builder.readResource(address, options));
    }

    @Override
    public DeferredOperation readChildrenNames(Address address, String childType) {
        return new DeferredOperation(builder.readChildrenNames(address, childType));
    }

    @Override
    public DeferredOperation add(Address address) {
        return new DeferredOperation(builder.add(address));
    }

    @Deprecated
    @Override
    public DeferredOperation add(Address address, Parameters parameters) {
        return add(address, parameters.toValues());
    }

    @Override
    public DeferredOperation add(Address address, Values parameters) {
        return new DeferredOperation(builder.add(address, parameters));
    }

    @Override
    public DeferredOperation remove(Address address) {
        return new DeferredOperation(builder.remove(address));
    }

    @Override
    public DeferredOperation invoke(String operationName, Address address) {
        return new DeferredOperation(builder.invoke(operationName, address));
    }

    @Deprecated
    @Override
    public DeferredOperation invoke(String operationName, Address address, Parameters parameters) {
        return invoke(operationName, address, parameters.toValues());
    }

    @Override
    public DeferredOperation invoke(String operationName, Address address, Values parameters) {
        return new DeferredOperation(builder.invoke(operationName, address, parameters));
    }

    // ---

    public static final class DeferredOperation {
        private final ModelNode operation;

        private DeferredOperation(ModelNode operation) {
            this.operation = operation;
        }

        public ModelNodeResult invoke(OnlineManagementClient client) throws IOException {
            return client.execute(operation);
        }
    }
}
