package org.wildfly.extras.creaper.core.online.operations;

import org.jboss.dmr.ModelNode;
import org.junit.Test;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;

import static org.junit.Assert.assertTrue;

public class UnknownOrNotFoundResultTest {
    private static final ModelNodeResult SIMPLE_FAILURE = new ModelNodeResult(ModelNode.fromString(""
            + "{\n"
            + "    \"outcome\" => \"failed\",\n"
            + "    \"failure-description\" => \"JBAS014883: No resource definition is registered for address [\n"
            + "    (\\\"profile\\\" => \\\"default\\\"),\n"
            + "    (\\\"subsystem\\\" => \\\"fubar\\\")\n"
            + "]\",\n"
            + "    \"rolled-back\" => true\n"
            + "}"));

    private static final ModelNodeResult DOMAIN_FAILURE = new ModelNodeResult(ModelNode.fromString(""
            + "{\n"
            + "    \"outcome\" => \"failed\",\n"
            + "    \"failure-description\" => {\"domain-failure-description\" => \"JBAS014807: Management resource '["
            + "(\\\"profile\\\" => \\\"fubar\\\")]' not found\"},\n"
            + "    \"rolled-back\" => true\n"
            + "}"));

    private static final ModelNodeResult HOST_FAILURE = new ModelNodeResult(ModelNode.fromString(""
            + "{\n"
            + "    \"outcome\" => \"failed\",\n"
            + "    \"failure-description\" => {\"host-failure-descriptions\" => [(\"master\" => \"JBAS014807: "
            + "Management resource '[\n"
            + "    (\\\"host\\\" => \\\"master\\\"),\n"
            + "    (\\\"server-config\\\" => \\\"fubar\\\")\n"
            + "]' not found\")]},\n"
            + "    \"rolled-back\" => true\n"
            + "}"));

    @Test
    public void simpleFailure() {
        assertTrue(Operations.isResultUnknownOrNotFound(SIMPLE_FAILURE));
    }

    @Test
    public void domainFailure() {
        assertTrue(Operations.isResultUnknownOrNotFound(DOMAIN_FAILURE));
    }

    @Test
    public void hostFailure() {
        assertTrue(Operations.isResultUnknownOrNotFound(HOST_FAILURE));
    }
}
