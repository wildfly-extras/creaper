package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;

import java.io.IOException;

/**
 * <p>A convenience for commonly performed management operations. The intent is to have a statically typed API
 * that is as easy to use as the CLI yet doesn't involve string manipulation.</p>
 *
 * <p>This class contains an {@link OnlineManagementClient}, but is otherwise stateless. Most importantly, this class
 * <b>doesn't</b> close the underlying {@code OnlineManagementClient}. This means that as long as that
 * {@code OnlineManagementClient} is valid, this class is usable.</p>
 */
public final class Operations implements SharedCommonOperations<ModelNodeResult> {
    private final OnlineManagementClient client;
    private final OperationsModelNodeBuilder builder;

    public Operations(OnlineManagementClient client) {
        this(client, new OperationsModelNodeBuilder());
    }

    private Operations(OnlineManagementClient client, OperationsModelNodeBuilder builder) {
        this.client = client;
        this.builder = builder;
    }

    /**
     * <p>Returns a new {@code Operations} object that will add the {@code headers} to all operations it executes.
     * Calling the {@code headers()} method on the resulting object again results in an exception. It is expected
     * to be used in two ways:</p>
     *
     * <ol>
     *     <li>As an {@code Operations} object for all management operations in given scenario,
     *         if the same headers should always be added.</li>
     *     <li>As a one-off {@code Operations} object for a single management operation that should
     *         have the given headers.</li>
     * </ol>
     *
     * <p>Both styles feature minimal syntactic overhead; the second one will needlessly allocate,
     * but that shouldn't be a problem (there are other inherent sources of excessive allocation).</p>
     */
    public Operations headers(Values headers) {
        return new Operations(client, builder.withHeaders(headers));
    }

    @Override
    public ModelNodeResult whoami() throws IOException {
        return client.execute(builder.whoami());
    }

    @Override
    public ModelNodeResult readAttribute(Address address, String attributeName, ReadAttributeOption... options)
            throws IOException {
        return client.execute(builder.readAttribute(address, attributeName, options));
    }

    @Override
    public ModelNodeResult writeAttribute(Address address, String attributeName, boolean attributeValue)
            throws IOException {
        return client.execute(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeAttribute(Address address, String attributeName, int attributeValue)
            throws IOException {
        return client.execute(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeAttribute(Address address, String attributeName, long attributeValue)
            throws IOException {
        return client.execute(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeAttribute(Address address, String attributeName, String attributeValue)
            throws IOException {
        return client.execute(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeAttribute(Address address, String attributeName, ModelNode attributeValue)
            throws IOException {
        return client.execute(builder.writeAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeListAttribute(Address address, String attributeName, boolean... attributeValue)
            throws IOException {
        return client.execute(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeListAttribute(Address address, String attributeName, int... attributeValue)
            throws IOException {
        return client.execute(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeListAttribute(Address address, String attributeName, long... attributeValue)
            throws IOException {
        return client.execute(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeListAttribute(Address address, String attributeName, String... attributeValue)
            throws IOException {
        return client.execute(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult writeListAttribute(Address address, String attributeName, ModelNode... attributeValue)
            throws IOException {
        return client.execute(builder.writeListAttribute(address, attributeName, attributeValue));
    }

    @Override
    public ModelNodeResult undefineAttribute(Address address, String attributeName) throws IOException {
        return client.execute(builder.undefineAttribute(address, attributeName));
    }

    @Override
    public ModelNodeResult readResource(Address address, ReadResourceOption... options) throws IOException {
        return client.execute(builder.readResource(address, options));
    }

    @Override
    public ModelNodeResult readChildrenNames(Address address, String childType) throws IOException {
        return client.execute(builder.readChildrenNames(address, childType));
    }

    @Override
    public ModelNodeResult add(Address address) throws IOException {
        return client.execute(builder.add(address));
    }

    @Deprecated
    @Override
    public ModelNodeResult add(Address address, Parameters parameters) throws IOException {
        return add(address, parameters.toValues());
    }

    @Override
    public ModelNodeResult add(Address address, Values parameters) throws IOException {
        return client.execute(builder.add(address, parameters));
    }

    @Override
    public ModelNodeResult remove(Address address) throws IOException {
        return client.execute(builder.remove(address));
    }

    @Override
    public ModelNodeResult invoke(String operationName, Address address) throws IOException {
        return client.execute(builder.invoke(operationName, address));
    }

    @Deprecated
    @Override
    public ModelNodeResult invoke(String operationName, Address address, Parameters parameters) throws IOException {
        return invoke(operationName, address, parameters.toValues());
    }

    @Override
    public ModelNodeResult invoke(String operationName, Address address, Values parameters) throws IOException {
        return client.execute(builder.invoke(operationName, address, parameters));
    }

    // ---

    public ModelNodeResult batch(Batch batch) throws IOException {
        return client.execute(batch.toModelNode());
    }

    /**
     * @return {@code true} if the resource specified by {@code address} exists, {@code false} otherwise
     * @throws OperationException if the underlying {@code read-resource} operation fails
     */
    public boolean exists(Address address) throws IOException, OperationException {
        ModelNodeResult result = readResource(address);
        if (result.isSuccess()) {
            return result.hasDefinedValue(); // should always be true
        }

        if (isResultUnknownOrNotFound(result)) {
            return false;
        }

        throw new OperationException("exists failed: " + result.asString());
    }

    /**
     * @return {@code true} if the resource specified by {@code address} was actually removed (i.e., it used to exist),
     * {@code false} if it didn't exist and therefore wasn't actually removed
     * @throws OperationException if the underlying {@code remove} operation fails with something else than "not found"
     */
    public boolean removeIfExists(Address address) throws IOException, OperationException {
        ModelNodeResult result = remove(address);
        if (result.isSuccess()) {
            return true;
        }

        if (isResultUnknownOrNotFound(result)) {
            return false;
        }

        throw new OperationException("removeIfExists failed: " + result.asString());
    }

    private static boolean isResultUnknownOrNotFound(ModelNodeResult result) {
        result.assertFailed();

        ModelNode failureDescription = result.get(Constants.FAILURE_DESCRIPTION);
        if (failureDescription.hasDefined(Constants.DOMAIN_FAILURE_DESCRIPTION)) {
            failureDescription = failureDescription.get(Constants.DOMAIN_FAILURE_DESCRIPTION);
        }

        String failureDescriptionString = failureDescription.asString();
        for (String code : Constants.RESULT_CODES_FOR_UNKNOWN_OR_NOT_FOUND) {
            if (failureDescriptionString.startsWith(code)) {
                return true;
            }
        }

        return false;
    }
}
