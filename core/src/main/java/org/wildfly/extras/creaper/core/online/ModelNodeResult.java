package org.wildfly.extras.creaper.core.online;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>A convenience subclass of {@link ModelNode} that provides some methods that are common when dealing with
 * management operation results:</p>
 *
 * <ul>
 * <li>Checking for success or failure ({@link #isSuccess()}, {@link #isFailed()})</li>
 * <li>Asserting success or failure ({@link #assertSuccess()}, {@link #assertFailed()})</li>
 * <li>Checking for defined {@code result} value ({@link #hasDefinedValue()})</li>
 * <li>Asserting that a {@code result} value is defined or not ({@link #assertDefinedValue()},
 *     {@link #assertNotDefinedValue()})</li>
 * <li>Getting the result value as a {@link ModelNode} or as various direct types ({@link #value()},
 *     {@link #booleanValue()}, {@link #intValue()}, {@link #longValue()}, {@link #stringValue()})</li>
 * <li>Getting the response headers ({@link #headers()}, {@link #isReloadRequired()}, {@link #isRestartRequired()})</li>
 * <li>Getting result for single server in domain as a {@code ModelNodeResult} ({@link #forServer(String, String)})</li>
 * </ul>
 *
 * <p>Other than that, work with {@code ModelNodeResult} just like with a {@code ModelNode}.</p>
 */
public class ModelNodeResult extends ModelNode {
    /** @deprecated not supposed to be called directly, only for Externalizable */
    public ModelNodeResult() {}

    public ModelNodeResult(ModelNode original) {
        this.set(original);
    }

    // ---
    // assert message

    private String adjustAssertMessage(String message) {
        return message == null || message.isEmpty() ? "" : message + "; ";
    }

    // ---
    // outcome (success/failed)

    public final boolean isSuccess() {
        if (!this.hasDefined(Constants.OUTCOME)) {
            return false;
        }

        String outcome = this.get(Constants.OUTCOME).asString();
        return Constants.SUCCESS.equals(outcome);
    }

    public final boolean isFailed() {
        if (!this.hasDefined(Constants.OUTCOME)) {
            return false;
        }

        String outcome = this.get(Constants.OUTCOME).asString();
        return Constants.FAILED.equals(outcome);
    }

    public final void assertSuccess() {
        assertSuccess(null);
    }

    public final void assertSuccess(String message) {
        message = adjustAssertMessage(message);
        if (!isSuccess()) {
            throw new AssertionError(message + "Expected success, but operation failed: " + this.asString());
        }
    }

    public final void assertFailed() {
        assertFailed(null);
    }

    public final void assertFailed(String message) {
        message = adjustAssertMessage(message);
        if (!isFailed()) {
            throw new AssertionError(message + "Expected failure, but operation succeeded: " + this.asString());
        }
    }

    // ---
    // result value

    public final boolean hasDefinedValue() {
        return this.hasDefined(Constants.RESULT);
    }

    public final void assertDefinedValue() {
        assertDefinedValue(null);
    }

    public final void assertDefinedValue(String message) {
        assertSuccess(message);

        message = adjustAssertMessage(message);
        if (!hasDefinedValue()) {
            throw new AssertionError(message + "Expected defined 'result', but it's missing: " + this.asString());
        }
    }

    public final void assertNotDefinedValue() {
        assertNotDefinedValue(null);
    }

    public final void assertNotDefinedValue(String message) {
        assertSuccess(message);

        message = adjustAssertMessage(message);
        if (this.hasDefined(Constants.RESULT)) {
            throw new AssertionError(message + "Expected NOT defined 'result', but it's present: " + this.asString());
        }
    }

    public final ModelNode value() {
        return get(Constants.RESULT);
    }

    public final boolean booleanValue() {
        return value().asBoolean();
    }

    public final boolean booleanValue(boolean defaultValue) {
        return value().asBoolean(defaultValue);
    }

    public final int intValue() {
        return value().asInt();
    }

    public final int intValue(int defaultValue) {
        return value().asInt(defaultValue);
    }

    public final long longValue() {
        return value().asLong();
    }

    public final long longValue(long defaultValue) {
        return value().asLong(defaultValue);
    }

    public final String stringValue() {
        if (!hasDefinedValue()) {
            throw new IllegalArgumentException();
        }
        return value().asString();
    }

    public final String stringValue(String defaultValue) {
        if (!hasDefinedValue()) {
            return defaultValue;
        }
        return value().asString();
    }

    public final List<ModelNode> listValue() {
        return value().asList();
    }

    public final List<Boolean> booleanListValue() {
        List<ModelNode> listValue = listValue();
        List<Boolean> result = new ArrayList<Boolean>(listValue.size());
        for (ModelNode value : listValue) {
            result.add(value.asBoolean());
        }
        return Collections.unmodifiableList(result);
    }

    public final List<Boolean> booleanListValue(List<Boolean> defaultValue) {
        return hasDefinedValue() ? booleanListValue() : defaultValue;
    }

    public final List<Integer> intListValue() {
        List<ModelNode> listValue = listValue();
        List<Integer> result = new ArrayList<Integer>(listValue.size());
        for (ModelNode value : listValue) {
            result.add(value.asInt());
        }
        return Collections.unmodifiableList(result);
    }

    public final List<Integer> intListValue(List<Integer> defaultValue) {
        return hasDefinedValue() ? intListValue() : defaultValue;
    }

    public final List<Long> longListValue() {
        List<ModelNode> listValue = listValue();
        List<Long> result = new ArrayList<Long>(listValue.size());
        for (ModelNode value : listValue) {
            result.add(value.asLong());
        }
        return Collections.unmodifiableList(result);
    }

    public final List<Long> longListValue(List<Long> defaultValue) {
        return hasDefinedValue() ? longListValue() : defaultValue;
    }

    public final List<String> stringListValue() {
        List<ModelNode> listValue = listValue();
        List<String> result = new ArrayList<String>(listValue.size());
        for (ModelNode value : listValue) {
            result.add(value.asString());
        }
        return Collections.unmodifiableList(result);
    }

    public final List<String> stringListValue(List<String> defaultValue) {
        return hasDefinedValue() ? stringListValue() : defaultValue;
    }

    // ---
    // batch (composite)

    public final ModelNodeResult forBatchStep(int stepIndex) {
        if (stepIndex < 1) {
            throw new IllegalArgumentException("Step number must be > 0 (first step has index 1)");
        }

        List<Property> steps = this.value().asPropertyList();
        if (stepIndex > steps.size()) {
            throw new IllegalArgumentException("No step " + stepIndex + ": " + this.asString());
        }

        return new ModelNodeResult(steps.get(stepIndex - 1).getValue());
    }

    public final Iterable<ModelNodeResult> forAllBatchSteps() {
        final List<Property> steps = this.value().asPropertyList();
        final int stepsCount = steps.size();

        return new Iterable<ModelNodeResult>() {
            @Override
            public final Iterator<ModelNodeResult> iterator() {
                return new Iterator<ModelNodeResult>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < stepsCount;
                    }

                    @Override
                    public ModelNodeResult next() {
                        index++;
                        return new ModelNodeResult(steps.get(index - 1).getValue());
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    // ---
    // headers

    public final ModelNode headers() {
        return this.get(Constants.RESPONSE_HEADERS);
    }

    public final boolean isReloadRequired() {
        ModelNode state = headers().get(Constants.PROCESS_STATE);
        return state.isDefined() && Constants.CONTROLLER_PROCESS_STATE_RELOAD_REQUIRED.equals(state.asString());
    }

    public final boolean isRestartRequired() {
        ModelNode state = headers().get(Constants.PROCESS_STATE);
        return state.isDefined() && Constants.CONTROLLER_PROCESS_STATE_RESTART_REQUIRED.equals(state.asString());
    }

    // ---
    // domain

    public final boolean isFromDomain() {
        return this.hasDefined(Constants.SERVER_GROUPS);
    }

    /**
     * Returns the part of the operation result that is in fact a result of an operation performed on one single server
     * in a domain. The server is identified by the {@code host} name and the {@code server} name. It's not needed
     * to specify the server group, because one host can only belong to one server group.
     * @throws IllegalArgumentException if {@code this} is not an operation result from domain or if no such
     * {@code host} + {@code server} combination is present in {@code this}
     */
    public final ModelNodeResult forServer(String host, String server) {
        if (!isFromDomain()) {
            throw new IllegalArgumentException("Can't call forServer on a result that isn't from domain");
        }

        List<Property> serverGroups = this.get(Constants.SERVER_GROUPS).asPropertyList();
        for (Property serverGroup : serverGroups) {
            ModelNode response = serverGroup.getValue().get(Constants.HOST, host, server, Constants.RESPONSE);
            if (response.isDefined()) {
                return new ModelNodeResult(response);
            }
        }

        throw new IllegalArgumentException("No such host or server: host = " + host + ", server = " + server);
    }
}
