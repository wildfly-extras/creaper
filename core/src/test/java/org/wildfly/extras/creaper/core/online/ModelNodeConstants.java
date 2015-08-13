package org.wildfly.extras.creaper.core.online;

import org.jboss.dmr.ModelNode;

final class ModelNodeConstants {
    private ModelNodeConstants() {} // avoid instantiation

    static final ModelNode EMPTY;
    static final ModelNode SUCCESS;
    static final ModelNode FAILED;
    static final ModelNode DEFINED_RESULT_BOOLEAN;
    static final ModelNode DEFINED_RESULT_NUMBER;
    static final ModelNode DEFINED_RESULT_LIST_BOOLEAN;
    static final ModelNode DEFINED_RESULT_LIST_NUMBER;
    static final ModelNode NOT_DEFINED_RESULT;
    static final ModelNode BATCH_RESULT;
    static final ModelNode RELOAD_REQUIRED;
    static final ModelNode RESTART_REQUIRED;
    static final ModelNode RESTART_REQUIRED_IN_DOMAIN;

    static {
        EMPTY = new ModelNode();
        EMPTY.protect();

        SUCCESS = new ModelNode();
        SUCCESS.get(Constants.OUTCOME).set(Constants.SUCCESS);
        SUCCESS.protect();

        FAILED = new ModelNode();
        FAILED.get(Constants.OUTCOME).set(Constants.FAILED);
        FAILED.protect();

        DEFINED_RESULT_BOOLEAN = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"result\" => true\n"
                + "}");
        DEFINED_RESULT_BOOLEAN.protect();

        DEFINED_RESULT_NUMBER = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"result\" => 13\n"
                + "}");
        DEFINED_RESULT_NUMBER.protect();

        DEFINED_RESULT_LIST_BOOLEAN = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"result\" => [true]\n"
                + "}");
        DEFINED_RESULT_LIST_BOOLEAN.protect();

        DEFINED_RESULT_LIST_NUMBER = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"result\" => [13]\n"
                + "}");
        DEFINED_RESULT_LIST_NUMBER.protect();

        NOT_DEFINED_RESULT = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"result\" => undefined\n"
                + "}");
        NOT_DEFINED_RESULT.protect();

        BATCH_RESULT = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"result\" => {\n"
                + "        \"step-1\" => {\n"
                + "            \"outcome\" => \"success\",\n"
                + "            \"result\" => \"running\"\n"
                + "        },\n"
                + "        \"step-2\" => {\n"
                + "            \"outcome\" => \"success\",\n"
                + "            \"result\" => \"reload-required\"\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
        BATCH_RESULT.protect();

        RELOAD_REQUIRED = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"response-headers\" => {\n"
                + "        \"operation-requires-reload\" => true,\n"
                + "        \"process-state\" => \"reload-required\"\n"
                + "    }\n"
                + "}");
        RELOAD_REQUIRED.protect();

        RESTART_REQUIRED = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"response-headers\" => {\n"
                + "        \"operation-requires-restart\" => true,\n"
                + "        \"process-state\" => \"restart-required\"\n"
                + "    }\n"
                + "}");
        RESTART_REQUIRED.protect();

        RESTART_REQUIRED_IN_DOMAIN = ModelNode.fromString(""
                + "{\n"
                + "    \"outcome\" => \"success\",\n"
                + "    \"result\" => undefined,\n"
                + "    \"server-groups\" => {\"main-server-group\" => {\"host\" => {\"master\" => {\n"
                + "        \"server-one\" => {\"response\" => {\n"
                + "            \"outcome\" => \"success\",\n"
                + "            \"response-headers\" => {\n"
                + "                \"operation-requires-restart\" => true,\n"
                + "                \"process-state\" => \"restart-required\"\n"
                + "            }\n"
                + "        }},\n"
                + "        \"server-two\" => {\"response\" => {\n"
                + "            \"outcome\" => \"success\",\n"
                + "            \"response-headers\" => {\n"
                + "                \"operation-requires-restart\" => true,\n"
                + "                \"process-state\" => \"restart-required\"\n"
                + "            }\n"
                + "        }}\n"
                + "    }}}}\n"
                + "}");
        RESTART_REQUIRED_IN_DOMAIN.protect();
    }
}
